package com.finpay.service.impl;

import com.finpay.dto.request.RefundRequest;
import com.finpay.dto.response.RefundResponse;
import com.finpay.entity.*;
import com.finpay.exception.*;
import com.finpay.repository.*;
import com.finpay.service.RefundService;
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
public class RefundServiceImpl implements RefundService {

    private final UserRepository userRepository;
    private final MerchantPaymentRepository merchantPaymentRepository;
    private final RefundRepository refundRepository;
    private final WalletRepository walletRepository;
    private final MerchantWalletRepository merchantWalletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public RefundResponse initiateRefund(
            String paymentReference,
            RefundRequest request,
            String idempotencyKey) {

        // 1. Validate request and idempotency key.
        validateRequest(paymentReference, request, idempotencyKey);

        String key = idempotencyKey.trim();
        BigDecimal refundAmount = request.getAmount();
        String reason = request.getReason().trim();

        // 2. Identify the authenticated merchant owner.
        User owner = getAuthenticatedUser();

        // 3. Lock the original payment.
        MerchantPayment payment = merchantPaymentRepository
                .findByPaymentReferenceForUpdate(paymentReference)
                .orElseThrow(() ->
                        new RefundNotFoundException(
                                "Original payment not found"
                        )
                );
        // 4. Verify that the authenticated user owns this merchant.
        Merchant merchant = payment.getMerchantWallet().getMerchant();

        if (!merchant.getOwner().getId().equals(owner.getId())) {
            throw new RefundNotAllowedException(
                    "You are not authorized to refund this payment"
            );
        }
        // 5. Check whether this refund request was already processed.
        // The payment lock serializes concurrent refunds for this payment.
        var existingRefund = refundRepository
                .findByPaymentAndIdempotencyKey(
                        payment.getId(),
                        key
                );

        if (existingRefund.isPresent()) {

            Refund existing = existingRefund.get();

            boolean sameRequest =
                    existing.getAmount().compareTo(refundAmount) == 0
                            && Objects.equals(existing.getReason(), reason);

            if (!sameRequest) {
                throw new IdempotencyKeyReuseException(
                        "Idempotency key was already used "
                                + "for a different refund request"
                );
            }

            // Return the original result without crediting again.
            return toResponse(existing);
        }
        // 6. Only successful payments can be refunded.
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RefundNotAllowedException(
                    "Only successful payments can be refunded"
            );
        }

        // 7. Lock the original customer wallet.
        Wallet customerWallet = walletRepository
                .findByIdForUpdate(
                        payment.getCustomerWallet().getId()
                )
                .orElseThrow(() ->
                        new WalletNotFoundException(
                                "Customer wallet not found"
                        )
                );
        // 8. Lock the merchant wallet.
        MerchantWallet merchantWallet = merchantWalletRepository
                .findByMerchantIdForUpdate(merchant.getId())
                .orElseThrow(() ->
                        new RefundNotAllowedException(
                                "Merchant wallet is not available for refund"
                        )
                );
        // 9. Validate wallet statuses.
        if (customerWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException(
                    "Customer wallet is not active"
            );
        }

        if (merchantWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException(
                    "Merchant wallet is not active"
            );
        }
        // 10. Verify currency compatibility.
        if (!Objects.equals(
                customerWallet.getCurrency(),
                merchantWallet.getCurrency())) {

            throw new RefundNotAllowedException(
                    "Customer and merchant wallet currencies must match"
            );
        }
        // 11. Calculate the total amount already refunded or pending.
        BigDecimal totalRefunded =
                refundRepository.calculateTotalRefundedAmount(
                        payment.getId(),
                        RefundStatus.FAILED
                );
        // 12. Calculate remaining refundable amount.
        BigDecimal remainingRefundable =
                payment.getAmount().subtract(totalRefunded);

        if (refundAmount.compareTo(remainingRefundable) > 0) {
            throw new RefundAmountExceededException(
                    "Refund amount exceeds remaining refundable amount: "
                            + remainingRefundable
            );
        }
        // 13. Ensure merchant has sufficient funds for the refund.
        BigDecimal merchantBalance = merchantWallet.getBalance();

        if (merchantBalance.compareTo(refundAmount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient merchant wallet balance for refund"
            );
        }
        // 14. Calculate both updated balances.
        BigDecimal customerBalanceAfter =
                customerWallet.getBalance().add(refundAmount);

        BigDecimal merchantBalanceAfter =
                merchantBalance.subtract(refundAmount);

        validateBalanceCapacity(customerBalanceAfter);

        LocalDateTime now = LocalDateTime.now();
        // 15. Update customer and merchant wallets.
        customerWallet.setBalance(customerBalanceAfter);
        customerWallet.setUpdatedAt(now);

        merchantWallet.setBalance(merchantBalanceAfter);
        merchantWallet.setUpdatedAt(now);

        walletRepository.save(customerWallet);
        merchantWalletRepository.save(merchantWallet);

        // 16. Create the refund record.
        Refund refund = new Refund();

        refund.setRefundReference(
                "REF-" + UUID.randomUUID()
        );
        refund.setMerchantPayment(payment);
        refund.setIdempotencyKey(key);
        refund.setAmount(refundAmount);
        refund.setStatus(RefundStatus.SUCCESS);
        refund.setReason(reason);
        refund.setCustomerBalanceAfter(customerBalanceAfter);
        refund.setMerchantBalanceAfter(merchantBalanceAfter);
        refund.setCreatedAt(now);
        refund.setUpdatedAt(now);

        Refund savedRefund = refundRepository.save(refund);
        // 17. Add the customer's refund credit to transaction history.
        Transaction customerTransaction = new Transaction();

        customerTransaction.setTransactionReference(
                "TXN-" + UUID.randomUUID()
        );
        customerTransaction.setWallet(customerWallet);
        customerTransaction.setTransactionType(
                TransactionType.REFUND_RECEIVED
        );
        customerTransaction.setAmount(refundAmount);
        customerTransaction.setStatus(TransactionStatus.SUCCESS);
        customerTransaction.setDescription(
                "Refund for payment "
                        + payment.getPaymentReference()
                        + ": " + reason
        );
        customerTransaction.setCreatedAt(now);
        customerTransaction.setUpdatedAt(now);

        transactionRepository.save(customerTransaction);

        // 18. Return the refund response.
        return toResponse(savedRefund);
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
                    "Authenticated user is required"
            );
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    private void validateRequest(
            String paymentReference,
            RefundRequest request,
            String idempotencyKey) {

        if (paymentReference == null
                || paymentReference.isBlank()) {

            throw new IllegalArgumentException(
                    "Payment reference is required"
            );
        }

        if (request == null || request.getAmount() == null) {
            throw new IllegalArgumentException(
                    "Refund amount is required"
            );
        }

        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Refund amount must be greater than zero"
            );
        }

        if (amount.scale() > 2
                || amount.precision() - amount.scale() > 17) {

            throw new IllegalArgumentException(
                    "Refund amount must have at most "
                            + "17 integer digits and 2 decimal places"
            );
        }

        if (request.getReason() == null
                || request.getReason().isBlank()) {

            throw new IllegalArgumentException(
                    "Refund reason is required"
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
                    "Customer balance exceeds the supported limit"
            );
        }
    }

    private RefundResponse toResponse(Refund refund) {

        MerchantPayment payment = refund.getMerchantPayment();

        return new RefundResponse(
                refund.getRefundReference(),
                payment.getPaymentReference(),
                refund.getAmount(),
                refund.getStatus(),
                refund.getCustomerBalanceAfter(),
                refund.getMerchantBalanceAfter(),
                refund.getCreatedAt()
        );
    }
}
