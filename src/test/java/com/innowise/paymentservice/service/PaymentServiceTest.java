package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dao.PaymentRepository;
import com.innowise.paymentservice.dto.CreatePaymentDto;
import com.innowise.paymentservice.dto.PaymentDto;
import com.innowise.paymentservice.dto.randomorg.AnswerDto;
import com.innowise.paymentservice.dto.randomorg.AnswerResultDto;
import com.innowise.paymentservice.dto.randomorg.AnswerResultRandomDto;
import com.innowise.paymentservice.exception.AccessDeniedException;
import com.innowise.paymentservice.exception.BadIncomingDataException;
import com.innowise.paymentservice.feign.RandomNumberServiceClient;
import com.innowise.paymentservice.kafka.producer.PaymentProducer;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.Payment;
import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.TotalResult;
import com.innowise.paymentservice.security.UserPrincipal;
import com.innowise.paymentservice.service.impl.PaymentAsyncServiceImpl;
import com.innowise.paymentservice.service.impl.PaymentServiceImpl;
import com.innowise.paymentservice.service.impl.RandomOrgServiceImpl;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private RandomNumberServiceClient randomNumberServiceClient;

    @Mock
    private PaymentProducer paymentProducer;

    private PaymentMapper paymentMapper = Mappers.getMapper(PaymentMapper.class);

    @InjectMocks
    private RandomOrgServiceImpl randomOrgService;

    private PaymentServiceImpl paymentService;

    private PaymentAsyncServiceImpl paymentAsyncService;

    @BeforeEach
    void setUp() {
        paymentAsyncService = new PaymentAsyncServiceImpl(paymentRepository, paymentProducer, randomOrgService);

        paymentService =
                new PaymentServiceImpl(paymentRepository, mongoTemplate, paymentMapper, randomOrgService, paymentProducer, paymentAsyncService);
    }

    private Payment generatePayment(Long userId, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setUserId(userId);
        payment.setPaymentAmount(amount);
        payment.setOrderId(0L);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTimestamp(LocalDateTime.now().minusDays(userId));

        return payment;
    }

    private AnswerDto generateRandomOrgAnswer() {
        AnswerDto answerDto = new AnswerDto();
        AnswerResultDto resultDto = new AnswerResultDto();
        AnswerResultRandomDto randomDto = new AnswerResultRandomDto();
        randomDto.setData(List.of(112));

        resultDto.setRandom(randomDto);
        answerDto.setResult(resultDto);

        return answerDto;
    }
    @Test
    void createPayment_shouldSavePaymentAndMockRandom() {
        Payment payment = generatePayment(0L, new BigDecimal(111));

        when(paymentRepository.save(any())).thenReturn(payment);
        when(paymentRepository.findById(any())).thenReturn(Optional.of(payment));
        when(randomNumberServiceClient.getRandomNumber(any())).thenReturn(generateRandomOrgAnswer());

        CreatePaymentDto createPaymentDto = new CreatePaymentDto();
        createPaymentDto.setPaymentAmount(new BigDecimal(111));
        createPaymentDto.setOrderId(1L);

        UserPrincipal principal = new UserPrincipal(0L, "ADMIN");

        PaymentDto paymentDto = paymentService.createPayment(createPaymentDto, principal);

        assertNotNull(paymentDto);
        assertEquals(PaymentStatus.PENDING.name(), paymentDto.getStatus());
        assertEquals(paymentDto.getUserId(), paymentDto.getUserId());

        verify(paymentProducer, times(1)).send(any());
        verify(paymentRepository, times(2)).save(any());
        verify(paymentRepository, times(1)).findById(any());
    }

    @Test
    void getPaymentById_shouldReturnPaymentById() {
        Payment payment = generatePayment(0L, new BigDecimal(111));

        when(paymentRepository.findById(any())).thenReturn(Optional.of(payment));

        UserPrincipal principal = new UserPrincipal(0L, "ADMIN");

        PaymentDto paymentDto = paymentService.getPaymentById("id", principal);

        assertNotNull(paymentDto);
        assertEquals(principal.getId(), paymentDto.getUserId());
    }

    @Test
    void getPaymentById_shouldThrowExceptionOnNotAccessibleResource() {
        Payment payment = generatePayment(0L, new BigDecimal(111));

        when(paymentRepository.findById(any())).thenReturn(Optional.of(payment));

        UserPrincipal principal = new UserPrincipal(1L, "USER");

        assertThrows(AccessDeniedException.class, () -> paymentService.getPaymentById("id", principal));
    }

    @Test
    void getSummary_shouldReturnSummaryOfAllOperations() {
        TotalResult totalResult = new TotalResult();
        totalResult.setTotal(new BigDecimal(111));

        AggregationResults<Object> total =
                new AggregationResults<>(new ArrayList<>(List.of(totalResult)), new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), any(String.class), any())).thenReturn(total);

        BigDecimal result = paymentService.getSummary(LocalDate.now().minusDays(1L), LocalDate.now());

        assertNotNull(result);
        assertEquals(totalResult.getTotal(), result);
    }

    @Test
    void getSummary_shouldReturnSummaryOfAllOperationsByUserIdAndDateRange() {
        TotalResult totalResult = new TotalResult();
        totalResult.setTotal(new BigDecimal(111));

        AggregationResults<Object> total =
                new AggregationResults<>(new ArrayList<>(List.of(totalResult)), new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), any(String.class), any())).thenReturn(total);

        BigDecimal result = paymentService.getSummary(0L, LocalDate.now().minusDays(1L), LocalDate.now());

        assertNotNull(result);
        assertEquals(totalResult.getTotal(), result);
    }

    @Test
    void getSummary_shouldThrowExceptionOnMissingIncomeData() {
        assertThrows(BadIncomingDataException.class, () -> paymentService.getSummary(null, LocalDate.now().minusDays(1L), LocalDate.now()));
    }

    @Test
    void findAllByFields_shouldReturnAllPaymentsByFilter() {
        UserPrincipal principal = new UserPrincipal(0L, "USER");

        List<Object> payments = List.of(generatePayment(0L, new BigDecimal(111)),
                generatePayment(1L, new BigDecimal(123)));

        when(mongoTemplate.find(any(), any())).thenReturn(payments);
        when(mongoTemplate.count(any(), any(Class.class))).thenReturn(2L);

        Page<PaymentDto> page = paymentService
                .findAllByFields(
                        0L,
                        0L,
                        LocalDate.now().minusDays(1L),
                        LocalDate.now(), PaymentStatus.SUCCESS.name(),
                        principal,
                        0,
                        10,
                        "orderId");

        assertNotNull(page);
        assertEquals(2L, page.getTotalElements());
        assertEquals(payments.stream().map(o -> paymentMapper.toDto((Payment) o)).toList(), page.getContent());
    }
}
