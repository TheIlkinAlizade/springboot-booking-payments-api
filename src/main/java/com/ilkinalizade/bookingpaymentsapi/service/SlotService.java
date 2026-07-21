package com.ilkinalizade.bookingpaymentsapi.service;

import com.ilkinalizade.bookingpaymentsapi.dto.request.CreateSlotRequest;
import com.ilkinalizade.bookingpaymentsapi.dto.response.SlotResponse;
import com.ilkinalizade.bookingpaymentsapi.entity.Slot;
import com.ilkinalizade.bookingpaymentsapi.entity.SlotStatus;
import com.ilkinalizade.bookingpaymentsapi.entity.User;
import com.ilkinalizade.bookingpaymentsapi.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SlotService {

    private final SlotRepository slotRepository;

    public SlotResponse createSlot(CreateSlotRequest request, User admin) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }

        Slot slot = Slot.builder()
                .title(request.getTitle())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .status(SlotStatus.AVAILABLE)
                .createdBy(admin)
                .build();

        slotRepository.save(slot);

        return toResponse(slot);
    }

    public List<SlotResponse> getAvailableSlots() {
        return slotRepository.findByStatus(SlotStatus.AVAILABLE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SlotResponse getSlotById(UUID id) {
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Slot not found: " + id));
        return toResponse(slot);
    }

    public void cancelSlot(UUID id) {
        // TODO: implement Stripe refunds and trigger a refund before cancelling
        Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Slot not found: " + id));

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new IllegalStateException("Cannot cancel a slot that is already booked");
        }

        slot.setStatus(SlotStatus.CANCELLED);
        slotRepository.save(slot);
    }

    private SlotResponse toResponse(Slot slot) {
        return SlotResponse.builder()
                .id(slot.getId())
                .title(slot.getTitle())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .price(slot.getPrice())
                .currency(slot.getCurrency())
                .status(slot.getStatus().name())
                .build();
    }
}