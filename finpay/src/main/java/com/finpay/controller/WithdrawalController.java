package com.finpay.controller;

import com.finpay.dto.request.WithdrawalRequest;
import com.finpay.dto.response.WithdrawalResponse;
import com.finpay.service.WithdrawalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping("/me/withdraw")
    public ResponseEntity<WithdrawalResponse> withdrawa(@Valid @RequestBody WithdrawalRequest request) {
        WithdrawalResponse response = withdrawalService.withdraw(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
