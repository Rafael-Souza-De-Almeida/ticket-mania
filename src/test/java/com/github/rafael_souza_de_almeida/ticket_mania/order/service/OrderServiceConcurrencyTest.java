package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.repository.EventRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class OrderServiceConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private PaymentService paymentService;

    private Ticket ticket;

    @BeforeEach
    void setUp() throws StripeException {
        orderRepository.deleteAll();
        ticketRepository.deleteAll();
        eventRepository.deleteAll();

        Event event = Event.builder()
                .name("Concurrency Test Event")
                .place("Stadium")
                .capacity(100)
                .date(LocalDateTime.now().plusDays(10))
                .build();
        eventRepository.save(event);

        ticket = Ticket.builder()
                .event(event)
                .sectorCode("VIP_1")
                .price(new BigDecimal("100.00"))
                .type(TicketType.VIP)
                .status(TicketStatus.AVAILABLE)
                .build();
        ticketRepository.save(ticket);

        when(paymentService.createPaymentIntent(any(Order.class))).thenReturn("fake_client_secret");
    }

    @Test
    void shouldAllowOnlyOneOrderWhen8UsersTryToBuyTheSameTicketSimultaneously() throws InterruptedException {

        int numOfThreads = 8;

        ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);

        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);


        for (int i = 0; i < numOfThreads; i++) {
            OrderRequestDto requestDto = new OrderRequestDto(ticket.getId());


            User fakeUser = User.builder().id(UUID.randomUUID()).email("user" + i + "@test.com").build();

            var authToken = new UsernamePasswordAuthenticationToken(fakeUser, null, fakeUser.getAuthorities());

            executorService.submit(() -> {
                try {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    context.setAuthentication(authToken);
                    SecurityContextHolder.setContext(context);

                    latch.await();
                    orderService.create(requestDto);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    SecurityContextHolder.clearContext();
                    doneLatch.countDown();
                }
            });
        }


        latch.countDown();

        doneLatch.await();

        assertEquals(1, successCount.get(), "Only one purchase should confirm successfully");
        assertEquals(7, failureCount.get(), "Seven requests should fail");

        long totalOrdersInDb = orderRepository.count();
        assertEquals(1, totalOrdersInDb, "Only one order should be registered in db");

        Ticket updatedTicket = ticketRepository.findById(ticket.getId()).orElseThrow();
        assertEquals(TicketStatus.RESERVED, updatedTicket.getStatus());

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }

}