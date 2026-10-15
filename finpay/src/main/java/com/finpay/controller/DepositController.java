package com.finpay.controller;


import com.finpay.dto.request.DepositRequest;
import com.finpay.dto.response.DepositResponse;
import com.finpay.service.DepositService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DepositController {

    private final DepositService depositService;

    @PostMapping("/me/deposit")
    public ResponseEntity<DepositResponse> deposit(@Valid @RequestBody DepositRequest request) {
        DepositResponse response = depositService.deposit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }


}
