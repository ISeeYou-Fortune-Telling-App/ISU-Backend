package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.account.SimpleSeerCardResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.dto.response.servicepackage.ServicePackageResponse;
import com.iseeyou.fortunetelling.dto.response.ServicePackageDetailResponse;
import com.iseeyou.fortunetelling.service.servicepackage.ServicePackageService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public")
@Tag(name = "000. Public APIs", description = "Public APIs that don't require authentication")
@Slf4j
public class PublicController extends AbstractBaseController {

    private final ServicePackageService servicePackageService;
    private final UserService userService;

    // ============ SERVICE PACKAGES PUBLIC ENDPOINTS ============
    
    @GetMapping("/service-packages")
    @Operation(
            summary = "Get all available service packages (Public)",
            description = "Get all available service packages with filters. No authentication required.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
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
            @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Find by name service package")
            @RequestParam(required = false) String searchText,
            @Parameter(description = "Minimum time (minute) filter")
            @RequestParam(required = false) Integer minTime,
            @Parameter(description = "Maximum time (minute) filter")
            @RequestParam(required = false) Integer maxTime,
            @Parameter(description = "Package category Ids")
            @RequestParam(required = false) List<UUID> packageCategoryIds,
            @Parameter(description = "Seer speciality Ids")
            @RequestParam(required = false) List<UUID> seerSpecialityIds
            ) {
        log.info("Public API: Get all service packages - page: {}, limit: {}", page, limit);
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        return responseFactory.successPage(servicePackageService.getAllPackagesWithInteractions(
                pageable,
                searchText,
                minPrice, maxPrice,
                packageCategoryIds,
                seerSpecialityIds,
                minTime, maxTime
        ), "Service packages retrieved successfully");
    }

    @GetMapping("/seers")
    @Operation(
            summary = "Get all available service packages (Public)",
            description = "Get all available service packages with filters. No authentication required.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<PageResponse<SimpleSeerCardResponse>> getAllSeersWithFilter(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field (createdAt, price, packageTitle)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Find by name seer")
            @RequestParam(required = false) String searchText,
            @Parameter(description = "Seer speciality Ids")
            @RequestParam(required = false) List<UUID> seerSpecialityIds
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        return responseFactory.successPage(userService.getSimpleSeerCardsWithFilter(
                pageable,
                searchText,
                seerSpecialityIds
        ), "Service packages retrieved successfully");
    }

    @GetMapping("/service-packages/detail")
    @Operation(
            summary = "Get service package detail (Public)",
            description = "Get detailed service package information including seer profile and rating. No authentication required.",
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
                    )
            }
    )
    public ResponseEntity<SingleResponse<ServicePackageDetailResponse>> getServicePackageDetail(
            @Parameter(description = "Service Package ID", required = true)
            @RequestParam String id
    ) {
        log.info("Public API: Get service package detail - id: {}", id);
        ServicePackageDetailResponse response = servicePackageService.findDetailById(id);
        return responseFactory.successSingle(response, "Service package detail retrieved successfully");
    }

    @GetMapping("/service-packages/by-category/{category}")
    @Operation(
            summary = "Get service packages by category (Public)",
            description = "Get all available service packages filtered by specific category. No authentication required.",
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
        log.info("Public API: Get service packages by category - category: {}, page: {}", category, page);
        Constants.ServiceCategoryEnum categoryEnum = Constants.ServiceCategoryEnum.get(category);
        Pageable pageable = createPageable(page, limit, sortType, sortBy);

        Page<ServicePackageResponse> response = servicePackageService.getPackagesByCategoryWithInteractions(categoryEnum, pageable, minPrice, maxPrice);

        return responseFactory.successPage(response,
                String.format("Service packages in category %s retrieved successfully", categoryEnum.getValue()));
    }


}
