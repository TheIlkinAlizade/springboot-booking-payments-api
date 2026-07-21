package com.ilkinalizade.bookingpaymentsapi.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private UUID id;
    private UUID slotId;
    private String slotTitle;
    private String status;
    private Instant createdAt;
    private String checkoutUrl;
}