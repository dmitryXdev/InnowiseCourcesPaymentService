package com.innowise.paymentservice.dto.randomorg;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestDto {
    @NotBlank
    private String jsonrpc;
    @NotBlank
    private String method;
    @NotNull
    private RequestParamsDto params;
    @NotNull
    private Long id;
}
