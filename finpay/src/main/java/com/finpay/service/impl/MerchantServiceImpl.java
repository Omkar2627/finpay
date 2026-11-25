package com.finpay.service.impl;

import com.finpay.dto.request.CreateMerchantRequest;
import com.finpay.dto.request.UpdateMerchantRequest;
import com.finpay.dto.response.MerchantResponse;
import com.finpay.entity.*;
import com.finpay.exception.MerchantAlreadyExistsException;
import com.finpay.exception.MerchantNotFoundException;
import com.finpay.exception.UserNotFoundException;
import com.finpay.mapper.MerchantMapper;
import com.finpay.repository.MerchantRepository;
import com.finpay.repository.MerchantWalletRepository;
import com.finpay.repository.UserRepository;
import com.finpay.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {


    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final MerchantMapper merchantMapper;
    private final MerchantWalletRepository merchantWalletRepository;

    @Override
    @Transactional
    public MerchantResponse registerMerchant(
            CreateMerchantRequest request) {

        // 1. Identify the authenticated user from JWT.
        User owner = getAuthenticatedUser();

        // 2. Check whether the user already owns a merchant profile.
        if (merchantRepository.existsByOwner_Id(owner.getId())) {
            throw new MerchantAlreadyExistsException(
                    "A merchant profile already exists for this user"
            );
        }

        // 3. Convert request DTO into Merchant entity.
        Merchant merchant = merchantMapper.toEntity(request);

        // 4. Generate a unique merchant number.
        merchant.setMerchantNumber(generateMerchantNumber());

        // 5. Associate merchant with authenticated user.
        merchant.setOwner(owner);

        // 6. New merchant must await admin approval.
        merchant.setStatus(MerchantStatus.PENDING);

        // 7. Set timestamps.
        LocalDateTime now = LocalDateTime.now();

        merchant.setCreatedAt(now);
        merchant.setUpdatedAt(now);

        // 8. Save merchant.
        Merchant savedMerchant =
                merchantRepository.save(merchant);

        // 9. Automatically create merchant wallet.
        MerchantWallet merchantWallet = new MerchantWallet();

        merchantWallet.setMerchant(savedMerchant);
        merchantWallet.setBalance(BigDecimal.ZERO);
        merchantWallet.setCurrency("INR");

        // Merchant wallet remains inactive until approval.
        merchantWallet.setStatus(WalletStatus.INACTIVE);

        merchantWallet.setCreatedAt(now);
        merchantWallet.setUpdatedAt(now);

        merchantWalletRepository.save(merchantWallet);

        // 10. Return merchant response.
        return merchantMapper.toResponse(savedMerchant);
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
                    "An authenticated user is required"
            );
        }
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Authenticated user not found"
                        )
                );

    }

    @Override
    @Transactional(readOnly = true)
    public MerchantResponse getMyMerchant() {

        // 1. Get authenticated user from JWT.
        User owner = getAuthenticatedUser();

        // 2. Find merchant belonging to this user.
        Merchant merchant = merchantRepository
                .findByOwner_Id(owner.getId())
                .orElseThrow(() ->
                        new MerchantNotFoundException(
                                "Merchant profile not found for current user"
                        )
                );

        // 3. Convert entity into response DTO.
        return merchantMapper.toResponse(merchant);
    }

    @Override
    @Transactional
    public MerchantResponse updateMyMerchant(
            UpdateMerchantRequest request) {

        // 1. Get authenticated user from JWT.
        User owner = getAuthenticatedUser();

        // 2. Find merchant belonging to this user.
        Merchant merchant = merchantRepository
                .findByOwner_Id(owner.getId())
                .orElseThrow(() ->
                        new MerchantNotFoundException(
                                "Merchant profile not found for current user"
                        )
                );

        // 3. Ensure at least one field is provided.
        if (request.getBusinessName() == null
                && request.getBusinessType() == null
                && request.getContactEmail() == null
                && request.getContactMobile() == null) {

            throw new IllegalArgumentException(
                    "At least one field must be provided for update"
            );
        }

        // 4. Update only the supplied fields.
        merchantMapper.updateEntity(merchant, request);

        // 5. Update timestamp.
        merchant.setUpdatedAt(LocalDateTime.now());

        // 6. Save updated merchant.
        Merchant updatedMerchant =
                merchantRepository.save(merchant);

        // 7. Return response DTO.
        return merchantMapper.toResponse(updatedMerchant);
    }

    private String generateMerchantNumber() {

        String merchantNumber;

        do {
            merchantNumber = "MER-"
                    + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();
        } while (
                merchantRepository.existsByMerchantNumber(
                        merchantNumber
                )
        );

        return merchantNumber;
    }
}

