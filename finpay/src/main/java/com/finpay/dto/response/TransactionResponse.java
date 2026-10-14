package com.finpay.dto.response;


import com.finpay.entity.TransactionStatus;
import com.finpay.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private String transactionReference;
    private String walletNumber;
    private TransactionType transactionType;
    private BigDecimal amount;
    private TransactionStatus transactionStatus;
    private String description;
    private LocalDateTime createdAt;
}
