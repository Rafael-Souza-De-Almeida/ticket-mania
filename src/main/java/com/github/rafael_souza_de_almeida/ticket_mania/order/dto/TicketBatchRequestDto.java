package com.github.rafael_souza_de_almeida.ticket_mania.order.dto;

import java.math.BigDecimal;

public record TicketBatchRequestDto(
        int quantity,
        String sectorCode,
        BigDecimal price
) {
}
