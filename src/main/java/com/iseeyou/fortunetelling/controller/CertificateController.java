package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.certificate.CertificateCreateRequest;
import com.iseeyou.fortunetelling.dto.request.certificate.CertificateUpdateRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.certificate.CertificateResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.entity.certificate.Certificate;
import com.iseeyou.fortunetelling.mapper.CertificateMapper;
import com.iseeyou.fortunetelling.service.certificate.CertificateService;
import com.iseeyou.fortunetelling.service.fileupload.CloudinaryService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/certificates")
@Tag(name = "004. Certificate", description = "Certificate API")
@Slf4j
public class CertificateController extends AbstractBaseController {
    private final CertificateService certificateService;
    private final CertificateMapper certificateMapper;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    @Operation(
            summary = "Get all certificates with pagination",
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
    public ResponseEntity<PageResponse<CertificateResponse>> getAllCertificates(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<Certificate> certificates = certificateService.findAll(pageable);
        Page<CertificateResponse> response = certificateMapper.mapToPage(certificates, CertificateResponse.class);
        return responseFactory.successPage(response, "Certificates retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get certificate by ID",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Certificate.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Certificate not found",
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
    public ResponseEntity<SingleResponse<CertificateResponse>> getCertificateById(
            @Parameter(description = "Certificate ID", required = true)
            @PathVariable UUID id
    ) {
        Certificate certificate = certificateService.findById(id);
        CertificateResponse response = certificateMapper.mapTo(certificate, CertificateResponse.class);
        return responseFactory.successSingle(response, "Certificate retrieved successfully");
    }

    @PostMapping
    @Operation(
            summary = "Create a new certificate",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Certificate created successfully",
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
    @PreAuthorize("hasAnyAuthority('SEER', 'ADMIN')")
    public ResponseEntity<SingleResponse<CertificateResponse>> createCertificate(
            @Parameter(description = "Certificate data to create", required = true)
            @ModelAttribute @Valid CertificateCreateRequest request
    ) throws IOException {
        Certificate certificateToCreate = certificateMapper.mapTo(request, Certificate.class);
        String imageUrl = cloudinaryService.uploadFile(request.getCertificateFile(), "certificates");
        certificateToCreate.setCertificateUrl(imageUrl);
        Certificate createdCertificate = certificateService.create(certificateToCreate, request.getCategoryIds());
        CertificateResponse response = certificateMapper.mapTo(createdCertificate, CertificateResponse.class);
        return responseFactory.successSingle(response, "Certificate created successfully");
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update certificate",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Certificate updated successfully",
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
                            responseCode = "404",
                            description = "Certificate not found",
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
    public ResponseEntity<SingleResponse<CertificateResponse>> updateCertificate(
            @Parameter(description = "Certificate ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated certificate data", required = true)
            @ModelAttribute @Valid CertificateUpdateRequest request
    ) throws IOException {
        Certificate certificateToUpdate = certificateMapper.mapTo(request, Certificate.class);
        Certificate updatedCertificate = certificateService.update(id, certificateToUpdate, request.getCategoryIds());
        CertificateResponse response = certificateMapper.mapTo(updatedCertificate, CertificateResponse.class);
        return responseFactory.successSingle(response, "Certificate updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete certificate",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Certificate deleted successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Certificate not found",
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
    public ResponseEntity<SingleResponse<Void>> deleteCertificate(
            @Parameter(description = "Certificate ID", required = true)
            @PathVariable UUID id
    ) throws IOException {
        certificateService.delete(id);
        return responseFactory.successSingle(null, "Certificate deleted successfully");
    }

    @GetMapping("/by-user/{userId}")
    @Operation(
            summary = "Find certificates by user ID",
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
    public ResponseEntity<PageResponse<CertificateResponse>> getCertificatesByUserId(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<Certificate> certificates = certificateService.findByUserId(userId, pageable);
        Page<CertificateResponse> response = certificateMapper.mapToPage(certificates, CertificateResponse.class);
        return responseFactory.successPage(response, "Certificates retrieved successfully");
    }

    @GetMapping("/by-category/{categoryId}")
    @Operation(
            summary = "Find certificates by category ID",
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
    public ResponseEntity<PageResponse<CertificateResponse>> getCertificatesByCategoryId(
            @Parameter(description = "Category ID", required = true)
            @PathVariable UUID categoryId,
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<Certificate> certificates = certificateService.findByCategoryId(categoryId, pageable);
        Page<CertificateResponse> response = certificateMapper.mapToPage(certificates, CertificateResponse.class);
        return responseFactory.successPage(response, "Certificates retrieved successfully");
    }

    @GetMapping("/by-user/{userId}/category/{categoryId}")
    @Operation(
            summary = "Find certificates by user ID and category ID",
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
    public ResponseEntity<PageResponse<CertificateResponse>> getCertificatesByUserIdAndCategoryId(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId,
            @Parameter(description = "Category ID", required = true)
            @PathVariable UUID categoryId,
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<Certificate> certificates = certificateService.findByUserIdAndCategoryId(userId, categoryId, pageable);
        Page<CertificateResponse> response = certificateMapper.mapToPage(certificates, CertificateResponse.class);
        return responseFactory.successPage(response, "Certificates retrieved successfully");
    }
}