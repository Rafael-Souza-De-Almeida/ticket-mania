package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.TicketUnavailableException;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import com.stripe.exception.StripeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTransactionalServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderTransactionalService orderTransactionalService;

    private UUID userId;
    private UUID ticketId;
    private Ticket ticket;
    private OrderRequestDto requestDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        ticketId = UUID.randomUUID();
        requestDto = new OrderRequestDto(ticketId);

        Event event = Event.builder().id(UUID.randomUUID()).name("Show").place("Arena").capacity(1000).build();

        ticket = Ticket.builder()
                .id(ticketId)
                .event(event)
                .sectorCode("A_1")
                .price(new BigDecimal("150.00"))
                .type(TicketType.FULL)
                .status(TicketStatus.AVAILABLE)
                .build();
    }

    @Test
    @DisplayName("createWithTransaction - should reserve the ticket, persist the order and return the payment secret")
    void shouldReserveTicketAndCreateOrder() throws StripeException {
        when(ticketRepository.findByIdAndStatus(ticketId, TicketStatus.AVAILABLE)).thenReturn(Optional.of(ticket));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });
        when(paymentService.createPaymentIntent(any(Order.class))).thenReturn("client_secret_123");

        OrderResponseDto response = orderTransactionalService.createWithTransaction(requestDto, userId);

        assertEquals(ticketId, response.ticketId());
        assertEquals(userId, response.userId());
        assertEquals("client_secret_123", response.paymentClientSecret());

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, times(1)).save(ticketCaptor.capture());
        assertEquals(TicketStatus.RESERVED, ticketCaptor.getValue().getStatus());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("createWithTransaction - should throw TicketUnavailableException when ticket is not available")
    void shouldThrowWhenTicketIsUnavailable() {
        when(ticketRepository.findByIdAndStatus(ticketId, TicketStatus.AVAILABLE)).thenReturn(Optional.empty());

        assertThrows(TicketUnavailableException.class,
                () -> orderTransactionalService.createWithTransaction(requestDto, userId));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("createWithTransaction - should wrap StripeException in a RuntimeException")
    void shouldWrapStripeExceptionInRuntimeException() throws StripeException {
        when(ticketRepository.findByIdAndStatus(ticketId, TicketStatus.AVAILABLE)).thenReturn(Optional.of(ticket));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });

        StripeException stripeException = org.mockito.Mockito.mock(StripeException.class);
        when(paymentService.createPaymentIntent(any(Order.class))).thenThrow(stripeException);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> orderTransactionalService.createWithTransaction(requestDto, userId));

        assertEquals(stripeException, thrown.getCause());
    }
}
