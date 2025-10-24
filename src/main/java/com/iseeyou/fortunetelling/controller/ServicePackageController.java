package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.servicepackage.PackageInteractionRequest;
import com.iseeyou.fortunetelling.dto.request.servicepackage.ServicePackageUpsertRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.dto.response.servicepackage.ServicePackageResponse;
import com.iseeyou.fortunetelling.dto.response.ServicePackageDetailResponse;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.mapper.ServicePackageMapper;
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
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    @Operation(
            summary = "Get all available service packages with filters",
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
    public ResponseEntity<PageResponse<ServicePackageResponse>> getAllServicePackages(
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
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ServicePackageResponse> response = servicePackageService.getAllPackagesWithInteractions(pageable, minPrice, maxPrice);
        return responseFactory.successPage(response, "Service packages retrieved successfully");
    }

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
        description = "Create a new service package with full info, image upload, price, duration. Status is HIDDEN (pending approval)",
        security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<SingleResponse<ServicePackage>> createServicePackage(
            @RequestParam("seerId") String seerId,
            @ModelAttribute ServicePackageUpsertRequest request
    ) {
        ServicePackage servicePackage = servicePackageService.createOrUpdatePackage(seerId, request);
        return responseFactory.successSingle(servicePackage, "Service package created successfully");
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Update service package",
        description = "Update an existing service package. Status remains HIDDEN if not approved.",
        security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<SingleResponse<ServicePackage>> updateServicePackage(
            @Parameter(description = "Service Package ID", required = true)
            @RequestParam String id,
            @ModelAttribute ServicePackageUpsertRequest request
    ) {
        request.setPackageId(id);
        // Lấy seerId từ service package hiện tại thay vì từ request param
        ServicePackage existingPackage = servicePackageService.findById(id);
        ServicePackage servicePackage = servicePackageService.createOrUpdatePackage(existingPackage.getSeer().getId().toString(), request);
        return responseFactory.successSingle(servicePackage, "Service package updated successfully");
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

        Page<ServicePackageResponse> response = servicePackageService.getPackagesByCategoryWithInteractions(categoryEnum, pageable, minPrice, maxPrice);

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
}
