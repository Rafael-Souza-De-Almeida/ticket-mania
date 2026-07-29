package com.github.rafael_souza_de_almeida.ticket_mania.order.controller;

import com.github.rafael_souza_de_almeida.ticket_mania.order.service.PaymentWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Pagamentos", description = "Recebimento de eventos de webhook de pagamentos")
public class WebhookController {

    private final PaymentWebhookService paymentWebhookService;

    public WebhookController(PaymentWebhookService paymentWebhookService) {
        this.paymentWebhookService = paymentWebhookService;
    }

    @PostMapping("/webhook")
    @Operation(summary = "Receber webhook do Stripe", description = "Processa eventos enviados pelo Stripe para atualizar o status dos pagamentos.")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signatureHeader) {

        try {
            paymentWebhookService.processWebhookEvent(payload, signatureHeader);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}