package com.finpay.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotBlank(message = "Receiver wallet number is required")
    @Size(max = 20, message = "Invalid receiver wallet number")
    private String receiverWalletNumber;

    @NotNull(message = "Transfer amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Transfer amount must be greater than zero"
    )
    @Digits(
            integer = 17,
            fraction = 2,
            message = "Amount must have at most 2 decimal places"
    )
    private BigDecimal amount;

    @Size(
            max = 255,
            message = "Description cannot exceed 255 characters"
    )
    private String description;

}
