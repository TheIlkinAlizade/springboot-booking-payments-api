package com.ilkinalizade.bookingpaymentsapi.controller;

import com.ilkinalizade.bookingpaymentsapi.dto.request.CreateBookingRequest;
import com.ilkinalizade.bookingpaymentsapi.dto.response.BookingResponse;
import com.ilkinalizade.bookingpaymentsapi.entity.User;
import com.ilkinalizade.bookingpaymentsapi.repository.UserRepository;
import com.ilkinalizade.bookingpaymentsapi.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication
    ) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(bookingService.createBooking(request, user));
    }

    @GetMapping("/me")
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication authentication) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(bookingService.getMyBookings(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(bookingService.getBookingById(id, user));
    }

    private User currentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Authenticated user not found: " + email));
    }
}