package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.core.rabbitMq.RabbitMQConfig;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.OrderNotFoundException;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void fulfillOrder(UUID orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if(order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Order is already fulfilled and paid.");
        }

        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                order.getId().toString()
        );

    }

}
