package com.finpay.exception;

public class IdempotencyKeyReuseException extends RuntimeException {

    public IdempotencyKeyReuseException(String message) {
        super(message);
    }
}
