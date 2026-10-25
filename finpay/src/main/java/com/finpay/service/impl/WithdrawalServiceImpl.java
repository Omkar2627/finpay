package com.finpay.service.impl;

import com.finpay.dto.request.WithdrawalRequest;
import com.finpay.dto.response.WithdrawalResponse;
import com.finpay.entity.*;
import com.finpay.exception.InsufficientBalanceException;
import com.finpay.exception.WalletNotActiveException;
import com.finpay.exception.WalletNotFoundException;
import com.finpay.repository.TransactionRepository;
import com.finpay.repository.UserRepository;
import com.finpay.repository.WalletRepository;
import com.finpay.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;


    @Override
    @Transactional
    public WithdrawalResponse withdraw(WithdrawalRequest request) {

        // 1. Identify the authenticated customer.
        User user = getAuthenticatedUser();

        // 2. Lock and retrieve that customer's wallet.
        Wallet wallet = walletRepository
                .findByUserIdForUpdate(user.getId())
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Wallet not found for current user"
                        )
                );
        // 3. Only active wallets may be used.

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException("cannot withdraw from an inactive or blocked wallet");
        }
        // 4. Validate the amount at the business-logic level too.

        BigDecimal amount = request.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.scale() > 2 || amount.precision() - amount.scale() > 17) {
            throw new IllegalArgumentException("Withdrawal amount must be positive and have "
                    + "at most 17 integer digits and 2 decimal places");
        }

        // 5. Ensure sufficient funds.
        BigDecimal currentBalance = wallet.getBalance();

        if (currentBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        // 6. Calculate the new balance.
        BigDecimal newBalance = currentBalance.subtract(amount);
        LocalDateTime now = LocalDateTime.now();

        // 7. Update the wallet.
        wallet.setBalance(newBalance);
        wallet.setUpdatedAt(now);
        walletRepository.save(wallet);

        //8. create the withdrawl transaction

        Transaction transaction = new Transaction();

        transaction.setTransactionReference("TXN-" + UUID.randomUUID());
        transaction.setWallet(wallet);
        transaction.setTransactionType(TransactionType.WITHDRAWAL);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription(request.getDescription());
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return new WithdrawalResponse(
                savedTransaction.getTransactionReference(),
                savedTransaction.getTransactionType(),
                savedTransaction.getAmount(),
                savedTransaction.getStatus(),
                newBalance,
                savedTransaction.getCreatedAt()
        );

    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
    }
}
