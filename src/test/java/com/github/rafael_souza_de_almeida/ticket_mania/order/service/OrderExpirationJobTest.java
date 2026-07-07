package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderExpirationJobTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private OrderExpirationJob orderExpirationJob;

    @Test
    @DisplayName("cancelExpiredOrders - should cancel expired pending orders and free up their tickets")
    void shouldCancelExpiredOrdersAndFreeTickets() {
        Ticket ticket = Ticket.builder().id(UUID.randomUUID()).status(TicketStatus.RESERVED).build();
        Order expiredOrder = Order.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .ticket(ticket)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now().minusMinutes(20))
                .build();

        when(orderRepository.findByStatusAndCreatedAtBefore(eq(OrderStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(expiredOrder));

        orderExpirationJob.cancelExpiredOrders();

        assertEquals(OrderStatus.CANCELLED, expiredOrder.getStatus());
        assertEquals(TicketStatus.AVAILABLE, ticket.getStatus());
        verify(ticketRepository, times(1)).save(ticket);
        verify(orderRepository, times(1)).save(expiredOrder);
    }

    @Test
    @DisplayName("cancelExpiredOrders - should do nothing when there are no expired orders")
    void shouldDoNothingWhenNoExpiredOrders() {
        when(orderRepository.findByStatusAndCreatedAtBefore(eq(OrderStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        orderExpirationJob.cancelExpiredOrders();

        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(orderRepository, never()).save(any(Order.class));
    }
}
