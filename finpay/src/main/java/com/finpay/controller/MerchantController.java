package com.finpay.controller;


import com.finpay.dto.request.CreateMerchantRequest;
import com.finpay.dto.request.UpdateMerchantRequest;
import com.finpay.dto.response.MerchantResponse;
import com.finpay.service.MerchantService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchants")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping
    public ResponseEntity<MerchantResponse> registerMerchant(
            @Valid @RequestBody CreateMerchantRequest request) {

        MerchantResponse response =
                merchantService.registerMerchant(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<MerchantResponse> getMyMerchant() {

        MerchantResponse response =
                merchantService.getMyMerchant();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<MerchantResponse> updateMyMerchant(
            @Valid @RequestBody UpdateMerchantRequest request) {

        MerchantResponse response =
                merchantService.updateMyMerchant(request);

        return ResponseEntity.ok(response);
    }
}
