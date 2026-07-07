package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.repository.OrderRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentWebhookService paymentWebhookService;

    private static final String PAYLOAD = "{\"fake\":\"payload\"}";
    private static final String SIGNATURE_HEADER = "t=123,v1=abc";
    private static final String WEBHOOK_SECRET = "whsec_fake";

    private MockedStatic<Webhook> webhookMockedStatic;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentWebhookService, "webhookSecret", WEBHOOK_SECRET);
        webhookMockedStatic = mockStatic(Webhook.class);
    }

    @AfterEach
    void tearDown() {
        webhookMockedStatic.close();
    }

    @Test
    @DisplayName("processWebhookEvent - should mark the order as COMPLETED when payment_intent.succeeded is received")
    void shouldCompleteOrderOnPaymentSucceeded() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).userId(UUID.randomUUID()).status(OrderStatus.PENDING).build();

        Event event = mockEventOfType("payment_intent.succeeded", orderId.toString());

        webhookMockedStatic.when(() -> Webhook.constructEvent(PAYLOAD, SIGNATURE_HEADER, WEBHOOK_SECRET))
                .thenReturn(event);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        paymentWebhookService.processWebhookEvent(PAYLOAD, SIGNATURE_HEADER);

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    @DisplayName("processWebhookEvent - should ignore events other than payment_intent.succeeded")
    void shouldIgnoreOtherEventTypes() {
        Event event = org.mockito.Mockito.mock(Event.class);
        when(event.getType()).thenReturn("payment_intent.created");

        webhookMockedStatic.when(() -> Webhook.constructEvent(PAYLOAD, SIGNATURE_HEADER, WEBHOOK_SECRET))
                .thenReturn(event);

        paymentWebhookService.processWebhookEvent(PAYLOAD, SIGNATURE_HEADER);

        verify(orderRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("processWebhookEvent - should throw IllegalArgumentException when signature is invalid")
    void shouldThrowWhenSignatureIsInvalid() {
        webhookMockedStatic.when(() -> Webhook.constructEvent(PAYLOAD, SIGNATURE_HEADER, WEBHOOK_SECRET))
                .thenThrow(new SignatureVerificationException("invalid signature", "sig_header"));

        assertThrows(IllegalArgumentException.class,
                () -> paymentWebhookService.processWebhookEvent(PAYLOAD, SIGNATURE_HEADER));

        verify(orderRepository, never()).findById(any());
    }

    @Test
    @DisplayName("processWebhookEvent - should do nothing when the PaymentIntent metadata has no orderId")
    void shouldDoNothingWhenMetadataHasNoOrderId() {
        Event event = mockEventOfType("payment_intent.succeeded", null);

        webhookMockedStatic.when(() -> Webhook.constructEvent(PAYLOAD, SIGNATURE_HEADER, WEBHOOK_SECRET))
                .thenReturn(event);

        paymentWebhookService.processWebhookEvent(PAYLOAD, SIGNATURE_HEADER);

        verify(orderRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("processWebhookEvent - should throw IllegalStateException when the order referenced in metadata does not exist")
    void shouldThrowWhenOrderFromMetadataNotFound() {
        UUID orderId = UUID.randomUUID();
        Event event = mockEventOfType("payment_intent.succeeded", orderId.toString());

        webhookMockedStatic.when(() -> Webhook.constructEvent(PAYLOAD, SIGNATURE_HEADER, WEBHOOK_SECRET))
                .thenReturn(event);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> paymentWebhookService.processWebhookEvent(PAYLOAD, SIGNATURE_HEADER));

        verify(orderRepository, never()).save(any());
    }

    private Event mockEventOfType(String type, String orderIdMetadata) {
        Event event = org.mockito.Mockito.mock(Event.class);
        when(event.getType()).thenReturn(type);

        PaymentIntent paymentIntent = org.mockito.Mockito.mock(PaymentIntent.class);
        Map<String, String> metadata = new HashMap<>();
        if (orderIdMetadata != null) {
            metadata.put("orderId", orderIdMetadata);
        }
        when(paymentIntent.getMetadata()).thenReturn(metadata);
        if ("payment_intent.succeeded".equals(type) && orderIdMetadata == null) {
            when(paymentIntent.getId()).thenReturn("pi_fake_123");
        }

        EventDataObjectDeserializer deserializer = org.mockito.Mockito.mock(EventDataObjectDeserializer.class);
        when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);

        return event;
    }
}
