package com.innowise.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDto {
    @NotBlank
    private String id;
    @NotNull
    private Long userId;
    @NotNull
    private Long orderId;
    @NotNull
    private LocalDateTime timestamp;
    @NotNull
    @Positive
    private BigDecimal paymentAmount;
    @NotNull
    private String status;
}
