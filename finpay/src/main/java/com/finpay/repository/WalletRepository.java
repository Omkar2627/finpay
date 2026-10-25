package com.finpay.repository;

import com.finpay.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

    Optional<Wallet> findByWalletNumber(String walletNumber);

    boolean existsByUserId(Long userId);

    boolean existsByWalletNumber(String walletNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId")
    Optional<Wallet> findByUserIdForUpdate(
            @Param("userId") Long userId
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT w
            FROM Wallet w
            WHERE w.id IN :walletIds
            ORDER BY w.id ASC
            """)
    List<Wallet> findWalletsForUpdate(
            @Param("walletIds") Collection<Long> walletIds
    );
}
