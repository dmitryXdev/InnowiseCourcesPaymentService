package com.innowise.paymentservice.dao;

import com.innowise.paymentservice.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRepository extends MongoRepository<Payment, String> {
}
