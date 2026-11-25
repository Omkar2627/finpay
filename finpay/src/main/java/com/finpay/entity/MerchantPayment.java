package com.finpay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "merchant_payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_merchant_payment_reference",
                        columnNames = "payment_reference"
                ),
                @UniqueConstraint(
                        name = "uk_customer_payment_idempotency",
                        columnNames = {
                                "customer_wallet_id",
                                "idempotency_key"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class MerchantPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "payment_reference",
            nullable = false,
            length = 50
    )
    private String paymentReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_wallet_id", nullable = false)
    private Wallet customerWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_wallet_id", nullable = false)
    private MerchantWallet merchantWallet;

    @Column(
            name = "idempotency_key",
            nullable = false,
            length = 128
    )
    private String idempotencyKey;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "customer_balance_after",
            nullable = false, precision = 19, scale = 2)
    private BigDecimal customerBalanceAfter;

    @Column(name = "merchant_balance_after",
            nullable = false, precision = 19, scale = 2)
    private BigDecimal merchantBalanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}