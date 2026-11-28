package com.finpay.dto.response;

import com.finpay.entity.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RefundResponse {

    private String refundReference;

    private String paymentReference;

    private BigDecimal refundAmount;

    private RefundStatus status;

    private BigDecimal customerBalanceAfter;

    private BigDecimal merchantBalanceAfter;

    private LocalDateTime createdAt;
}