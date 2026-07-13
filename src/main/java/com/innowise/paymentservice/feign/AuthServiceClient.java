package com.innowise.paymentservice.feign;

import com.innowise.paymentservice.config.FeignConfiguration;
import com.innowise.paymentservice.feign.fallbackfactory.AuthServiceClientFallbackFactory;
import jakarta.validation.Valid;
import com.innowise.paymentservice.dto.TokenValidationRequestDto;
import com.innowise.paymentservice.dto.TokenValidationResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service",
        url = "${services.auth.url}",
        configuration = FeignConfiguration.class,
        fallbackFactory = AuthServiceClientFallbackFactory.class)
public interface AuthServiceClient {
    @PostMapping("/auth-service/auth/validate")
    TokenValidationResponseDto validate(@RequestBody @Valid TokenValidationRequestDto dto);
}
