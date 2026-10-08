package com.finpay.service.impl;

import com.finpay.dto.response.WalletResponse;
import com.finpay.entity.User;
import com.finpay.entity.Wallet;
import com.finpay.entity.WalletStatus;
import com.finpay.exception.UserNotFoundException;
import com.finpay.exception.WalletAlreadyExistsException;
import com.finpay.exception.WalletNotFoundException;
import com.finpay.mapper.WalletMapper;
import com.finpay.repository.UserRepository;
import com.finpay.repository.WalletRepository;
import com.finpay.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class WalletServiceImpl  implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WalletMapper walletMapper;

    public WalletServiceImpl ( WalletRepository walletRepository,UserRepository userRepository,WalletMapper walletMapper){
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.walletMapper = walletMapper;
    }


    @Override
    @Transactional
    public WalletResponse createWallet() {

        User user = getAuthenticatedUser();

        if (walletRepository.existsByUserId(user.getId())) {
            throw new WalletAlreadyExistsException(
                    "Wallet already exists for this user"
            );
        }

        Wallet wallet = new Wallet();

        wallet.setWalletNumber(generateWalletNumber());
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency("INR");
        wallet.setStatus(WalletStatus.ACTIVE);

        LocalDateTime now = LocalDateTime.now();

        wallet.setCreatedAt(now);
        wallet.setUpdatedAt(now);

        Wallet savedWallet = walletRepository.save(wallet);

        return walletMapper.toResponse(savedWallet);
    }

    @Override
    public WalletResponse getMyWallet() {

        User user = getAuthenticatedUser();

        Wallet wallet = walletRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found for current user"
                        )
                );

        return walletMapper.toResponse(wallet);
    }

    @Override
    public BigDecimal getMyBalance() {

        User user = getAuthenticatedUser();

        Wallet wallet = walletRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found for current user"
                        )
                );

        return wallet.getBalance();
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    private String generateWalletNumber() {

        return "FP" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase();
    }
}
