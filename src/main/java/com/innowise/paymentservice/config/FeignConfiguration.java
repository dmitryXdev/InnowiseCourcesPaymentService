package com.innowise.paymentservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients("com.innowise.paymentservice.feign")
public class FeignConfiguration {
    @Value("${internal.secret.header}")
    private String internalSecretHeader;
    @Value("${internal.secret}")
    private String secret;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> requestTemplate.header(internalSecretHeader, secret);
    }
}
