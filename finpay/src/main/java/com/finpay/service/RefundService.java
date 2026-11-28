package com.finpay.service;

import com.finpay.dto.request.RefundRequest;
import com.finpay.dto.response.RefundResponse;

public interface RefundService {

    RefundResponse initiateRefund(
            String paymentReference,
            RefundRequest request,
            String idempotencyKey
    );
}
