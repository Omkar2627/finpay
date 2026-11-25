package com.finpay.repository;


import com.finpay.entity.MerchantPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MerchantPaymentRepository
        extends JpaRepository<MerchantPayment, Long> {

    @Query("""
            SELECT p
            FROM MerchantPayment p
            WHERE p.customerWallet.id = :customerWalletId
            AND p.idempotencyKey = :idempotencyKey
            """)
    Optional<MerchantPayment> findByCustomerWalletAndIdempotencyKey(
            @Param("customerWalletId") Long customerWalletId,
            @Param("idempotencyKey") String idempotencyKey
    );
}
