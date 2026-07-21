package com.ilkinalizade.bookingpaymentsapi.controller;

import com.ilkinalizade.bookingpaymentsapi.dto.request.CreateSlotRequest;
import com.ilkinalizade.bookingpaymentsapi.dto.response.SlotResponse;
import com.ilkinalizade.bookingpaymentsapi.entity.User;
import com.ilkinalizade.bookingpaymentsapi.repository.UserRepository;
import com.ilkinalizade.bookingpaymentsapi.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotService slotService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<SlotResponse>> getAvailableSlots() {
        return ResponseEntity.ok(slotService.getAvailableSlots());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SlotResponse> getSlotById(@PathVariable UUID id) {
        return ResponseEntity.ok(slotService.getSlotById(id));
    }

    @PostMapping
    public ResponseEntity<SlotResponse> createSlot(
            @Valid @RequestBody CreateSlotRequest request,
            Authentication authentication
    ) {
        User admin = currentUser(authentication);
        return ResponseEntity.ok(slotService.createSlot(request, admin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelSlot(@PathVariable UUID id) {
        slotService.cancelSlot(id);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found: " + email));
    }
}