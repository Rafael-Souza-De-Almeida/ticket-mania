package com.github.rafael_souza_de_almeida.ticket_mania.order.controller;

import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.service.OrderPaymentService;
import com.github.rafael_souza_de_almeida.ticket_mania.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Criação de pedidos e simulação de pagamento")
public class OrderController {

    private final OrderService orderService;
    private final OrderPaymentService orderPaymentService;

    @PostMapping
    @Operation(summary = "Criar pedido", description = "Cria um novo pedido com base nas informações enviadas.")
    public ResponseEntity<OrderResponseDto> create(@RequestBody OrderRequestDto requestDto) {

        OrderResponseDto response = orderService.create(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PatchMapping("/mock-success/{orderId}")
    @Operation(summary = "Simular pagamento aprovado", description = "Atualiza o pedido para o cenário de pagamento aprovado para testes.")
    public ResponseEntity<Void> simulateSuccessfulPayment(@PathVariable UUID orderId) {
        orderPaymentService.fulfillOrder(orderId);
        return ResponseEntity.ok().build();
    }

}
