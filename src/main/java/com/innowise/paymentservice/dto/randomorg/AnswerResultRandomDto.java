package com.innowise.paymentservice.dto.randomorg;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class AnswerResultRandomDto {
    private List<Integer> data;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ssX")
    private Instant completionTime;
}
