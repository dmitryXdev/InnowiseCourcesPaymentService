package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.dao.PaymentRepository;
import com.innowise.paymentservice.exception.EntityNotFoundException;
import com.innowise.paymentservice.kafka.event.PaymentEvent;
import com.innowise.paymentservice.kafka.producer.PaymentProducer;
import com.innowise.paymentservice.model.Payment;
import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.service.PaymentAsyncService;
import com.innowise.paymentservice.service.RandomOrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentAsyncServiceImpl implements PaymentAsyncService {
    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;
    private final RandomOrgService randomOrgService;

    private static final String ENTITY_NOT_FOUND_MESSAGE = "Entity not found";

    /**
     * Processes payment.
     * @param id
     */
    @Async
    public void processPaymentAsync(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE));

        if(randomOrgService.tryToPay()) {
            payment.setStatus(PaymentStatus.SUCCESS);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);

        paymentProducer.send(PaymentEvent.builder()
                .orderId(payment.getOrderId())
                .status(payment.getStatus().name())
                .build());
    }
}
