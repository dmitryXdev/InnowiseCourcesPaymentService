package com.innowise.paymentservice.exception;

public class BadIncomingDataException extends RuntimeException {
    public BadIncomingDataException(String message) {
        super(message);
    }
}
