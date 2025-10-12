package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.ResponseFactory;
import com.iseeyou.fortunetelling.dto.request.auth.EmailVerificationRequest;
import com.iseeyou.fortunetelling.dto.request.auth.ForgotPasswordRequest;
import com.iseeyou.fortunetelling.dto.request.auth.ResendOtpRequest;
import com.iseeyou.fortunetelling.dto.request.auth.ResetPasswordRequest;
import com.iseeyou.fortunetelling.dto.request.auth.SeerRegisterRequest;
import com.iseeyou.fortunetelling.dto.response.user.UserResponse;
import com.iseeyou.fortunetelling.service.email.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.iseeyou.fortunetelling.dto.request.auth.LoginRequest;
import com.iseeyou.fortunetelling.dto.request.auth.RegisterRequest;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.SuccessResponse;
import com.iseeyou.fortunetelling.dto.response.auth.TokenResponse;
import com.iseeyou.fortunetelling.dto.response.error.DetailedErrorResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.service.auth.AuthService;
import com.iseeyou.fortunetelling.service.user.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "001. Auth", description = "Auth API")
public class AuthController {
    private final AuthService authService;

    private final UserService userService;

    private final ResponseFactory responseFactory;

    private final EmailVerificationService emailVerificationService;


    @PostMapping("/login")
    @Operation(
            summary = "Login endpoint",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TokenResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Bad credentials",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = DetailedErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<TokenResponse>> login(
            @Parameter(description = "Request body to login", required = true)
            @RequestBody @Validated final LoginRequest request
    ) {
        TokenResponse tokenResponse = authService.login(request.getEmail(), request.getPassword(), false);
        return responseFactory.successSingle(tokenResponse, "Login successful");
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register endpoint",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SuccessResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = DetailedErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<SuccessResponse>> register(
            @Parameter(description = "Request body to register", required = true)
            @RequestBody @Valid RegisterRequest request
    ) throws BindException {
        userService.register(request);
        return responseFactory.successSingle(null, "Register successful, an OTP has been sent to your email for verification");
    }

    @PostMapping(path = "/seer/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Register a new seer user",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Seer registered successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)) ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)) )
            }
    )
    public ResponseEntity<SingleResponse<SuccessResponse>> seerRegister(
            @Parameter(description = "Seer register data (multipart/form-data)", required = true)
            @Valid @ModelAttribute SeerRegisterRequest request,
            BindingResult bindingResult
    ) throws BindException, IOException {

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        userService.seerRegister(request);

        return responseFactory.successSingle(null, "Seer registered successfully");
    }


    @GetMapping("/refresh")
    @Operation(
            summary = "Refresh endpoint",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TokenResponse.class)
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
                            description = "Bad credentials",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<TokenResponse>> refresh(
            @Parameter(description = "Refresh token", required = true)
            @RequestHeader("Authorization") @Validated final String refreshToken
    ) {
        TokenResponse tokenResponse = authService.refreshFromBearerString(refreshToken);
        return responseFactory.successSingle(tokenResponse, "Refresh successful");
    }

    @GetMapping("/logout")
    @Operation(
            summary = "Logout endpoint",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SuccessResponse.class)
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
                            description = "Bad request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<SuccessResponse>> logout(
            @Parameter(description = "Firebase token (optional)")
            @RequestParam(required = false) String firebaseToken
    ) {

        User user = userService.getUser();

        authService.logout(user);

        return responseFactory.successSingle(null, "Logout successful");
    }

    @PostMapping("/verify-email")
    @Operation(
            summary = "Verify email with OTP",
            description = "Verify email address using OTP code sent to user's email",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Email verified successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SuccessResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid or expired OTP",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = DetailedErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<SuccessResponse>> verifyEmail(
            @Parameter(description = "Email verification request with email and OTP", required = true)
            @RequestBody @Valid EmailVerificationRequest request
    ) {
        boolean isValid = emailVerificationService.verifyOtp(request.getEmail(), request.getOtpCode());

        if (!isValid) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã hết hạn");
        }

        // Kích hoạt tài khoản user
        userService.activateUserByEmail(request.getEmail());

        return responseFactory.successSingle(null, "Email verified successfully");
    }

    @PostMapping("/resend-otp")
    @Operation(
            summary = "Resend OTP to email",
            description = "Resend OTP verification code to user's email address",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OTP sent successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SuccessResponse.class)
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
                            responseCode = "422",
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = DetailedErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<SuccessResponse>> resendOtp(
            @Parameter(description = "Request with email to resend OTP", required = true)
            @RequestBody @Valid ResendOtpRequest request
    ) {
        emailVerificationService.sendVerificationEmail(request.getEmail());
        return responseFactory.successSingle(null, "OTP sent successfully to your email");
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Send OTP for password reset",
            description = "Send OTP to email for password reset. Email must belong to an existing user.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OTP sent successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SuccessResponse.class)
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
                            responseCode = "422",
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = DetailedErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<SuccessResponse>> forgotPassword(
            @Parameter(description = "Email to send password reset OTP", required = true)
            @RequestBody @Valid ForgotPasswordRequest request
    ) {
        // Kiểm tra email có tồn tại không
        userService.findByEmail(request.getEmail());

        // Gửi OTP reset password
        emailVerificationService.sendPasswordResetEmail(request.getEmail());

        return responseFactory.successSingle(null, "OTP for password reset sent successfully to your email");
    }

    @PostMapping("/forgot-password/verify")
    @Operation(
            summary = "Reset password with OTP",
            description = "Reset user password using OTP verification. Requires email, OTP, new password and confirm password.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Password reset successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SuccessResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid OTP, passwords don't match, or other validation errors",
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
                            responseCode = "422",
                            description = "Validation failed",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = DetailedErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<SuccessResponse>> resetPassword(
            @Parameter(description = "Password reset request with email, OTP and new password", required = true)
            @RequestBody @Valid ResetPasswordRequest request
    ) {
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu và xác nhận mật khẩu không khớp");
        }

        // Verify OTP
        boolean isValidOtp = emailVerificationService.verifyOtp(request.getEmail(), request.getOtpCode());
        if (!isValidOtp) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã hết hạn");
        }

        // Reset password
        userService.resetPassword(request.getEmail(), request.getPassword());

        return responseFactory.successSingle(null, "Password reset successfully");
    }
}
