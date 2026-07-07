package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.repository.EventRepository;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.TicketBatchRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.EventNotFoundException;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private TicketService ticketService;

    private UUID eventId;
    private Event event;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        event = Event.builder()
                .id(eventId)
                .name("Show da Virada")
                .place("Copacabana")
                .capacity(100000)
                .date(LocalDateTime.now().plusMonths(3))
                .build();
    }

    @Test
    @DisplayName("generateTicketsForEvent - should generate the requested quantity of tickets with unique sector codes")
    void shouldGenerateRequestedQuantityOfTickets() {
        TicketBatchRequestDto dto = new TicketBatchRequestDto(5, "VIP", TicketType.VIP, new BigDecimal("250.00"));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        ArgumentCaptor<List<Ticket>> captor = ArgumentCaptor.forClass(List.class);

        ticketService.generateTicketsForEvent(eventId, dto);

        verify(ticketRepository, times(1)).saveAll(captor.capture());

        List<Ticket> savedTickets = captor.getValue();
        assertEquals(5, savedTickets.size());

        List<String> sectorCodes = savedTickets.stream().map(Ticket::getSectorCode).collect(Collectors.toList());
        assertEquals(List.of("VIP_1", "VIP_2", "VIP_3", "VIP_4", "VIP_5"), sectorCodes);

        for (Ticket ticket : savedTickets) {
            assertEquals(event, ticket.getEvent());
            assertEquals(TicketType.VIP, ticket.getType());
            assertEquals(TicketStatus.AVAILABLE, ticket.getStatus());
            assertEquals(new BigDecimal("250.00"), ticket.getPrice());
        }
    }

    @Test
    @DisplayName("generateTicketsForEvent - should throw EventNotFoundException when event does not exist")
    void shouldThrowWhenEventNotFound() {
        TicketBatchRequestDto dto = new TicketBatchRequestDto(5, "VIP", TicketType.VIP, new BigDecimal("250.00"));

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> ticketService.generateTicketsForEvent(eventId, dto));

        verify(ticketRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("generateTicketsForEvent - should not persist any ticket when requested quantity is zero")
    void shouldNotPersistTicketsWhenQuantityIsZero() {
        TicketBatchRequestDto dto = new TicketBatchRequestDto(0, "VIP", TicketType.VIP, new BigDecimal("250.00"));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        ArgumentCaptor<List<Ticket>> captor = ArgumentCaptor.forClass(List.class);

        ticketService.generateTicketsForEvent(eventId, dto);

        verify(ticketRepository, times(1)).saveAll(captor.capture());
        assertEquals(0, captor.getValue().size());
    }
}
