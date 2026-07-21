package com.ilkinalizade.bookingpaymentsapi.service;

import com.ilkinalizade.bookingpaymentsapi.entity.Booking;
import com.ilkinalizade.bookingpaymentsapi.entity.BookingStatus;
import com.ilkinalizade.bookingpaymentsapi.entity.SlotStatus;
import com.ilkinalizade.bookingpaymentsapi.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingExpiryService {

    private final BookingRepository bookingRepository;

    @Value("${app.booking.expiry-minutes}")
    private int expiryMinutes;

    @Scheduled(fixedRateString = "${app.booking.expiry-check-interval-ms}")
    @Transactional
    public void expireStaleBookings() {
        Instant cutoff = Instant.now().minus(expiryMinutes, ChronoUnit.MINUTES);

        List<Booking> staleBookings =
                bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING, cutoff);

        for (Booking booking : staleBookings) {
            booking.setStatus(BookingStatus.EXPIRED);
            booking.getSlot().setStatus(SlotStatus.AVAILABLE);
            log.info("Expired stale booking {} and released slot {}",
                    booking.getId(), booking.getSlot().getId());
        }

        if (!staleBookings.isEmpty()) {
            bookingRepository.saveAll(staleBookings);
        }
    }
}