package com.finpay.service.impl;

import com.finpay.dto.request.TransferRequest;
import com.finpay.dto.response.TransferResponse;
import com.finpay.entity.*;
import com.finpay.exception.IdempotencyKeyReuseException;
import com.finpay.exception.InsufficientBalanceException;
import com.finpay.exception.UserNotFoundException;
import com.finpay.exception.WalletNotFoundException;
import com.finpay.repository.TransactionRepository;
import com.finpay.repository.TransferRepository;
import com.finpay.repository.UserRepository;
import com.finpay.repository.WalletRepository;
import com.finpay.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {


    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransferRepository transferRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public TransferResponse transfer(
            TransferRequest request,
            String idempotencyKey) {

        //1. validate the request and idempotency key

        validateRequest(request, idempotencyKey);

        String key = idempotencyKey.trim();
        BigDecimal amount = request.getAmount();

        // 2. Identify the sender using the authenticated JWT.
        User user = getAuthenticatedUser();

        Wallet sendingPerson = walletRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Sender wallet not found"
                        )
                );
        // 3. Find the receiver using their wallet number.
        Wallet receivingPerson = walletRepository
                .findByWalletNumber(request.getReceiverWalletNumber())
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Receiver wallet not found"
                        )
                );
        // 4. Prevent transfers to the same wallet.
        if (sendingPerson.getId().equals(receivingPerson.getId())) {
            throw new IllegalArgumentException("Cannot Transfer Money to Your Own Wallet");
        }
        // 5. Lock both wallets in a consistent order.
        List<Wallet> lockWallets = walletRepository.findWalletsForUpdate(List.of(sendingPerson.getId(), receivingPerson.getId()));

        if (lockWallets.size() != 2) {
            throw new WalletNotFoundException("sender or receiver wallet not found");
        }
        Map<Long, Wallet> walletsById = new HashMap<>();

        for (Wallet wallet : lockWallets) {
            walletsById.put(wallet.getId(), wallet);
        }

        Wallet senderWallet =
                walletsById.get(sendingPerson.getId());

        Wallet receiverWallet =
                walletsById.get(receivingPerson.getId());

        if (senderWallet == null || receiverWallet == null) {
            throw new WalletNotFoundException(
                    "Sender or receiver wallet not found"
            );
        }

        // 6. Check idempotency AFTER acquiring wallet locks.
        // A previous request may have completed while this request waited.
        var existingTransfer = transferRepository.findBySenderWalletIdAndIdempotencyKey(senderWallet.getId(), key);

        if (existingTransfer.isPresent()) {
            Transfer existing = existingTransfer.get();
            boolean sameRequest = existing.getReceiverWallet().getId().equals(receiverWallet.getId())
                    && existing.getAmount().compareTo(amount) == 0 && Objects.equals(existing.getDescription(), request.getDescription());
            if (!sameRequest) {
                throw new IdempotencyKeyReuseException(
                        "Idempotency key was already used "
                                + "for a different transfer"
                );
            }

            // Return the original result without moving money again.
            return toResponse(existing);
        }
        // 7. Validate both wallets.
        if (senderWallet.getStatus() != WalletStatus.ACTIVE || receiverWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotFoundException("Both sender and receiver wallets must be active");
        }

        if (!Objects.equals(senderWallet.getCurrency(), receiverWallet.getCurrency())) {
            throw new IllegalArgumentException("Sender and receiver currencies must match");
        }
        // 8. verify sufficient funds.
        BigDecimal senderBalance = senderWallet.getBalance();
        BigDecimal receiverBalance = receiverWallet.getBalance();

        if (senderBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }

        //9. calculate both new balance,

        BigDecimal senderBalanceAfter = senderBalance.subtract(amount);
        BigDecimal receiverBalanceAfter = receiverBalance.add(amount);

        validateBalanceCapacity(receiverBalanceAfter);

        LocalDateTime now = LocalDateTime.now();

        // 10.update both wallet

        senderWallet.setBalance(senderBalanceAfter);
        senderWallet.setUpdatedAt(now);

        receiverWallet.setBalance(receiverBalanceAfter);
        receiverWallet.setUpdatedAt(now);

        walletRepository.saveAll(List.of(senderWallet, receiverWallet));

        //11.Create the overall transfer record.
        String transferReference = "TRF-" + UUID.randomUUID();

        Transfer transfer = new Transfer();
        transfer.setTransferReference(transferReference);
        transfer.setSenderWallet(senderWallet);
        transfer.setReceiverWallet(receiverWallet);
        transfer.setIdempotencyKey(idempotencyKey);
        transfer.setAmount(amount);
        transfer.setSenderBalanceAfter(senderBalanceAfter);
        transfer.setReceiverBalanceAfter(receiverBalanceAfter);
        transfer.setStatus(TransferStatus.SUCCESS);
        transfer.setDescription(request.getDescription());
        transfer.setCreatedAt(now);
        transfer.setUpdatedAt(now);

        Transfer savedTransfer = transferRepository.save(transfer);

        //12.record the debit in the senders histroy
        Transaction senderTransaction = buildTransaction(senderWallet, TransactionType.TRANSFER_SENT, amount, request.getDescription(), now);

        //13. record the credit in the receiver history

        Transaction receiverTransaction = buildTransaction(receiverWallet, TransactionType.TRANSFER_RECEIVED, amount, request.getDescription(), now);

        transactionRepository.saveAll(List.of(senderTransaction, receiverTransaction));

        //14 return the saved transfer result
        return toResponse(savedTransfer);
    }

    private void validateRequest(TransferRequest request, String idempotencyKey) {

        if (request == null || request.getAmount() == null) {
            throw new IllegalArgumentException("Transfer amount is required");
        }

        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero"
            );
        }
        if (amount.scale() > 2 || amount.precision() - amount.scale() > 17) {
            throw new IllegalArgumentException("Amount must have at most 17 integer digits " + "and 2 decimal places");
        }
        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.trim().length() > 128) {
            throw new IllegalArgumentException(
                    "A valid Idempotency-Key is required"
            );
        }
        if (request.getReceiverWalletNumber() == null
                || request.getReceiverWalletNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Receiver wallet number is required"
            );
        }

    }

    private void validateBalanceCapacity(BigDecimal balance) {
        if (balance.precision() - balance.scale() > 17) {
            throw new IllegalArgumentException("Receiver balance exceed the supported limit");
        }
    }

    private Transaction buildTransaction(Wallet wallet, TransactionType type, BigDecimal amount, String descriptioon, LocalDateTime now) {

        Transaction transaction = new Transaction();
        transaction.setTransactionReference("TXN-" + UUID.randomUUID());
        transaction.setWallet(wallet);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription(descriptioon);
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);
        return transaction;
    }

    private TransferResponse toResponse(Transfer transfer) {

        return new TransferResponse(
                transfer.getTransferReference(),
                transfer.getSenderWallet().getWalletNumber(),
                transfer.getReceiverWallet().getWalletNumber(),
                transfer.getAmount(),
                transfer.getStatus(),
                transfer.getSenderBalanceAfter(),
                transfer.getReceiverBalanceAfter(),
                transfer.getCreatedAt()
        );
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "An authenticated user is required"
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
