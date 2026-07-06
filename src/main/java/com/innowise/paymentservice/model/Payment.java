package com.innowise.paymentservice.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "payments")
@Data
public class Payment {
    @Id
    private String id;

    @Field(name = "order_id")
    private Long orderId;

    @Field(name = "user_id")
    private Long userId;

    @Field(name = "timestamp")
    @CreatedDate
    private LocalDateTime timestamp;

    @Field(name = "payment_amount")
    private BigDecimal paymentAmount;

    @Field(name = "payment_status")
    private PaymentStatus status;
}
