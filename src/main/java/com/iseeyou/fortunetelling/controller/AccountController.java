package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.user.UpdatePaypalEmailRequest;
import com.iseeyou.fortunetelling.dto.request.user.UpdateUserRoleRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.account.AccountStatsResponse;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.mapper.UserMapper;
import com.iseeyou.fortunetelling.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.iseeyou.fortunetelling.dto.request.user.UpdateUserRequest;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.dto.response.user.SimpleUserResponse;
import com.iseeyou.fortunetelling.dto.response.user.UserResponse;
import com.iseeyou.fortunetelling.service.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
@Tag(name = "002. Account", description = "Account API")
@Slf4j
public class AccountController extends AbstractBaseController {
    private final UserService userService;
    private final UserMapper userMapper;


    @GetMapping("/me")
    @Operation(
            summary = "Me endpoint",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Bad credentials",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<UserResponse>> me() {
        User currentUser = userService.getUser();
        // Nếu là seer, lấy thêm thống kê booking
        if (currentUser.getRole() == Constants.RoleEnum.SEER) {
            currentUser = userService.getUserWithSeerStats(currentUser.getId());
        }
        UserResponse userResponse = userMapper.mapTo(currentUser, UserResponse.class);
        return responseFactory.successSingle(userResponse, "Successful operation");
    }

    @PatchMapping("/me")
    @Operation(
            summary = "Update current user endpoint",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<UserResponse>> updateMe(
            @Parameter(description = "Request body to update current user", required = true)
            @RequestBody @Valid final UpdateUserRequest request
    ) throws BindException {
        UserResponse updatedUser = userMapper.mapTo(userService.updateMe(request), UserResponse.class);
        return responseFactory.successSingle(updatedUser, "User updated successfully");
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload avatar for current user",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Avatar file to upload",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Avatar uploaded successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<String>> uploadMeAvatar(
            @Parameter(description = "Avatar file to upload", required = true)
            @RequestParam("avatar") final MultipartFile avatar
    ) throws Exception {
        String avatarUrl = userService.uploadImage(avatar, "avatars");
        return responseFactory.successSingle(avatarUrl, "Avatar uploaded successfully");
    }

    @PostMapping(value = "/me/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload cover for current user",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Cover file to upload",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Cover uploaded successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<String>> uploadMeCover(
            @Parameter(description = "Cover file to upload", required = true)
            @RequestParam("cover") final MultipartFile cover
    ) throws Exception {
        String coverUrl = userService.uploadImage(cover, "covers");
        return responseFactory.successSingle(coverUrl, "Cover uploaded successfully");
    }

    @GetMapping
    @Operation(
            summary = "Get all users with pagination, filters and search",
            description = "Get all users with optional filters (role, status) and search by name or email",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )

    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @Parameter(description = "Search keyword (name or email)", required = false)
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by role (GUEST, CUSTOMER, SEER, UNVERIFIED_SEER, ADMIN)", required = false)
            @RequestParam(required = false) String role,
            @Parameter(description = "Filter by status (ACTIVE, INACTIVE, VERIFIED, UNVERIFIED, BLOCKED)", required = false)
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number", required = false)
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Items per page", required = false)
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort type (asc/desc)", required = false)
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort by field", required = false)
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<User> userPage = userService.findAllWithFilters(keyword, role, status, pageable);
        Page<UserResponse> response = userMapper.mapToPage(userPage, UserResponse.class);

        String message = (keyword != null && !keyword.trim().isEmpty())
                ? "Search results retrieved successfully"
                : "Users retrieved successfully";

        return responseFactory.successPage(response, message);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<UserResponse<?>>> getUserById(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID id
    ) {
        User user = userService.findById(id);
        // Kiểm tra xem người dùng có phải là seer không và lấy thống kê nếu có
        if (user.getRole() == Constants.RoleEnum.SEER) {
            user = userService.getUserWithSeerStats(user.getId());
        }
        UserResponse<?> userResponse = userMapper.mapTo(user, UserResponse.class);
        return responseFactory.successSingle(userResponse, "User retrieved successfully");
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Update user status",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User status updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SingleResponse<UserResponse<?>>> updateUserStatus(
            @Parameter(description = "User ID", required = true)
            @PathVariable String id,
            @Parameter(description = "New status", required = true)
            @RequestParam String status
    ) {
        User updatedUser = userService.updateStatus(UUID.fromString(id), status);
        UserResponse<?> userResponse = userMapper.mapTo(updatedUser, UserResponse.class);
        return responseFactory.successSingle(userResponse, "User status updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete user account (Admin only)",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Account blocked successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Admin access required",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SingleResponse<String>> deleteAccount(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID id
    ) {
        userService.delete(String.valueOf(id));
        return responseFactory.successSingle(null, "Account blocked successfully");
    }

    @PatchMapping("/{id}/role")
    @Operation(
            summary = "Update user permissions (Admin only)",
            description = "Update user's role to grant or revoke permissions. Transitions: " +
                    "GUEST → CUSTOMER/SEER, CUSTOMER ↔ SEER, SEER/CUSTOMER → ADMIN",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User role updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request - Invalid role or invalid role transition",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Admin access required",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SingleResponse<UserResponse<?>>> updateUserRole(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Role update request", required = true)
            @RequestBody @Valid UpdateUserRoleRequest request
    ) throws BindException {
        User updatedUser = userService.updateUserRole(id, request);
        UserResponse<?> userResponse = userMapper.mapTo(updatedUser, UserResponse.class);
        return responseFactory.successSingle(userResponse, "User role updated successfully");
    }

    @GetMapping("/stats")
    @Operation(
            summary = "Get account statistics",
            description = "Get statistics of all accounts including total accounts, customers, seers, pending accounts, and blocked accounts",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Account statistics retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Admin access required",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SingleResponse<AccountStatsResponse>> getAccountStats() {
        AccountStatsResponse stats = userService.getAccountStats();
        return responseFactory.successSingle(stats, "Account statistics retrieved successfully");
    }

    @PutMapping("/me/paypal-email")
    @Operation(
            summary = "Update PayPal email for seer",
            description = "Seer can update their PayPal email address for receiving payments. " +
                    "Only seers can use this endpoint.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "PayPal email updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request - Invalid email format or user is not a seer",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Only seers can update PayPal email",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAnyAuthority('SEER', 'UNVERIFIED_SEER')")
    public ResponseEntity<SingleResponse<UserResponse>> updatePaypalEmail(
            @Parameter(description = "PayPal email update request", required = true)
            @RequestBody @Valid UpdatePaypalEmailRequest request
    ) {
        log.info("Updating PayPal email for seer");
        User updatedUser = userService.updatePaypalEmail(request);
        UserResponse userResponse = userMapper.mapTo(updatedUser, UserResponse.class);
        return responseFactory.successSingle(userResponse, "PayPal email updated successfully");
    }

    @GetMapping("/simple")
    @Operation(
            summary = "Get simple user by ID",
            description = "Get basic user information (id, name, avatarUrl) by user ID",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SimpleUserResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<SimpleUserResponse>> getSimpleUserById(
            @Parameter(description = "User ID", required = true)
            @RequestParam UUID userId
    ) {
        User user = userService.getSimpleUserById(userId);
        SimpleUserResponse simpleUserResponse = SimpleUserResponse.builder()
                .id(user.getId())
                .name(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
        return responseFactory.successSingle(simpleUserResponse, "User retrieved successfully");
    }
}


