package com.finpay.dto.response;

import com.finpay.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MerchantPaymentResponse {

    private String paymentReference;
    private String merchantNumber;
    private String businessName;
    private BigDecimal amount;
    private PaymentStatus status;
    private BigDecimal customerBalanceAfter;
    private BigDecimal merchantBalanceAfter;
    private LocalDateTime createdAt;
}
