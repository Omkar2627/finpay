package com.finpay.service.impl;

import com.finpay.dto.request.MerchantPaymentRequest;
import com.finpay.dto.response.MerchantPaymentResponse;
import com.finpay.entity.*;
import com.finpay.exception.*;
import com.finpay.repository.*;
import com.finpay.service.MerchantPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantPaymentServiceImpl implements MerchantPaymentService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantWalletRepository merchantWalletRepository;
    private final MerchantPaymentRepository merchantPaymentRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public MerchantPaymentResponse makePayment(
            String merchantNumber,
            MerchantPaymentRequest request,
            String idempotencyKey) {

        // 1. Validate request and idempotency key.
        validateRequest(merchantNumber, request, idempotencyKey);

        String key = idempotencyKey.trim();
        BigDecimal amount = request.getAmount();

        // 2. Identify customer from JWT.
        User customer = getAuthenticatedUser();

        // 3. Lock customer's wallet.
        Wallet customerWallet = walletRepository
                .findByUserIdForUpdate(customer.getId())
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Customer wallet not found"
                        )
                );

        if (customerWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException(
                    "Customer wallet is not active"
            );
        }

        /*
         * 4. Check idempotency after locking the customer wallet.
         *
         * A concurrent retry from this customer must wait for
         * the first request to finish before reaching this check.
         */
        var existingPayment =
                merchantPaymentRepository
                        .findByCustomerWalletAndIdempotencyKey(
                                customerWallet.getId(),
                                key
                        );

        if (existingPayment.isPresent()) {

            MerchantPayment existing = existingPayment.get();

            boolean sameRequest =
                    Objects.equals(
                            existing.getMerchantWallet()
                                    .getMerchant()
                                    .getMerchantNumber(),
                            merchantNumber
                    )
                            && existing.getAmount().compareTo(amount) == 0
                            && Objects.equals(
                            existing.getDescription(),
                            request.getDescription()
                    );

            if (!sameRequest) {
                throw new IdempotencyKeyReuseException(
                        "Idempotency key was already used "
                                + "for a different payment"
                );
            }

            return toResponse(existing);
        }

        // 5. Find and lock the merchant.
        Merchant merchant = merchantRepository
                .findByMerchantNumberForUpdate(merchantNumber)
                .orElseThrow(() ->
                        new MerchantNotFoundException(
                                "Merchant not found"
                        )
                );

        // 6. Only approved, active merchants can receive payments.
        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw new MerchantNotActiveException(
                    "Merchant is not approved or active"
            );
        }

        // 7. Find and lock the merchant wallet.
        MerchantWallet merchantWallet =
                merchantWalletRepository
                        .findByMerchantIdForUpdate(merchant.getId())
                        .orElseThrow(() ->
                                new MerchantNotFoundException(
                                        "Merchant wallet not found"
                                )
                        );

        if (merchantWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException(
                    "Merchant wallet is not active"
            );
        }

        // 8. Verify currency compatibility.
        if (!Objects.equals(
                customerWallet.getCurrency(),
                merchantWallet.getCurrency())) {

            throw new IllegalArgumentException(
                    "Customer and merchant currencies must match"
            );
        }

        // 9. Validate sufficient customer funds.
        BigDecimal customerBalance = customerWallet.getBalance();

        if (customerBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient customer wallet balance"
            );
        }

        BigDecimal merchantBalance = merchantWallet.getBalance();

        // 10. Calculate new balances.
        BigDecimal customerBalanceAfter =
                customerBalance.subtract(amount);

        BigDecimal merchantBalanceAfter =
                merchantBalance.add(amount);

        validateBalanceCapacity(merchantBalanceAfter);

        LocalDateTime now = LocalDateTime.now();

        // 11. Update both wallets.
        customerWallet.setBalance(customerBalanceAfter);
        customerWallet.setUpdatedAt(now);

        merchantWallet.setBalance(merchantBalanceAfter);
        merchantWallet.setUpdatedAt(now);

        walletRepository.save(customerWallet);
        merchantWalletRepository.save(merchantWallet);

        // 12. Create the merchant payment record.
        MerchantPayment payment = new MerchantPayment();

        payment.setPaymentReference(
                "PAY-" + UUID.randomUUID()
        );
        payment.setCustomerWallet(customerWallet);
        payment.setMerchantWallet(merchantWallet);
        payment.setIdempotencyKey(key);
        payment.setAmount(amount);
        payment.setCustomerBalanceAfter(customerBalanceAfter);
        payment.setMerchantBalanceAfter(merchantBalanceAfter);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setDescription(request.getDescription());
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        MerchantPayment savedPayment =
                merchantPaymentRepository.save(payment);

        // 13. Record the customer's payment debit in transaction history.
        Transaction transaction = new Transaction();

        transaction.setTransactionReference(
                "TXN-" + UUID.randomUUID()
        );
        transaction.setWallet(customerWallet);
        transaction.setTransactionType(
                TransactionType.MERCHANT_PAYMENT
        );
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription(
                request.getDescription() == null
                        ? "Payment to " + merchant.getBusinessName()
                        : request.getDescription()
        );
        transaction.setCreatedAt(now);
        transaction.setUpdatedAt(now);

        transactionRepository.save(transaction);

        // 14. Return the payment response.
        return toResponse(savedPayment);
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new IllegalStateException(
                    "Authenticated customer is required"
            );
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Authenticated customer not found"
                        )
                );
    }

    private void validateRequest(
            String merchantNumber,
            MerchantPaymentRequest request,
            String idempotencyKey) {

        if (merchantNumber == null
                || merchantNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Merchant number is required"
            );
        }

        if (request == null || request.getAmount() == null) {
            throw new IllegalArgumentException(
                    "Payment amount is required"
            );
        }

        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        if (amount.scale() > 2
                || amount.precision() - amount.scale() > 17) {
            throw new IllegalArgumentException(
                    "Payment amount must have at most "
                            + "17 integer digits and 2 decimal places"
            );
        }

        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.trim().length() > 128) {
            throw new IllegalArgumentException(
                    "A valid Idempotency-Key is required"
            );
        }
    }

    private void validateBalanceCapacity(BigDecimal balance) {

        if (balance.precision() - balance.scale() > 17) {
            throw new IllegalArgumentException(
                    "Merchant balance exceeds the supported limit"
            );
        }
    }

    private MerchantPaymentResponse toResponse(
            MerchantPayment payment) {

        Merchant merchant =
                payment.getMerchantWallet().getMerchant();

        return new MerchantPaymentResponse(
                payment.getPaymentReference(),
                merchant.getMerchantNumber(),
                merchant.getBusinessName(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCustomerBalanceAfter(),
                payment.getMerchantBalanceAfter(),
                payment.getCreatedAt()
        );
    }

}
