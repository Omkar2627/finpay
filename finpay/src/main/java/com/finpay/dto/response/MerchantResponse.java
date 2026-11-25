package com.finpay.dto.response;

import com.finpay.entity.MerchantStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MerchantResponse {
    private Long id;
    private String merchantNumber;
    private String businessName;
    private String businessType;
    private String contactEmail;
    private String contactMobile;
    private MerchantStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
