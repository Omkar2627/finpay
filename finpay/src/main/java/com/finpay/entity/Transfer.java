package com.finpay.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_reference", nullable = false, length = 50)
    private String transferReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_wallet_id", nullable = false)
    private Wallet senderWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_wallet_id", nullable = false)
    private Wallet receiverWallet;

    @Column(
            name = "idempotency_key",
            nullable = false,
            length = 128
    )
    private String idempotencyKey;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(
            name = "sender_balance_after",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal senderBalanceAfter;

    @Column(
            name = "receiver_balance_after",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal receiverBalanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
