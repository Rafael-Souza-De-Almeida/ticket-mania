package com.github.rafael_souza_de_almeida.ticket_mania.catalog.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Getter
public class EventRequestDto {

    @NotBlank(message = "Event must have a name.")
    private String name;

    @NotNull(message = "Event must have a date.")
    @Future(message = "Event date must be in the future.")
    private LocalDateTime date;

    @NotBlank(message = "Event must have a place.")
    private String place;

    @NotNull(message = "Event must have a capacity value.")
    @Positive(message = "The capacity must be greater than zero.")
    private BigInteger capacity;
}
