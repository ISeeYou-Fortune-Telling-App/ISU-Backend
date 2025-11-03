package com.iseeyou.fortunetelling.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePaypalEmailRequest {
    @NotBlank(message = "PayPal email is required")
    @Email(message = "Invalid email format")
    private String paypalEmail;
}

