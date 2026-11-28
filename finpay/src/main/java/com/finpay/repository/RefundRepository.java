package com.finpay.repository;

import com.finpay.entity.Refund;
import com.finpay.entity.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RefundRepository
        extends JpaRepository<Refund, Long> {

    // Find refund using its business reference.
    Optional<Refund> findByRefundReference(
            String refundReference
    );

    // Check whether the same payment has already used this idempotency key.
    @Query("""
            SELECT r
            FROM Refund r
            WHERE r.merchantPayment.id = :paymentId
            AND r.idempotencyKey = :idempotencyKey
            """)
    Optional<Refund> findByPaymentAndIdempotencyKey(
            @Param("paymentId") Long paymentId,
            @Param("idempotencyKey") String idempotencyKey
    );

    // Calculate the total amount already refunded or reserved as pending.
    @Query("""
            SELECT COALESCE(SUM(r.amount), 0)
            FROM Refund r
            WHERE r.merchantPayment.id = :paymentId
            AND r.status <> :failedStatus
            """)
    BigDecimal calculateTotalRefundedAmount(
            @Param("paymentId") Long paymentId,
            @Param("failedStatus") RefundStatus failedStatus
    );

    // Retrieve all refunds belonging to an original payment.
    List<Refund> findByMerchantPayment_IdOrderByCreatedAtDesc(
            Long paymentId
    );


}

