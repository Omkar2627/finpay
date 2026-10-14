package com.finpay.mapper;


import com.finpay.dto.response.TransactionResponse;
import com.finpay.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getWallet().getWalletNumber(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}
