package com.github.rafael_souza_de_almeida.ticket_mania.service;

import com.github.rafael_souza_de_almeida.ticket_mania.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.dto.EventRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.dto.EventResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService{

    private final EventRepository eventRepository;

    public List<EventResponseDto> findAll() {

        return eventRepository.findAll().stream().map(EventResponseDto::new).toList();

    }

    public EventResponseDto create(EventRequestDto dto) {

        Event event = Event.builder()
                .name(dto.getName())
                .capacity(dto.getCapacity())
                .date(dto.getDate())
                .place(dto.getPlace())
                .build();

        eventRepository.save(event);

        return new EventResponseDto(event);

    }

}
