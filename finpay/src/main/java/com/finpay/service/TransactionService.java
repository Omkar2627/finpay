package com.finpay.service;

import com.finpay.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {

    List<TransactionResponse> getMyTransactions();

    TransactionResponse getMyTransactionByReference(
            String transactionReference
    );
}