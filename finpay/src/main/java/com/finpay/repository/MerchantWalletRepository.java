package com.finpay.repository;

import com.finpay.entity.MerchantWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MerchantWalletRepository
        extends JpaRepository<MerchantWallet, Long> {

    Optional<MerchantWallet> findByMerchantId(Long merchantId);

    boolean existsByMerchantId(Long merchantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT mw
            FROM MerchantWallet mw
            WHERE mw.merchant.id = :merchantId
            """)
    Optional<MerchantWallet> findByMerchantIdForUpdate(
            @Param("merchantId") Long merchantId
    );
}
