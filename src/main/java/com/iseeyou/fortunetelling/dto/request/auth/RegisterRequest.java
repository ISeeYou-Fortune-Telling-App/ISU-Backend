package com.iseeyou.fortunetelling.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class RegisterRequest {
    @NotBlank(message = "{not_blank}")
    @Email(message = "{invalid_email}")
    @Size(max = 100, message = "{max_length}")
    @Schema(
            name = "email",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "mail@email.com"
    )
    private String email;

    @Size(max = 20, message = "{max_length}")
    @Schema(
            name = "phoneNumber",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "0901234567"
    )
    private String phoneNumber;

    @Size(max = 10, message = "{max_length}")
    @Schema(
            name = "gender",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "Nam"
    )
    private String gender;

    @NotBlank(message = "{not_blank}")
    @Schema(
            name = "password",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "P@sswd123."
    )
    private String password;

    @NotBlank(message = "{not_blank}")
    @Schema(
            name = "passwordConfirm",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "P@sswd123."
    )
    private String passwordConfirm;

    @NotBlank(message = "{not_blank}")
    @Size(max = 50, message = "{max_length}")
    @Schema(
            name = "fullName",
            description = "Full name of the user",
            type = "String",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Nguyễn Văn A"
    )
    private String fullName;

    @Schema(
            name = "birthDate",
            description = "Birth date of the user",
            type = "string",
            format = "date-time",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "30-01-2000T00:00:00"
    )
    private LocalDateTime birthDate;

}