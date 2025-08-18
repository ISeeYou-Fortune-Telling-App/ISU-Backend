package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.ResponseFactory;
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
import com.iseeyou.fortunetelling.service.MessageSourceService;
import com.iseeyou.fortunetelling.service.auth.AuthService;
import com.iseeyou.fortunetelling.service.user.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "001. Auth", description = "Auth API")
public class AuthController {
    private final AuthService authService;

    private final UserService userService;

    private final MessageSourceService messageSourceService;
    private final ResponseFactory responseFactory;


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
        return responseFactory.successSingle(null, "Register successful");
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
}
