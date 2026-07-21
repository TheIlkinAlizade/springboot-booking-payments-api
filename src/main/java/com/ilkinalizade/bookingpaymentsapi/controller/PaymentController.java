package com.ilkinalizade.bookingpaymentsapi.controller;

import com.ilkinalizade.bookingpaymentsapi.dto.response.PaymentResponse;
import com.ilkinalizade.bookingpaymentsapi.entity.User;
import com.ilkinalizade.bookingpaymentsapi.repository.UserRepository;
import com.ilkinalizade.bookingpaymentsapi.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    @GetMapping("/{bookingId}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(
            @PathVariable UUID bookingId,
            Authentication authentication
    ) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(bookingService.getPaymentStatus(bookingId, user));
    }

    private User currentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found: " + email));
    }
}