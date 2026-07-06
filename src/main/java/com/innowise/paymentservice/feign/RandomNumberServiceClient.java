package com.innowise.paymentservice.feign;

import com.innowise.paymentservice.dto.randomorg.AnswerDto;
import com.innowise.paymentservice.dto.randomorg.RequestDto;
import com.innowise.paymentservice.feign.fallbackfactory.RandomNumberServiceClientFallbackFactory;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "random-number-service",
        url = "${service.random.url}",
        fallbackFactory = RandomNumberServiceClientFallbackFactory.class)
public interface RandomNumberServiceClient {
    @PostMapping
    AnswerDto getRandomNumber(@RequestBody @Valid RequestDto requestDto);
}
