package com.iseeyou.fortunetelling.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Email verification request")
public class EmailVerificationRequest {

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must be valid")
    @Schema(description = "Email address to verify", example = "user@example.com")
    private String email;

    @NotBlank(message = "OTP code cannot be empty")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
    @Schema(description = "6-digit OTP code", example = "123456")
    private String otpCode;
}
