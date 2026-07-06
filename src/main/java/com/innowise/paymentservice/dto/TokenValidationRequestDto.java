package com.innowise.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenValidationRequestDto {
    @NotNull
    @NotBlank
    private String token;
}
