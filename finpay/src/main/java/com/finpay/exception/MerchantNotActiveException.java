package com.finpay.exception;

public class MerchantNotActiveException extends RuntimeException {
    public MerchantNotActiveException(String message) {

        super(message);
    }
}
