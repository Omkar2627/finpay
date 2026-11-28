package com.finpay.controller;


import com.finpay.dto.request.RefundRequest;
import com.finpay.dto.response.RefundResponse;
import com.finpay.service.RefundService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant-payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/{paymentReference}/refunds")
    public ResponseEntity<RefundResponse> initiateRefund(
            @PathVariable String paymentReference,
            @Valid @RequestBody RefundRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        RefundResponse response =
                refundService.initiateRefund(
                        paymentReference,
                        request,
                        idempotencyKey
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
