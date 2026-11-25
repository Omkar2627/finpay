package com.finpay.repository;

import com.finpay.entity.Merchant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByMerchantNumber(String merchantNumber);

    Optional<Merchant> findByOwner_Id(Long ownerId);

    boolean existsByMerchantNumber(String merchantNumber);

    boolean existsByOwner_Id(Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT m
            FROM Merchant m
            WHERE m.merchantNumber = :merchantNumber
            """)
    Optional<Merchant> findByMerchantNumberForUpdate(
            @Param("merchantNumber") String merchantNumber
    );
}
