package com.github.rafael_souza_de_almeida.ticket_mania.order.dto;

import java.util.UUID;

public record OrderRequestDto(
        UUID ticketId,
        UUID userId
) {
}
