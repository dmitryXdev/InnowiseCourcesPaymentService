package com.innowise.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentDto {
    @NotNull
    private Long orderId;
    @NotNull
    @Positive
    private BigDecimal paymentAmount;
}
