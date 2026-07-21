package com.ilkinalizade.bookingpaymentsapi.repository;

import com.ilkinalizade.bookingpaymentsapi.entity.Booking;
import com.ilkinalizade.bookingpaymentsapi.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByUserId(UUID userId);
    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, Instant cutoff);
}