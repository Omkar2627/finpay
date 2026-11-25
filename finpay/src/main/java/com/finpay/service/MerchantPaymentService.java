package com.finpay.service;


import com.finpay.dto.request.MerchantPaymentRequest;
import com.finpay.dto.response.MerchantPaymentResponse;

public interface MerchantPaymentService {

    MerchantPaymentResponse makePayment(
            String merchantNumber,
            MerchantPaymentRequest request,
            String idempotencyKey
    );
}
