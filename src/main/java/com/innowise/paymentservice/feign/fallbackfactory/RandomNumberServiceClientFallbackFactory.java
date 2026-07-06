package com.innowise.paymentservice.feign.fallbackfactory;

import com.innowise.paymentservice.feign.RandomNumberServiceClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class RandomNumberServiceClientFallbackFactory implements FallbackFactory<RandomNumberServiceClient> {
    @Override
    public RandomNumberServiceClient create(Throwable cause) {
        return client -> {
            if(cause instanceof RuntimeException exception) {
                throw exception;
            }

            throw new RuntimeException(cause);
        };
    }
}
