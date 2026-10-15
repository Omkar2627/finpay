package com.finpay.service.impl;

import com.finpay.controller.UserController;
import com.finpay.dto.request.DepositRequest;
import com.finpay.dto.response.DepositResponse;
import com.finpay.entity.*;
import com.finpay.exception.UserNotFoundException;
import com.finpay.exception.WalletNotFoundException;
import com.finpay.repository.TransactionRepository;
import com.finpay.repository.UserRepository;
import com.finpay.repository.WalletRepository;
import com.finpay.service.DepositService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class DepositServiceImpl implements DepositService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public DepositResponse deposit(DepositRequest depositRequest) {
        // 1. Identify the authenticated customer.
        User user = getAuthicatedUser();

        // 2. Find that customer's wallet.
        Wallet wallet = walletRepository.findByUserId(user.getId()).orElseThrow(() -> new WalletNotFoundException(" Wallent Not Found For The Current User "));

        // 3. Only active wallets may receive deposits.
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotFoundException("Cannot deposit into an inactive or blocked wallet");
        }

        // 4. Validate the amount at the business-logic level too.
        BigDecimal amount = depositRequest.getAmount();
        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 2
                || amount.precision() - amount.scale() > 17) {
            throw new IllegalArgumentException(
                    "Deposit amount must be positive and have at most "
                            + "17 integer digits and 2 decimal places"
            );
        }

        // 5. Calculate the new balance without using floating-point types.
        BigDecimal newBalance = wallet.getBalance().add(amount);

        if (newBalance.precision() - newBalance.scale() > 17) {
            throw new IllegalArgumentException("Resulting wallet balance exceed the supported limit");
        }
        LocalDateTime now = LocalDateTime.now();

        // 6. Update the Wallet

        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(now);

        walletRepository.save(wallet);

        //7. Create a transaction record for the deposit.

        Transaction transaction = new Transaction();

        transaction.setTransactionReference("TXN-" + UUID.randomUUID());
        transaction.setWallet(wallet);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription(depositRequest.getDescription());
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);

        Transaction savedTransaction = transactionRepository.save(transaction);

        //return the transaction detail and update balance

        return new DepositResponse(
                savedTransaction.getTransactionReference(),
                savedTransaction.getTransactionType(),
                savedTransaction.getAmount(),
                savedTransaction.getStatus(),
                newBalance,
                savedTransaction.getCreatedAt()
        );
    }

    private User getAuthicatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(
                "Authenticated user not found"
        ));
    }
}
