package com.finpay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "merchant_wallets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_merchant_wallet_merchant",
                        columnNames = "merchant_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class MerchantWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "merchant_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_merchant_wallet")
    )
    private Merchant merchant;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
