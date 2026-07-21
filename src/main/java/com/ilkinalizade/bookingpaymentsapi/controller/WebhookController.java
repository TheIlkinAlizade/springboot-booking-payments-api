package com.ilkinalizade.bookingpaymentsapi.controller;

import com.ilkinalizade.bookingpaymentsapi.service.StripeWebhookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final StripeWebhookService stripeWebhookService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) throws IOException {
        String payload = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String sigHeader = request.getHeader("Stripe-Signature");

        stripeWebhookService.handleWebhook(payload, sigHeader);

        return ResponseEntity.ok("Webhook processed");
    }
}