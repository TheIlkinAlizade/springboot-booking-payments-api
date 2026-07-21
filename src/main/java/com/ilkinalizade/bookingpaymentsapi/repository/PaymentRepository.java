package com.ilkinalizade.bookingpaymentsapi.repository;

import com.ilkinalizade.bookingpaymentsapi.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByStripeCheckoutSessionId(String sessionId);
    Optional<Payment> findByBookingId(UUID bookingId);
}