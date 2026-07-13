package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.dto.CreatePaymentDto;
import com.innowise.paymentservice.dto.PaymentDto;
import com.innowise.paymentservice.security.UserPrincipal;
import com.innowise.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/payment-service/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    /**
     * Creates and returns new payment.
     * @param dto
     * @param principal
     * @return ResponseEntity<PaymentDto>
     */
    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(@RequestBody @Valid CreatePaymentDto dto,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(paymentService.createPayment(dto, principal));
    }

    /**
     * Returns existing payment by its id
     * @param id
     * @param principal
     * @return ResponseEntity<PaymentDto>
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(@PathVariable String id,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(paymentService.getPaymentById(id, principal));
    }

    /**
     * Return all operations by filter
     * @param userId
     * @param orderId
     * @param status
     * @param principal
     * @param from
     * @param to
     * @param page
     * @param size
     * @param sortBy
     * @return ResponseEntity<Page<PaymentDto>>
     */
    @GetMapping
    public ResponseEntity<Page<PaymentDto>> getAllByFilter(@RequestParam(value = "userId", required = false) Long userId,
                                                           @RequestParam(value = "orderId", required = false) Long orderId,
                                                           @RequestParam(value = "status", required = false) String status,
                                                           @AuthenticationPrincipal UserPrincipal principal,
                                                           @RequestParam(value = "from", required = false) LocalDate from,
                                                           @RequestParam(value = "to", required = false) LocalDate to,
                                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                                           @RequestParam(value = "size", defaultValue = "10") int size,
                                                           @RequestParam(value = "sortBy", defaultValue = "user_id") String sortBy) {
        return ResponseEntity.ok(paymentService
                .findAllByFields(userId, orderId, from, to, status, principal, page, size, sortBy));
    }

    /**
     * Returns summary of all user's operations.
     * @param userId
     * @param from
     * @param to
     * @return ResponseEntity<BigDecimal>
     */
    @GetMapping("/users/{user_id}/summary")
    @PreAuthorize("hasRole('ADMIN') || authentication.principal.id == #userId")
    public ResponseEntity<BigDecimal> getUserSummary(@PathVariable("user_id") Long userId,
                                                     @RequestParam("from") LocalDate from,
                                                     @RequestParam("to") LocalDate to) {
        return ResponseEntity.ok(paymentService.getSummary(userId, from, to));
    }

    /**
     * Return summary of all operations of all users.
     * @param from
     * @param to
     * @return
     */
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BigDecimal> getGeneralSummary(@RequestParam("from") LocalDate from,
                                                        @RequestParam("to") LocalDate to) {
        return ResponseEntity.ok(paymentService.getSummary(from, to));
    }
}
