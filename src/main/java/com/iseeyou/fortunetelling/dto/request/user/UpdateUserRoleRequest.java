package com.iseeyou.fortunetelling.dto.request.user;

import com.iseeyou.fortunetelling.util.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update user permissions")
public class UpdateUserRoleRequest {
    @NotBlank(message = "Role cannot be empty")
    @Schema(description = "New role for the user", example = "CUSTOMER",
            allowableValues = {"ADMIN", "SEER", "UNVERIFIED_SEER", "GUEST", "CUSTOMER"})
    private Constants.RoleEnum role;
}
