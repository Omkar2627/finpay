package com.finpay.service;

import com.finpay.dto.request.CreateMerchantRequest;
import com.finpay.dto.request.UpdateMerchantRequest;
import com.finpay.dto.response.MerchantResponse;

public interface MerchantService {
    MerchantResponse registerMerchant(CreateMerchantRequest request);

    MerchantResponse getMyMerchant();

    MerchantResponse updateMyMerchant(UpdateMerchantRequest request);
}
