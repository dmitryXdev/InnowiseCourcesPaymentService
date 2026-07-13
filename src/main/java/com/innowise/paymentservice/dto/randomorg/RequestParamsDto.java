package com.innowise.paymentservice.dto.randomorg;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestParamsDto {
    @NotBlank
    private String apiKey;
    @NotNull
    private Integer n;
    @NotNull
    private Long min;
    @NotNull
    private Long max;
    @NotNull
    private Boolean replacement;
    @NotNull
    private Integer base;
    private Object pregeneratedRandomization;
}
