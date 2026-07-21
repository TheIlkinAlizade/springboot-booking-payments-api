package com.ilkinalizade.bookingpaymentsapi.service;

import com.ilkinalizade.bookingpaymentsapi.entity.*;
import com.ilkinalizade.bookingpaymentsapi.repository.BookingRepository;
import com.ilkinalizade.bookingpaymentsapi.repository.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.stripe.webhook-secret}")
    private String webhookSecret;

    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new IllegalArgumentException("Invalid Stripe webhook signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            handleCheckoutCompleted(event);
        }
        // other event types (payment_failed, session expired, etc.)
    }

    private void handleCheckoutCompleted(Event event) {
        String sessionId = extractSessionId(event);

        Session session;
        try {
            session = Session.retrieve(sessionId);
        } catch (StripeException e) {
            throw new IllegalStateException("Could not retrieve Stripe session: " + e.getMessage());
        }

        Payment payment = paymentRepository.findByStripeCheckoutSessionId(session.getId())
                .orElseThrow(() -> new NoSuchElementException(
                        "No payment found for Stripe session: " + session.getId()));

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            return;
        }

        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setStripePaymentIntentId(session.getPaymentIntent());
        paymentRepository.save(payment);

        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setConfirmedAt(Instant.now());
        bookingRepository.save(booking);
    }

    private String extractSessionId(Event event) {
        JsonNode root = objectMapper.readTree(event.toJson());
        return root.path("data").path("object").path("id").asText();
    }
}