package com.finpay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "refunds",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_refund_reference",
                        columnNames = "refund_reference"
                ),
                @UniqueConstraint(
                        name = "uk_refund_payment_idempotency",
                        columnNames = {
                                "merchant_payment_id",
                                "idempotency_key"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "refund_reference",
            nullable = false,
            length = 50
    )
    private String refundReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "merchant_payment_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_refund_merchant_payment"
            )
    )
    private MerchantPayment merchantPayment;

    @Column(
            name = "idempotency_key",
            nullable = false,
            length = 128
    )
    private String idempotencyKey;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private RefundStatus status;

    @Column(length = 255)
    private String reason;

    @Column(
            name = "customer_balance_after",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal customerBalanceAfter;

    @Column(
            name = "merchant_balance_after",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal merchantBalanceAfter;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;
}
