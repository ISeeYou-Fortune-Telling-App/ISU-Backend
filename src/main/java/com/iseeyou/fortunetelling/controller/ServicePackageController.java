package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.servicepackage.PackageInteractionRequest;
import com.iseeyou.fortunetelling.dto.request.servicepackage.ServicePackageConfirmRequest;
import com.iseeyou.fortunetelling.dto.request.servicepackage.ServicePackageUpsertRequest;
import com.iseeyou.fortunetelling.dto.request.servicepackage.ServiceReviewRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.dto.response.servicepackage.ServicePackagePageResponse;
import com.iseeyou.fortunetelling.dto.response.servicepackage.ServicePackageResponse;
import com.iseeyou.fortunetelling.service.servicepackage.impl.ServicePackageServiceImpl;
import com.iseeyou.fortunetelling.dto.response.ServicePackageDetailResponse;
import com.iseeyou.fortunetelling.dto.response.servicepackage.ServiceReviewResponse;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.entity.servicepackage.ServiceReview;
import com.iseeyou.fortunetelling.mapper.ServicePackageMapper;
import com.iseeyou.fortunetelling.mapper.ServiceReviewMapper;
import com.iseeyou.fortunetelling.service.servicepackage.ServicePackageService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/service-packages")
@Tag(name = "006. Service Packages", description = "Service Packages API")
@Slf4j
public class ServicePackageController extends AbstractBaseController {

    private final ServicePackageService servicePackageService;
    private final ServicePackageMapper servicePackageMapper;
    private final ServiceReviewMapper serviceReviewMapper;

    @GetMapping("/detail")
    @Operation(
            summary = "Get service package detail with seer information",
            description = "Get detailed service package information including seer profile and rating",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ServicePackageDetailResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Service package not found",
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
    public ResponseEntity<SingleResponse<ServicePackageDetailResponse>> getServicePackageDetail(
            @Parameter(description = "Service Package ID", required = true)
            @RequestParam String id
    ) {
        ServicePackageDetailResponse response = servicePackageService.findDetailById(id);
        return responseFactory.successSingle(response, "Service package detail retrieved successfully");
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Create new service package",
        description = "Create a new service package with multiple categories, optional image upload, price, duration. " +
                     "Status is HIDDEN (pending approval). SeerId is automatically extracted from JWT. " +
                     "Provide categoryIds as a list of knowledge category IDs.",
        security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    @PreAuthorize("hasAuthority('SEER')")
    public ResponseEntity<SingleResponse<ServicePackageResponse>> createServicePackage(
            @Valid @ModelAttribute ServicePackageUpsertRequest request
    ) throws IOException {
        log.info("=== START: Creating service package with title: {}, categories: {}",
                request.getPackageTitle(), request.getCategoryIds());

        ServicePackage servicePackage = servicePackageService.createOrUpdatePackage(request);

        log.info("=== Service package created with ID: {}", servicePackage.getId());

        ServicePackageResponse response = servicePackageMapper.mapTo(servicePackage, ServicePackageResponse.class);

        log.info("=== END: Mapped to response, returning...");

        return responseFactory.successSingle(response, "Service package created successfully");
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Update service package",
        description = "Update an existing service package with multiple categories. " +
                     "Status remains HIDDEN if not approved. SeerId is automatically extracted from JWT. " +
                     "Provide categoryIds as a list of knowledge category IDs. " +
                     "Admin can also update commissionRate (optional field).",
        security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    @PreAuthorize("hasAnyAuthority('SEER', 'ADMIN')")
    public ResponseEntity<SingleResponse<ServicePackageResponse>> updateServicePackage(
            @Parameter(description = "Service Package ID", required = true)
            @RequestParam String id,
            @Valid @ModelAttribute ServicePackageUpsertRequest request
    ) throws IOException {
        ServicePackage servicePackage = servicePackageService.createOrUpdatePackage(id, request);
        ServicePackageResponse response = servicePackageMapper.mapTo(servicePackage, ServicePackageResponse.class);
        return responseFactory.successSingle(response, "Service package updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete service package (Soft Delete with Auto Refund)",
            description = "Soft delete a service package. Seer can delete their own packages, Admin can delete any package. " +
                         "The package will be hidden from all queries but data is preserved for existing bookings and reports. " +
                         "All incomplete bookings (PENDING, CONFIRMED) will be automatically refunded and canceled.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Service package deleted successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Service package not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Service package already deleted or cannot be deleted",
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
                            description = "Forbidden - Only package owner or admin can delete",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<String>> deleteServicePackage(
            @Parameter(description = "Service Package ID", required = true)
            @PathVariable String id
    ) {
        servicePackageService.deleteServicePackage(id);
        return responseFactory.successSingle(
                "Service package deleted successfully. All incomplete bookings have been refunded and canceled. " +
                "Data is preserved for existing bookings and reports (soft delete).",
                "Service package deleted successfully"
        );
    }

    @GetMapping("/by-category/{category}")
    @Operation(
            summary = "Get service packages by category",
            description = "Get all available service packages filtered by specific category",
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
                            responseCode = "400",
                            description = "Invalid category",
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
    public ResponseEntity<PageResponse<ServicePackageResponse>> getServicePackagesByCategory(
            @Parameter(description = "Service category (TAROT, PALM_READING, CONSULTATION, PHYSIOGNOMY)", required = true)
            @PathVariable String category,
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field (createdAt, price, packageTitle)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Minimum price filter")
            @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Maximum price filter")
            @RequestParam(required = false) Double maxPrice
    ) {
        Constants.ServiceCategoryEnum categoryEnum = Constants.ServiceCategoryEnum.get(category);
        Pageable pageable = createPageable(page, limit, sortType, sortBy);

        Page<ServicePackageResponse> response = servicePackageService.getPackagesByCategoryWithInteractions(categoryEnum, pageable, minPrice, maxPrice, null);

        return responseFactory.successPage(response,
                String.format("Service packages in category %s retrieved successfully", categoryEnum.getValue()));
    }

    @PostMapping("/{packageId}/interact")
    @Operation(
            summary = "Like or Dislike a service package",
            description = "Toggle like/dislike on a service package. " +
                    "Click LIKE: +1 like (or remove if already liked). " +
                    "Click DISLIKE: +1 dislike (or remove if already disliked). " +
                    "Click DISLIKE when LIKED: remove like and add dislike (and vice versa). " +
                    "Returns full service package response with all user interactions.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Interaction updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ServicePackageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid interaction type",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Service package not found",
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
    public ResponseEntity<SingleResponse<ServicePackageResponse>> toggleInteraction(
            @Parameter(description = "Service Package ID", required = true)
            @PathVariable UUID packageId,
            @Parameter(description = "Interaction request (LIKE or DISLIKE)", required = true)
            @RequestBody @Valid PackageInteractionRequest request
    ) {
        Constants.InteractionTypeEnum interactionType = Constants.InteractionTypeEnum.get(request.getInteractionType());
        ServicePackageResponse response = servicePackageService.toggleInteraction(packageId, interactionType);
        return responseFactory.successSingle(response, "Interaction updated successfully");
    }

    @GetMapping("/{packageId}/interactions")
    @Operation(
            summary = "Get service package with all user interactions",
            description = "Get service package information with list of all users who interacted (liked/disliked)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Service package with interactions retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ServicePackageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Service package not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<ServicePackageResponse>> getPackageWithInteractions(
            @Parameter(description = "Service Package ID", required = true)
            @PathVariable UUID packageId
    ) {
        ServicePackageResponse response = servicePackageService.getPackageWithInteractions(packageId);
        return responseFactory.successSingle(response, "Service package with interactions retrieved successfully");
    }

    // ============ Reviews (merged) ============
    @GetMapping("/{packageId}/reviews")
    @Operation(summary = "Get top-level reviews for a service package", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    public ResponseEntity<PageResponse<ServiceReviewResponse>> getTopLevelReviews(
            @Parameter(description = "Service package ID", required = true) @PathVariable UUID packageId,
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ServiceReview> reviews = servicePackageService.getTopLevelReviewsByPackage(packageId, pageable);
        Page<ServiceReviewResponse> response = serviceReviewMapper.mapToPage(reviews, ServiceReviewResponse.class);
        return responseFactory.successPage(response, "Reviews retrieved successfully");
    }

    @GetMapping("/reviews/{id}")
    @Operation(summary = "Get review by ID", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    public ResponseEntity<SingleResponse<ServiceReviewResponse>> getReviewById(
            @Parameter(description = "Review ID", required = true) @PathVariable UUID id
    ) {
        ServiceReview review = servicePackageService.getReviewById(id);
        ServiceReviewResponse response = serviceReviewMapper.mapTo(review, ServiceReviewResponse.class);
        return responseFactory.successSingle(response, "Review retrieved successfully");
    }

    @GetMapping("/reviews/{id}/replies")
    @Operation(summary = "Get replies for a review", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    public ResponseEntity<PageResponse<ServiceReviewResponse>> getReplies(
            @Parameter(description = "Parent review ID", required = true) @PathVariable UUID id,
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortType,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ServiceReview> replies = servicePackageService.getReplies(id, pageable);
        Page<ServiceReviewResponse> response = serviceReviewMapper.mapToPage(replies, ServiceReviewResponse.class);
        return responseFactory.successPage(response, "Replies retrieved successfully");
    }

    @PostMapping(path = "/{packageId}/reviews", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a review or reply for a service package", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    @PreAuthorize("hasAnyAuthority('CUSTOMER','SEER','ADMIN')")
    public ResponseEntity<SingleResponse<ServiceReviewResponse>> createReview(
            @Parameter(description = "Service Package ID", required = true) @PathVariable UUID packageId,
            @Valid @RequestBody ServiceReviewRequest request
    ) {
        ServiceReview entity = new ServiceReview();
        entity.setComment(request.getComment());
        if (request.getParentReviewId() != null) {
            ServiceReview parent = new ServiceReview();
            parent.setId(request.getParentReviewId());
            entity.setParentReview(parent);
        }

        ServiceReview saved = servicePackageService.createReview(packageId, entity);
        ServiceReviewResponse response = serviceReviewMapper.mapTo(saved, ServiceReviewResponse.class);
        return responseFactory.successSingle(response, "Review created successfully");
    }

    @PutMapping(path = "/reviews/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a review", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    @PreAuthorize("hasAnyAuthority('CUSTOMER','SEER','ADMIN')")
    public ResponseEntity<SingleResponse<ServiceReviewResponse>> updateReview(
            @Parameter(description = "Review ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody ServiceReviewRequest request
    ) {
        ServiceReview entity = new ServiceReview();
        entity.setComment(request.getComment());
        ServiceReview updated = servicePackageService.updateReview(id, entity);
        ServiceReviewResponse response = serviceReviewMapper.mapTo(updated, ServiceReviewResponse.class);
        return responseFactory.successSingle(response, "Review updated successfully");
    }

    @DeleteMapping("/reviews/{id}")
    @Operation(summary = "Delete a review (cascade deletes replies)", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    @PreAuthorize("hasAnyAuthority('CUSTOMER','SEER','ADMIN')")
    public ResponseEntity<SingleResponse<String>> deleteReview(
            @Parameter(description = "Review ID", required = true) @PathVariable UUID id
    ) {
        servicePackageService.deleteReview(id);
        return responseFactory.successSingle("Review deleted successfully", "Review deleted successfully");
    }

    // ============ Seer Endpoints ============

    @GetMapping("/my-packages")
    @Operation(
            summary = "Get my service packages (Seer only)",
            description = "Seer endpoint to retrieve all service packages created by the authenticated seer. " +
                         "Returns packages with all statuses (AVAILABLE, HIDDEN, REJECTED, etc.)",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "My service packages retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Seer only",
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
    @PreAuthorize("hasAuthority('SEER')")
    public ResponseEntity<PageResponse<ServicePackageResponse>> getMyPackages(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field (createdAt, price, packageTitle)")
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        log.info("Seer fetching their service packages");
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ServicePackageResponse> response = servicePackageService.getMyPackages(pageable);
        return responseFactory.successPage(response, "My service packages retrieved successfully");
    }

    // ============ Admin Endpoints ============

    @PutMapping("/{packageId}/confirm")
    @Operation(
            summary = "Admin confirm/reject service package",
            description = "Admin endpoint to approve or reject a service package. " +
                         "Action can be: APPROVED, REJECTED. Only packages with HIDDEN status can be confirmed. " +
                         "If action is REJECTED, rejectionReason is required.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Service package status updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ServicePackageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request - rejection reason required for REJECTED action or invalid transition",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Service package not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Admin only",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SingleResponse<ServicePackageResponse>> confirmServicePackage(
            @Parameter(description = "Service Package ID", required = true)
            @PathVariable String packageId,
            @Parameter(description = "Confirmation request with action (APPROVED or REJECTED) and optional rejection reason", required = true)
            @RequestBody @Valid ServicePackageConfirmRequest request
    ) {
        log.info("Admin confirming service package {} with action: {}", packageId, request.getAction());

        ServicePackage servicePackage = servicePackageService.confirmServicePackage(
                packageId,
                request.getAction(),
                request.getRejectionReason()
        );

        ServicePackageResponse response = servicePackageMapper.mapTo(servicePackage, ServicePackageResponse.class);

        String message = request.getAction() == Constants.PackageActionEnum.REJECTED
                ? "Service package rejected successfully"
                : "Service package approved successfully";

        return responseFactory.successSingle(response, message);
    }

    @GetMapping("/admin/hidden")
    @Operation(
            summary = "Get all hidden service packages (Admin only)",
            description = "Admin endpoint to retrieve all service packages with HIDDEN status (pending approval)",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Hidden service packages retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Admin only",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PageResponse<ServicePackageResponse>> getAllHiddenPackages(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field (createdAt, price, packageTitle)")
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        log.info("Admin fetching hidden service packages");
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ServicePackageResponse> response = servicePackageService.getAllHiddenPackages(pageable);
        return responseFactory.successPage(response, "Hidden service packages retrieved successfully");
    }

    @GetMapping("/admin")
    @Operation(
            summary = "Admin: Get all service packages",
            description = "Admin endpoint to retrieve all service packages with filters. Similar to /public/service-packages.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Service packages retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Admin only",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PageResponse<ServicePackageResponse>> getAllServicePackagesAdmin(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field (createdAt, price, packageTitle)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Search text for package title")
            @RequestParam(required = false) String searchText,
            @Parameter(description = "Minimum price filter")
            @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Maximum price filter")
            @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Package category IDs filter (multiple values supported)")
            @RequestParam(required = false) List<UUID> packageCategoryIds,
            @Parameter(description = "Seer speciality IDs filter (multiple values supported)")
            @RequestParam(required = false) List<UUID> seerSpecialityIds,
            @Parameter(description = "Minimum duration in minutes")
            @RequestParam(required = false) Integer minTime,
            @Parameter(description = "Maximum duration in minutes")
            @RequestParam(required = false) Integer maxTime,
            @Parameter(description = "Package status filter (AVAILABLE, REJECTED, HAVE_REPORT, HIDDEN)")
            @RequestParam(required = false) String status
    ) {
        log.info("Admin fetching all service packages");
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Constants.PackageStatusEnum statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = Constants.PackageStatusEnum.get(status);
        }
        
        Page<ServicePackageResponse> packages = servicePackageService.getAllPackagesWithInteractions(
                pageable,
                searchText,
                minPrice, maxPrice,
                packageCategoryIds,
                seerSpecialityIds,
                minTime, maxTime,
                statusEnum
        );
        
        return responseFactory.successPage(packages, "Service packages retrieved successfully");
    }

    @GetMapping("/stat")
    @Operation(
            summary = "Admin: Get service package statistics",
            description = "Admin endpoint to get service package statistics (total, reported, hidden, total interactions)",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Statistics retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Admin only",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<SingleResponse<ServicePackagePageResponse.ServicePackageStats>> getServicePackageStats() {
        ServicePackageServiceImpl.ServicePackageStats stats = 
                ((ServicePackageServiceImpl) servicePackageService).getAllPackagesStats();
        ServicePackagePageResponse.ServicePackageStats statsResponse = 
                ServicePackagePageResponse.ServicePackageStats.builder()
                        .totalPackages(stats.getTotalPackages())
                        .reportedPackages(stats.getReportedPackages())
                        .hiddenPackages(stats.getHiddenPackages())
                        .totalInteractions(stats.getTotalInteractions())
                        .build();
        return responseFactory.successSingle(statsResponse, "Service package statistics retrieved successfully");
    }
}
