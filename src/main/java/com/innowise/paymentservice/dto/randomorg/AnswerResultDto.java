package com.innowise.paymentservice.dto.randomorg;

import lombok.Data;

@Data
public class AnswerResultDto {
    private AnswerResultRandomDto random;
    private Integer bitsUsed;
    private Integer bitsLeft;
    private Integer requestsLeft;
    private Integer advisoryDelay;
}
