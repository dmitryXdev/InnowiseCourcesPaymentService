package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dto.CreatePaymentDto;
import com.innowise.paymentservice.dto.PaymentDto;
import com.innowise.paymentservice.security.UserPrincipal;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PaymentService {
    PaymentDto getPaymentById(String id, UserPrincipal principal);
    BigDecimal getSummary(LocalDate from, LocalDate to);
    BigDecimal getSummary(Long userId, LocalDate from, LocalDate to);
    PaymentDto createPayment(CreatePaymentDto createPaymentDto, UserPrincipal principal);
    Page<PaymentDto> findAllByFields(Long userId, Long orderId, LocalDate from, LocalDate to, String status, UserPrincipal principal, int page, int size, String sortBy);
}
