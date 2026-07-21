package com.ilkinalizade.bookingpaymentsapi.repository;

import com.ilkinalizade.bookingpaymentsapi.entity.Slot;
import com.ilkinalizade.bookingpaymentsapi.entity.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SlotRepository extends JpaRepository<Slot, UUID> {
    List<Slot> findByStatus(SlotStatus status);
    List<Slot> findByStatusAndStartTimeBetween(SlotStatus status, Instant from, Instant to);
}