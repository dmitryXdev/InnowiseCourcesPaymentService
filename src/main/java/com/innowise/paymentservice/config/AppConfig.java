package com.innowise.paymentservice.config;

import io.mongock.runner.springboot.EnableMongock;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableMongoAuditing
@EnableAsync
@EnableKafka
@EnableMongock
public class AppConfig {
}
