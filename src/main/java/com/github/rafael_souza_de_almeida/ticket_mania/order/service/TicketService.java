package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.repository.EventRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.TicketBatchRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.EventNotFoundException;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    @Transactional
    public void generateTicketsForEvent(UUID eventId, TicketBatchRequestDto dto) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        List<Ticket> ticketsToSave = new ArrayList<>();

        for (int i = 1; i <= dto.quantity(); i++) {

            String uniqueCode = dto.sectorCode() + "_" + i;

            Ticket ticket = Ticket.builder()
                    .event(event)
                    .sectorCode(uniqueCode)
                    .price(dto.price())
                    .status(TicketStatus.AVAILABLE)
                    .build();

            ticketsToSave.add(ticket);
        }

        ticketRepository.saveAll(ticketsToSave);
    }
}
