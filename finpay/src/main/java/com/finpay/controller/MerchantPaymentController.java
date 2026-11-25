package com.finpay.controller;


import com.finpay.dto.request.MerchantPaymentRequest;
import com.finpay.dto.response.MerchantPaymentResponse;
import com.finpay.service.MerchantPaymentService;
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
public class MerchantPaymentController {

    private final MerchantPaymentService merchantPaymentService;

    @PostMapping("/{merchantNumber}/payments")
    public ResponseEntity<MerchantPaymentResponse> makePayment(
            @PathVariable String merchantNumber,
            @Valid @RequestBody MerchantPaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        MerchantPaymentResponse response =
                merchantPaymentService.makePayment(
                        merchantNumber,
                        request,
                        idempotencyKey
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}