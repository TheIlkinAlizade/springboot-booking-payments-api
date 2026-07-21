package com.ilkinalizade.bookingpaymentsapi.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private UUID bookingId;
    private String bookingStatus;
    private String paymentStatus;
    private BigDecimal amount;
    private String currency;
}