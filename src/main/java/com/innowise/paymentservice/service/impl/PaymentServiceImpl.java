package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.dao.PaymentRepository;
import com.innowise.paymentservice.dto.CreatePaymentDto;
import com.innowise.paymentservice.dto.PaymentDto;
import com.innowise.paymentservice.exception.AccessDeniedException;
import com.innowise.paymentservice.exception.BadIncomingDataException;
import com.innowise.paymentservice.exception.EntityNotFoundException;
import com.innowise.paymentservice.kafka.producer.PaymentProducer;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.Payment;
import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.TotalResult;
import com.innowise.paymentservice.query.PaymentQueryTuner;
import com.innowise.paymentservice.security.UserPrincipal;
import com.innowise.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final MongoTemplate mongoTemplate;
    private final PaymentMapper paymentMapper;
    private final RandomOrgServiceImpl randomOrgService;
    private final PaymentProducer paymentProducer;
    private final PaymentAsyncServiceImpl paymentAsyncServiceImpl;

    private static final String ENTITY_NOT_FOUND_MESSAGE = "Entity not found";
    private static final String INCORRECT_INCOME_DATA = "Incorrect income data";
    private static final String FORBIDDEN_MESSAGE = "Forbidden";


    @Override
    public PaymentDto createPayment(CreatePaymentDto createPaymentDto, UserPrincipal principal) {
        if(createPaymentDto == null) {
            throw new BadIncomingDataException(INCORRECT_INCOME_DATA);
        }

        Payment payment = new Payment();
        payment.setPaymentAmount(createPaymentDto.getPaymentAmount());
        payment.setOrderId(createPaymentDto.getOrderId());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setUserId(principal.getId());

        paymentRepository.save(payment);

        paymentAsyncServiceImpl.processPaymentAsync(payment.getId());

        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentDto getPaymentById(String id, UserPrincipal principal) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE));

        if(!payment.getUserId().equals(principal.getId()) && !principal.getRole().equals("ADMIN")) {
            throw new AccessDeniedException(FORBIDDEN_MESSAGE);
        }

        return paymentMapper.toDto(payment);
    }

    private BigDecimal getSummary(Aggregation aggregation) {
        AggregationResults<TotalResult> total = mongoTemplate
                .aggregate(aggregation, "payments", TotalResult.class);

        if(total.getUniqueMappedResult() == null) {
            return BigDecimal.ZERO;
        }

        return total.getUniqueMappedResult().getTotal();
    }

    @Override
    public BigDecimal getSummary(LocalDate from, LocalDate to) {
        Criteria criteria = Criteria.where("payment_status").is(PaymentStatus.SUCCESS);

        if(from != null && to != null) {
            criteria.and("timestamp").gte(from.atStartOfDay()).lte(to.plusDays(1L).atStartOfDay());
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group()
                        .sum("payment_amount").as("total")
        );

        return getSummary(aggregation);
    }

    @Override
    public BigDecimal getSummary(Long userId, LocalDate from, LocalDate to) {
        if(userId == null || from == null || to == null) {
            throw new BadIncomingDataException(INCORRECT_INCOME_DATA);
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("payment_status").is(PaymentStatus.SUCCESS)
                        .and("user_id").is(userId).and("timestamp").gte(from.atStartOfDay())
                        .lte(to.plusDays(1L).atStartOfDay())),
                Aggregation.group()
                        .sum("payment_amount").as("total")
        );

        return getSummary(aggregation);
    }

    @Override
    public Page<PaymentDto> findAllByFields(Long userId, Long orderId, LocalDate from, LocalDate to, String status, UserPrincipal principal, int page, int size, String sortBy) {
        if(!principal.getRole().equals("ADMIN")) {
            userId = principal.getId();
        }

        Query query = new Query();

        PaymentQueryTuner.addCriteriaForField(query, "user_id", userId);

        PaymentQueryTuner.addCriteriaForField(query, "order_id", orderId);

        if(status != null && !status.isBlank()) {
            PaymentQueryTuner.addCriteriaForField(query, "payment_status", PaymentStatus.valueOf(status.toUpperCase(Locale.ROOT)));
        }

        if(from != null && to != null) {
            PaymentQueryTuner.addCriteriaForFieldBetween(query, "timestamp", from.atStartOfDay(), to.plusDays(1L).atStartOfDay());
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));

        query.with(pageable);

        List<Payment> payments = mongoTemplate.find(query, Payment.class);

        return new PageImpl<>(
                payments.stream().map(paymentMapper::toDto).toList(),
                pageable,
                mongoTemplate.count(query, Payment.class)
        );
    }
}
