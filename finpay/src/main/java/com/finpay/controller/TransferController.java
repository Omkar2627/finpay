package com.finpay.controller;

import com.finpay.dto.request.TransferRequest;
import com.finpay.dto.response.TransferResponse;
import com.finpay.service.TransferService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TransferController {
    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        TransferResponse response =
                transferService.transfer(request, idempotencyKey);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
