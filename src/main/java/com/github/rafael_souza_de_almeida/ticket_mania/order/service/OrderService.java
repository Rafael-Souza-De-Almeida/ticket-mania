package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.TicketUnavailableException;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;


    @Transactional
    public OrderResponseDto create(OrderRequestDto dto) {

        // Implement User id verification

        Ticket ticket = ticketRepository.findByIdAndStatus(dto.ticketId(), TicketStatus.AVAILABLE)
                .orElseThrow(() -> new TicketUnavailableException("Unavailable ticket."));

        ticket.setStatus(TicketStatus.RESERVED);

        Order order = Order.builder()
                .ticket(ticket)
                .userId(dto.userId())
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        return new OrderResponseDto(savedOrder.getId(), savedOrder.getTicket().getId(), savedOrder.getUserId());



    }



}
