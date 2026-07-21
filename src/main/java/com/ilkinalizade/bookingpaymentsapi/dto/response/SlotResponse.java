package com.ilkinalizade.bookingpaymentsapi.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotResponse {
    private UUID id;
    private String title;
    private Instant startTime;
    private Instant endTime;
    private BigDecimal price;
    private String currency;
    private String status;
}