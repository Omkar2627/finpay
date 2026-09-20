package com.finpay.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "FinPay API",
                version = "1.0",
                description = "Digital wallet and payment system API"
        )
)
public class OpenApiConfig {
}
