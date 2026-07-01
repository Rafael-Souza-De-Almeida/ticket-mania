package com.github.rafael_souza_de_almeida.ticket_mania.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderRequestDto(
        @NotNull UUID ticketId
) {
}
