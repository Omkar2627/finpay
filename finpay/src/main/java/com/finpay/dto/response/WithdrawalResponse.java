package com.finpay.dto.response;

import com.finpay.entity.TransactionStatus;
import com.finpay.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@AllArgsConstructor
public class WithdrawalResponse {

    private String transactionReference;
    private TransactionType transactionType;
    private BigDecimal amount;
    private TransactionStatus status;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
}
