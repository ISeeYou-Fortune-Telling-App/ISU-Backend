package com.iseeyou.fortunetelling.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "{not_blank}")
    @Email(message = "{invalid_email}")
    @Schema(
            name = "email",
            description = "E-mail of the user",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "khachhang@iseeyou.com"
    )
    private String email;

    @NotBlank(message = "{not_blank}")
    @Schema(
            name = "password",
            description = "Password of the user",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "P@sswd123."
    )
    private String password;

    @Schema(
            name = "fcmToken",
            description = "FCM Token from device, for push notification",
            type = "String",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String fcmToken;
}