package com.innowise.paymentservice.dto.randomorg;

import lombok.Data;

@Data
public class AnswerDto {
    private String jsonrpc;
    private AnswerResultDto result;
    private Long id;
}
