package com.innowise.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentDto {
    @NotNull
    private Long orderId;
    @NotNull
    private BigDecimal paymentAmount;
}
