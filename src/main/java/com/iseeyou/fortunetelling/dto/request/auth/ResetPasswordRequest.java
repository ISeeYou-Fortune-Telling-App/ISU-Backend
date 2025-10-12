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
@Schema(description = "Reset password request")
public class ResetPasswordRequest {

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must be valid")
    @Schema(description = "Email address", example = "user@example.com")
    private String email;

    @NotBlank(message = "OTP code cannot be empty")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
    @Schema(description = "6-digit OTP code", example = "123456")
    private String otpCode;

    @NotBlank(message = "New password cannot be empty")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
             message = "Password must contain at least 8 characters, one uppercase, one lowercase, one digit and one special character")
    @Schema(description = "New password", example = "NewPassword123!")
    private String password;

    @NotBlank(message = "Confirm password cannot be empty")
    @Schema(description = "Confirm new password", example = "NewPassword123!")
    private String confirmPassword;
}
