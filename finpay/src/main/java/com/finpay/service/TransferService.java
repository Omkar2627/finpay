package com.finpay.service;

import com.finpay.dto.request.TransferRequest;
import com.finpay.dto.response.TransferResponse;

public interface TransferService {

    TransferResponse transfer(TransferRequest request, String idempotencyKey);
}
