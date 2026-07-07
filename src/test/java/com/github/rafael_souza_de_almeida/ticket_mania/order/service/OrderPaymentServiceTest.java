package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.core.rabbitMq.RabbitMQConfig;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.OrderNotFoundException;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderPaymentService orderPaymentService;

    private UUID orderId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        order = Order.builder()
                .id(orderId)
                .userId(UUID.randomUUID())
                .status(OrderStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("fulfillOrder - should mark the order as COMPLETED and publish the fulfillment event")
    void shouldFulfillPendingOrder() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderPaymentService.fulfillOrder(orderId);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
        verify(rabbitTemplate, times(1)).convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                orderId.toString());
    }

    @Test
    @DisplayName("fulfillOrder - should throw OrderNotFoundException when order does not exist")
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderPaymentService.fulfillOrder(orderId));

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("fulfillOrder - should throw IllegalStateException when order is already completed")
    void shouldThrowWhenOrderAlreadyCompleted() {
        order.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderPaymentService.fulfillOrder(orderId));

        verify(orderRepository, never()).save(order);
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }
}
