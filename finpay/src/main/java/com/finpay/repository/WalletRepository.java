package com.finpay.repository;

import com.finpay.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

    Optional<Wallet> findByWalletNumber(String walletNumber);

    boolean existsByUserId(Long userId);

    boolean existsByWalletNumber(String walletNumber);

}
