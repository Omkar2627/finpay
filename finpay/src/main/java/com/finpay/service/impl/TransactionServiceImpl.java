package com.finpay.service.impl;

import com.finpay.dto.response.TransactionResponse;
import com.finpay.entity.Transaction;
import com.finpay.entity.User;
import com.finpay.entity.Wallet;
import com.finpay.exception.TransactionNotFoundException;
import com.finpay.exception.UserNotFoundException;
import com.finpay.mapper.TransactionMapper;
import com.finpay.repository.TransactionRepository;
import com.finpay.repository.UserRepository;
import com.finpay.repository.WalletRepository;
import com.finpay.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {


    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public List<TransactionResponse> getMyTransactions() {

        User user = getAuthenticatedUser();
        Wallet wallet = walletRepository.findByUserId(user.getId()).orElseThrow(() -> new TransactionNotFoundException("wallet not found for the current user"));

        return transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId()).stream().map(transactionMapper::toResponse).toList();
    }

    @Override
    public TransactionResponse getMyTransactionByReference(String transactionReference) {
        User user = getAuthenticatedUser();
        Wallet wallet = walletRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new TransactionNotFoundException(
                                "Wallet not found for current user"
                        )
                );
        Transaction transaction = transactionRepository.findByTransactionReference(transactionReference).orElseThrow(() -> new TransactionNotFoundException("Transaction Not Found"));
        if (!transaction.getWallet().getId().equals(wallet.getId())) {
            throw new TransactionNotFoundException("Transaction Not Found");
        }
        return transactionMapper.toResponse(transaction);
    }

    private User getAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "User is not authenticated"
            );
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }
}



