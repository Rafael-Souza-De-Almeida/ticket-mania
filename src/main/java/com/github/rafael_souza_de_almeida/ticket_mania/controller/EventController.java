package com.github.rafael_souza_de_almeida.ticket_mania.controller;

import com.github.rafael_souza_de_almeida.ticket_mania.dto.EventRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.dto.EventResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventResponseDto>> findAll() {
        return ResponseEntity.ok(eventService.findAll());
    }

    @PostMapping
    public ResponseEntity<EventResponseDto> create(@RequestBody @Valid EventRequestDto requestDto) {

        EventResponseDto response = eventService.create(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }


}
