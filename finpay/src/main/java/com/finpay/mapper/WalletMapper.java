package com.finpay.mapper;

import com.finpay.dto.response.WalletResponse;
import com.finpay.entity.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletResponse toResponse(Wallet wallet) {

        return new WalletResponse(
                wallet.getId(),
                wallet.getWalletNumber(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getStatus(),
                wallet.getCreatedAt()
        );
    }
}
