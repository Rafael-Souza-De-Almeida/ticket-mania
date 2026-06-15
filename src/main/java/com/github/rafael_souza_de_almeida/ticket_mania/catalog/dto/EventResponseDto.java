package com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import lombok.Getter;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Getter
public class EventResponseDto {

    private String name;
    private LocalDateTime date;
    private String place;
    private Integer capacity;

    public EventResponseDto(Event event) {
        this.name = event.getName();
        this.date = event.getDate();
        this.place = event.getPlace();
        this.capacity = event.getCapacity();
    }

}
