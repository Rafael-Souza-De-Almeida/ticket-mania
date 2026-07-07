package com.github.rafael_souza_de_almeida.ticket_mania.order.service;

import com.github.rafael_souza_de_almeida.ticket_mania.catalog.domain.Event;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Order;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.OrderStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private PaymentService paymentService;
    private MockedStatic<PaymentIntent> paymentIntentMockedStatic;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
        ReflectionTestUtils.setField(paymentService, "stripeApiKey", "sk_test_fake_key");
        paymentIntentMockedStatic = mockStatic(PaymentIntent.class);
    }

    @AfterEach
    void tearDown() {
        paymentIntentMockedStatic.close();
    }

    @Test
    @DisplayName("createPaymentIntent - should convert the ticket price to cents and return the Stripe client secret")
    void shouldCreatePaymentIntentWithAmountInCents() throws StripeException {
        Event event = Event.builder().id(UUID.randomUUID()).name("Show").place("Arena").capacity(500).build();

        Ticket ticket = Ticket.builder()
                .id(UUID.randomUUID())
                .event(event)
                .sectorCode("A_1")
                .price(new BigDecimal("99.90"))
                .type(TicketType.FULL)
                .status(TicketStatus.RESERVED)
                .build();

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .ticket(ticket)
                .status(OrderStatus.PENDING)
                .build();

        PaymentIntent fakeIntent = Mockito.mock(PaymentIntent.class);
        when(fakeIntent.getClientSecret()).thenReturn("secret_abc123");

        ArgumentCaptor<PaymentIntentCreateParams> paramsCaptor = ArgumentCaptor.forClass(PaymentIntentCreateParams.class);
        paymentIntentMockedStatic.when(() -> PaymentIntent.create(paramsCaptor.capture())).thenReturn(fakeIntent);

        String clientSecret = paymentService.createPaymentIntent(order);

        assertEquals("secret_abc123", clientSecret);

        PaymentIntentCreateParams capturedParams = paramsCaptor.getValue();
        assertEquals(9990L, capturedParams.getAmount());
        assertEquals("brl", capturedParams.getCurrency());
        assertEquals(order.getId().toString(), capturedParams.getMetadata().get("orderId"));
    }
}
