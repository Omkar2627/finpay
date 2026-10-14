package com.finpay.controller;


import com.finpay.dto.response.TransactionResponse;
import com.finpay.service.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("/me")
    public ResponseEntity<List<TransactionResponse>> getMyTransaction() {
        return ResponseEntity.ok(transactionService.getMyTransactions());
    }

    @GetMapping("/{transactionReference}")
    public ResponseEntity<TransactionResponse> getTransactionByReference(@PathVariable String transactionReference) {
        return ResponseEntity.ok(transactionService.getMyTransactionByReference(transactionReference));
    }
}
