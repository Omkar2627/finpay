package com.finpay.mapper;

import com.finpay.dto.request.CreateMerchantRequest;
import com.finpay.dto.request.UpdateMerchantRequest;
import com.finpay.dto.response.MerchantResponse;
import com.finpay.entity.Merchant;
import org.springframework.stereotype.Component;

@Component
public class MerchantMapper {
    public Merchant toEntity(CreateMerchantRequest request) {

        Merchant merchant = new Merchant();
        merchant.setBusinessName(request.getBusinessName());
        merchant.setBusinessType(request.getBusinessType());
        merchant.setContactEmail(request.getContactEmail());
        merchant.setContactMobile(request.getContactMobile());
        return merchant;
    }

    public void updateEntity(Merchant merchant, UpdateMerchantRequest request) {
        if (request.getBusinessName() != null) {
            merchant.setBusinessName(request.getBusinessName().trim());
        }
        if (request.getBusinessType() != null) {
            merchant.setBusinessType(request.getBusinessType().trim());
        }
        if (request.getContactEmail() != null) {
            merchant.setContactEmail(request.getContactEmail().trim());
        }
        if (request.getContactMobile() != null) {
            merchant.setContactMobile(request.getContactMobile().trim());
        }
    }

    public MerchantResponse toResponse(Merchant merchant) {

        return new MerchantResponse(
                merchant.getId(),
                merchant.getMerchantNumber(),
                merchant.getBusinessName(),
                merchant.getBusinessType(),
                merchant.getContactEmail(),
                merchant.getContactMobile(),
                merchant.getStatus(),
                merchant.getCreatedAt(),
                merchant.getUpdatedAt()
        );
    }
}
