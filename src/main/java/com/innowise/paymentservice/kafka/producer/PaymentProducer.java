package com.innowise.paymentservice.kafka.producer;

import com.innowise.paymentservice.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProducer {
    private static final String TOPIC = "payment-events";

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void send(PaymentEvent event) {
        kafkaTemplate.send(TOPIC, event.getOrderId().toString(), event);
    }
}
