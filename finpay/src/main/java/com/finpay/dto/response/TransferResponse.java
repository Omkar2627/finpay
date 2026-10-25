package com.finpay.dto.response;

import com.finpay.entity.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TransferResponse {
    
    private String transferReference;
    private String senderWalletNumber;
    private String receiverWalletNumber;
    private BigDecimal amount;
    private TransferStatus status;
    private BigDecimal senderBalanceAfter;
    private BigDecimal receiverBalanceAfter;
    private LocalDateTime createdAt;
}
