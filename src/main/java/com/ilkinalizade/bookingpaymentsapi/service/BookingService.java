package com.ilkinalizade.bookingpaymentsapi.service;

import com.ilkinalizade.bookingpaymentsapi.dto.request.CreateBookingRequest;
import com.ilkinalizade.bookingpaymentsapi.dto.response.BookingResponse;
import com.ilkinalizade.bookingpaymentsapi.entity.*;
import com.ilkinalizade.bookingpaymentsapi.repository.BookingRepository;
import com.ilkinalizade.bookingpaymentsapi.repository.PaymentRepository;
import com.ilkinalizade.bookingpaymentsapi.repository.SlotRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final PaymentRepository paymentRepository;

    @Value("${app.stripe.success-url}")
    private String successUrl;

    @Value("${app.stripe.cancel-url}")
    private String cancelUrl;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, User user) {
        Slot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new NoSuchElementException("Slot not found: " + request.getSlotId()));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new IllegalStateException("Slot is not available for booking");
        }

        // Mark slot BOOKED now, at creation time, not after payment succeeds.
        // This prevents two users racing to book the same slot between now and payment.
        slot.setStatus(SlotStatus.BOOKED);

        try {
            slotRepository.saveAndFlush(slot);
        } catch (ObjectOptimisticLockingFailureException e) {
            // Someone else booked this slot in the moment between our read and write.
            throw new IllegalStateException("Slot was just booked by someone else. Please choose another.");
        }

        Booking booking = Booking.builder()
                .user(user)
                .slot(slot)
                .status(BookingStatus.PENDING)
                .build();
        bookingRepository.save(booking);

        Session session = createStripeCheckoutSession(booking, slot);

        Payment payment = Payment.builder()
                .booking(booking)
                .stripeCheckoutSessionId(session.getId())
                .amount(slot.getPrice())
                .currency(slot.getCurrency())
                .status(PaymentStatus.CREATED)
                .build();
        paymentRepository.save(payment);

        return BookingResponse.builder()
                .id(booking.getId())
                .slotId(slot.getId())
                .slotTitle(slot.getTitle())
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt())
                .checkoutUrl(session.getUrl())
                .build();
    }

    private Session createStripeCheckoutSession(Booking booking, Slot slot) {
        try {
            long amountInSmallestUnit = slot.getPrice()
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?bookingId=" + booking.getId())
                    .setCancelUrl(cancelUrl + "?bookingId=" + booking.getId())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(slot.getCurrency().toLowerCase())
                                                    .setUnitAmount(amountInSmallestUnit)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(slot.getTitle())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("bookingId", booking.getId().toString())
                    .build();

            return Session.create(params);
        } catch (StripeException e) {
            throw new IllegalStateException("Failed to create Stripe checkout session: " + e.getMessage());
        }
    }

    public List<BookingResponse> getMyBookings(User user) {
        return bookingRepository.findByUserId(user.getId())
                .stream()
                .map(b -> BookingResponse.builder()
                        .id(b.getId())
                        .slotId(b.getSlot().getId())
                        .slotTitle(b.getSlot().getTitle())
                        .status(b.getStatus().name())
                        .createdAt(b.getCreatedAt())
                        .build())
                .toList();
    }

    public BookingResponse getBookingById(UUID id, User user) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Booking not found: " + id));

        if (!booking.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new IllegalStateException("You do not have access to this booking");
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .slotId(booking.getSlot().getId())
                .slotTitle(booking.getSlot().getTitle())
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}