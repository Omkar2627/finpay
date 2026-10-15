package com.finpay.service;

import com.finpay.dto.request.DepositRequest;
import com.finpay.dto.response.DepositResponse;

public interface DepositService {

    DepositResponse deposit(DepositRequest request);
}
