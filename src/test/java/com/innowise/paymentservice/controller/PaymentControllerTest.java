package com.innowise.paymentservice.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.paymentservice.config.TestConfig;
import com.innowise.paymentservice.dao.PaymentRepository;
import com.innowise.paymentservice.dto.CreatePaymentDto;
import com.innowise.paymentservice.dto.PageResponse;
import com.innowise.paymentservice.dto.PaymentDto;
import com.innowise.paymentservice.dto.TokenValidationResponseDto;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.Payment;
import com.innowise.paymentservice.model.PaymentStatus;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Import(TestConfig.class)
@AutoConfigureMockMvc
@EnableWireMock(
        @ConfigureWireMock(name = "auth-service", port = 8081)
)
class PaymentControllerTest {
    @InjectWireMock("auth-service")
    private WireMockServer authService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private final String TOKEN = "Bearer token";

    @AfterEach
    void clearDb() {
        paymentRepository.deleteAll();
    }

    private void authenticateAs(String role) {
        TokenValidationResponseDto response = TokenValidationResponseDto.builder()
                .valid(true)
                .role(role)
                .userId(0L)
                .build();

        authService.stubFor(WireMock.post(WireMock.urlEqualTo("/auth/validate"))
                .willReturn(okJson(objectMapper.writeValueAsString(response))));
    }

    private void errorAuth(boolean throwException, ResponseDefinitionBuilder builder) {
        if(throwException) {
            authService.stubFor(WireMock.post(WireMock.urlEqualTo("/auth/validate"))
                    .willReturn(builder));
        } else {
            TokenValidationResponseDto response = TokenValidationResponseDto.builder()
                    .valid(false)
                    .role(null)
                    .userId(null)
                    .build();

            authService.stubFor(WireMock.post(WireMock.urlEqualTo("/auth/validate"))
                    .willReturn(okJson(objectMapper.writeValueAsString(response))));
        }
    }

    private void fillDbWithPayments(int amount, Long userId, String status) {
        PaymentStatus[] values = PaymentStatus.values();

        for(int i = 0; i < amount; i++) {
            Payment payment = new Payment();

            if(status == null) {
                payment.setStatus(values[(int) (Math.random() * values.length)]);
            } else {
                payment.setStatus(PaymentStatus.valueOf(status));
            }

            if(userId == null) {
                payment.setUserId((long)i);
            } else {
                payment.setUserId(userId);
            }

            payment.setOrderId((long)i);
            payment.setTimestamp(LocalDateTime.now().minusDays((long) (Math.random() * (i + 1))));
            payment.setPaymentAmount(new BigDecimal(i + 100));

            paymentRepository.save(payment);
        }
    }

    @Test
    void createPayment_shouldCreateAndReturnPayment() throws Exception {
        authenticateAs("ADMIN");
        CreatePaymentDto dto = new CreatePaymentDto();
        dto.setPaymentAmount(new BigDecimal(111));
        dto.setOrderId(0L);

        PaymentDto paymentDto = objectMapper.readValue(mockMvc.perform(post("/payments")
                .header(HttpHeaders.AUTHORIZATION, TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString(), PaymentDto.class);

        assertNotNull(paymentDto);
        assertEquals(PaymentStatus.PENDING.name(), paymentDto.getStatus());
    }

    @Test
    void createPayment_shouldThrowExceptionOnNotBearerToken() throws Exception {
        mockMvc.perform(post("/payments")
                .header(HttpHeaders.AUTHORIZATION, "token")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(""))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPayment_shouldThrowExceptionOnAuthServiceNotValidToken() throws Exception {
        errorAuth(false, null);

        mockMvc.perform(post("/payments")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(""))
                .andExpect(status().isForbidden())
                .andReturn().getResponse();
    }

    @Test
    void getPaymentById_shouldReturnPaymentById() throws Exception {
        authenticateAs("ADMIN");
        fillDbWithPayments(15, null, null);

        Payment payment = paymentRepository.findAll().get(0);

        PaymentDto paymentDto = objectMapper.readValue(mockMvc.perform(get("/payments/" + payment.getId())
                .header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), PaymentDto.class);

        assertNotNull(paymentDto);
        assertEquals(paymentMapper.toDto(payment), paymentDto);
    }

    @Test
    void getPaymentById_shouldThrowExceptionOnForbiddenResource() throws Exception {
        authenticateAs("USER");
        fillDbWithPayments(3, null, null);

        Payment payment = paymentRepository.findAll().get(2);

        mockMvc.perform(get("/payments/" + payment.getId())
                        .header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPaymentById_shouldThrowExceptionOnNotFoundResource() throws Exception {
        authenticateAs("USER");

        mockMvc.perform(get("/payments/not-exists")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllByFilter_shouldReturnAllIfAdmin() throws Exception {
        authenticateAs("ADMIN");
        fillDbWithPayments(15, null, null);

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(PageResponse.class, PaymentDto.class);

        PageResponse<PaymentDto> page = objectMapper.readValue(mockMvc.perform(get("/payments")
                .header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), javaType);

        assertNotNull(page);
        assertEquals(10, page.getContent().size());
    }

    @Test
    void getAllByFilter_shouldReturnAllByFilter() throws Exception {
        authenticateAs("ADMIN");

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);
        payment.setUserId(0L);
        payment.setOrderId(1L);
        payment.setTimestamp(LocalDateTime.now().minusDays(1L));

        paymentRepository.save(payment);

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(PageResponse.class, PaymentDto.class);

        PageResponse<PaymentDto> page = objectMapper.readValue(mockMvc.perform(get("/payments")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .param("userId", "0")
                        .param("orderId", "1")
                        .param("status", "PENDING")
                        .param("from", LocalDate.now().minusDays(2L).toString())
                        .param("to", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), javaType);

        PaymentDto paymentDto = page.getContent().get(0);

        assertFalse(page.getContent().isEmpty());
        assertEquals(payment.getId(), paymentDto.getId());
        assertEquals(0L, paymentDto.getUserId());
        assertEquals(1L, paymentDto.getOrderId());
        assertEquals(PaymentStatus.PENDING.name(), paymentDto.getStatus());
    }

    @Test
    void getAllByFilter_shouldReturnAllByUserIdForUser() throws Exception {
        authenticateAs("USER");

        fillDbWithPayments(5, null, null);

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(PageResponse.class, PaymentDto.class);

        PageResponse<PaymentDto> page = objectMapper.readValue(mockMvc.perform(get("/payments")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), javaType);

        assertEquals(1, page.getContent().size());
        assertEquals(0L, page.getContent().get(0).getUserId());
    }

    @Test
    void getSummary_shouldReturnSummaryByUserId() throws Exception {
        authenticateAs("USER");

        fillDbWithPayments(5, 0L, PaymentStatus.SUCCESS.name());

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(PageResponse.class, PaymentDto.class);

        PageResponse<PaymentDto> page = objectMapper.readValue(mockMvc.perform(get("/payments")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .param("from", LocalDate.now().minusDays(10L).toString())
                        .param("to", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), javaType);

        BigDecimal total = objectMapper.readValue(mockMvc.perform(get("/payments/users/0/summary")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .param("from", LocalDate.now().minusDays(10L).toString())
                        .param("to", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), BigDecimal.class);

        BigDecimal expectedTotal = (page.getContent().stream().map(PaymentDto::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        assertEquals(expectedTotal, total);
    }

    @Test
    void getSummary_shouldThrowExceptionOnBadIncomingData() throws Exception {
        authenticateAs("USER");

        mockMvc.perform(get("/payments/users/0/summary")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .param("from", "")
                        .param("to", LocalDate.now().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSummary_shouldThrowExceptionOnArgumentTypeMismatch() throws Exception {
        authenticateAs("USER");

        mockMvc.perform(get("/payments/users/0/summary")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .param("from", "null")
                        .param("to", LocalDate.now().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSummary_shouldThrowExceptionOnAuthServiceFall() throws Exception {
        errorAuth(true, serverError());

        mockMvc.perform(get("/payments/users/0/summary")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .param("from", "")
                        .param("to", LocalDate.now().toString()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getSummary_shouldThrowExceptionOnAuthServiceUnavailable() throws Exception {
        errorAuth(true, WireMock.serviceUnavailable());

        mockMvc.perform(get("/payments/users/0/summary")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .param("from", "")
                        .param("to", LocalDate.now().toString()))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void getSummary_shouldThrowExceptionOnMissingArgument() throws Exception {
        authenticateAs("USER");

        mockMvc.perform(get("/payments/users/0/summary")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSummary_shouldReturnGeneralSummaryIfAdmin() throws Exception {
        authenticateAs("ADMIN");
        fillDbWithPayments(15, null, PaymentStatus.SUCCESS.name());

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(PageResponse.class, PaymentDto.class);

        PageResponse<PaymentDto> page = objectMapper.readValue(mockMvc.perform(get("/payments")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                                .param("size", "15")
                        .param("from", LocalDate.now().minusDays(10L).toString())
                        .param("to", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), javaType);

        BigDecimal total = objectMapper.readValue(mockMvc.perform(get("/payments/summary")
                        .header(HttpHeaders.AUTHORIZATION, TOKEN)
                        .param("from", LocalDate.now().minusDays(10L).toString())
                        .param("to", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), BigDecimal.class);

        BigDecimal expectedTotal = (page.getContent().stream().map(PaymentDto::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        assertEquals(expectedTotal, total);
    }
}
