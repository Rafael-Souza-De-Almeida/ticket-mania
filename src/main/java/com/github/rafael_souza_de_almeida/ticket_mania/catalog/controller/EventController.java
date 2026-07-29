
package com.github.rafael_souza_de_almeida.ticket_mania.catalog.controller;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto.EventResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.catalog.service.EventService;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.TicketBatchRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Gerenciamento de eventos e emissão de ingressos")
public class EventController {

    private final EventService eventService;
    private final TicketService ticketService;

    @GetMapping
    @Operation(summary = "Listar eventos", description = "Retorna todos os eventos disponíveis para venda.")
    public ResponseEntity<List<EventResponseDto>> findAll() {
        return ResponseEntity.ok(eventService.findAll());
    }

    @PostMapping
    @Operation(summary = "Criar evento", description = "Cria um novo evento com os dados fornecidos.")
    public ResponseEntity<EventResponseDto> create(@RequestBody @Valid EventRequestDto requestDto) {

        EventResponseDto response = eventService.create(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/{id}/tickets")
    @Operation(summary = "Gerar ingressos", description = "Gera um lote de ingressos para um evento existente.")
    public ResponseEntity<Void> add_tickets(@PathVariable UUID id, @RequestBody TicketBatchRequestDto dto) {

        ticketService.generateTicketsForEvent(id, dto);

        return ResponseEntity.status(HttpStatus.OK).build();

    }


}
