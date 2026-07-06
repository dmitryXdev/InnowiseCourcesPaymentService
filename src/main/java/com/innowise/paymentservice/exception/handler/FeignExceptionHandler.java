package com.innowise.paymentservice.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static feign.FeignException.BadRequest;
import static feign.FeignException.Forbidden;
import static feign.FeignException.InternalServerError;
import static feign.FeignException.ServiceUnavailable;

@RestControllerAdvice
public class FeignExceptionHandler {
    @ExceptionHandler(InternalServerError.class)
    public ResponseEntity<ErrorResponse> internalServiceError(InternalServerError e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(
                        ErrorResponse.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .build()
                );
    }

    @ExceptionHandler(ServiceUnavailable.class)
    public ResponseEntity<ErrorResponse> serviceUnavailable(ServiceUnavailable e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY.value())
                .body(
                        ErrorResponse.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.BAD_GATEWAY.value())
                                .build()
                );
    }

    @ExceptionHandler(Forbidden.class)
    public ResponseEntity<ErrorResponse> serviceForbidden(Forbidden e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN.value())
                .body(
                        ErrorResponse.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.FORBIDDEN.value())
                                .build()
                );
    }

    @ExceptionHandler(BadRequest.class)
    public ResponseEntity<ErrorResponse> badRequest(BadRequest e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(
                        ErrorResponse.builder()
                                .message(e.getMessage())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .build()
                );
    }
}
