package com.github.rafael_souza_de_almeida.ticket_mania.order.dto;

import java.util.UUID;

public record OrderResponseDto(
        UUID orderId,
        UUID ticketId,
        UUID userId
) {
}
