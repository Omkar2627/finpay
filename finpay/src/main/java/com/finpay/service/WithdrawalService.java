package com.finpay.service;

import com.finpay.dto.request.WithdrawalRequest;
import com.finpay.dto.response.WithdrawalResponse;

public interface WithdrawalService {

    WithdrawalResponse withdraw(WithdrawalRequest request);
}
