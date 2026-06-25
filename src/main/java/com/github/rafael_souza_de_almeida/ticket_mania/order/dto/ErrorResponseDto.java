package com.github.rafael_souza_de_almeida.ticket_mania.order.dto;

import java.time.LocalDateTime;

public record ErrorResponseDto(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {
}
