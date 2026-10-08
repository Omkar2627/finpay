package com.finpay.controller;

import com.finpay.dto.response.WalletResponse;
import com.finpay.service.WalletService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(){
        WalletResponse response =  walletService.createWallet();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<WalletResponse> getMyWallet(){
        WalletResponse response = walletService.getMyWallet();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/balance")
    public ResponseEntity<BigDecimal> getMyBalance(){
       BigDecimal  balance = walletService.getMyBalance();
        return ResponseEntity.ok(balance);
    }
}
