package com.finpay.repository;

import com.finpay.entity.Transaction;
import com.finpay.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(
            String transactionReference
    );

    List<Transaction> findByWalletIdOrderByCreatedAtDesc(
            Long walletId
    );

    List<Transaction> findByWalletIdAndTransactionTypeOrderByCreatedAtDesc(
            Long walletId,
            TransactionType transactionType
    );
}
