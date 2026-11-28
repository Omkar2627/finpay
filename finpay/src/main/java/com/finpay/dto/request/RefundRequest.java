package com.finpay.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class RefundRequest {

    @NotNull(message = "Refund amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Refund amount must be greater than zero"
    )
    @Digits(
            integer = 17,
            fraction = 2,
            message = "Refund amount must have at most 2 decimal places"
    )
    private BigDecimal amount;

    @NotBlank(message = "Refund reason is required")
    @Size(
            max = 255,
            message = "Refund reason cannot exceed 255 characters"
    )
    private String reason;
}
