package com.innowise.paymentservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mongodb.MongoDBContainer;

@TestConfiguration
public class TestConfig {
    @Bean
    @ServiceConnection
    public MongoDBContainer mongoDBContainer() {
        return new MongoDBContainer("mongo:7.0");
    }

    @Bean
    @ServiceConnection
    public KafkaContainer kafkaContainer() {
        return new KafkaContainer("apache/kafka-native:3.8.0");
    }
}
