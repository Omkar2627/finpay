package com.finpay.service;

import com.finpay.dto.response.WalletResponse;

import java.math.BigDecimal;

public interface WalletService {

    WalletResponse createWallet();

    WalletResponse getMyWallet();

    BigDecimal getMyBalance();
}