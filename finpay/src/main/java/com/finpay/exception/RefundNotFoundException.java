package com.finpay.exception;

public class RefundNotFoundException extends RuntimeException {

    public RefundNotFoundException(String message) {
        super(message);
    }
}
