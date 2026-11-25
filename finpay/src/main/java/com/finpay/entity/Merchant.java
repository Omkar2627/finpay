package com.finpay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchant", uniqueConstraints = {@UniqueConstraint(name = "uk_merchant_number", columnNames = "merchant_number"),
        @UniqueConstraint(name = "uk_merchant_owner", columnNames = "owner_user_id")})
@Getter
@Setter
@NoArgsConstructor
public class Merchant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_number", nullable = false, length = 20)
    private String merchantNumber;

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType;

    @Column(name = "contact_email", nullable = false, length = 150)
    private String contactEmail;

    @Column(name = "contact_mobile", nullable = false, length = 15)
    private String contactMobile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_merchant_owner"))
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MerchantStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

