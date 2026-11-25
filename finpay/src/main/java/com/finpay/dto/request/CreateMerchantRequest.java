package com.finpay.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateMerchantRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 150, message = "Business name cannot exceed 150 characters")
    private String businessName;

    @NotBlank(message = "Business type is required")
    @Size(max = 50, message = "Business type cannot exceed 50 characters")
    private String businessType;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid contact email format")
    @Size(max = 150, message = "Contact email cannot exceed 150 characters")
    private String contactEmail;

    @NotBlank(message = "Contact mobile is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Mobile number must contain 10 to 15 digits")
    private String contactMobile;

}
