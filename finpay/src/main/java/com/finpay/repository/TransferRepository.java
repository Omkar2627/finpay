package com.finpay.repository;

import com.finpay.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    Optional<Transfer> findByTransferReference(String trasnferReference);


    @Query("""
            SELECT t
            FROM Transfer t
            WHERE t.senderWallet.id = :senderWalletId
            AND t.idempotencyKey = :idempotencyKey
            """)
    Optional<Transfer> findBySenderWalletIdAndIdempotencyKey(
            @Param("senderWalletId") Long senderWalletId,
            @Param("idempotencyKey") String idempotencyKey
    );
}
