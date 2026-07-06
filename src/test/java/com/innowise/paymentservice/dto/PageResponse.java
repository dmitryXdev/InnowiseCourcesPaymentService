package com.innowise.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
}
