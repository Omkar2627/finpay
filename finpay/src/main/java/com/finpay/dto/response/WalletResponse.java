package com.finpay.dto.response;


import com.finpay.entity.WalletStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class WalletResponse {

    private Long id;

    private String walletNumber;

    private BigDecimal balance;

    private String currency;

    private WalletStatus status;

    private LocalDateTime createdAt;
}