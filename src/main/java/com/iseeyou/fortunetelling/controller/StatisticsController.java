package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.service.statistics.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
@Tag(name = "012. Statistics", description = "Statistics API")
@Slf4j
public class StatisticsController extends AbstractBaseController {

    private final StatisticsService statisticsService;

    @GetMapping("/users/monthly")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
            summary = "Get monthly new users statistics",
            description = "Returns the number of new users created in each month for the specified year. " +
                          "The result includes all 12 months; months with no new users will have a count of 0.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Statistics retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    public ResponseEntity<SingleResponse<Map<Integer, Long>>> getNewUsersByMonth(
            @Parameter(description = "Year to query", example = "2025", required = true)
            @RequestParam Integer year
    ) {
        log.info("Fetching new users statistics for year: {}", year);
        Map<Integer, Long> statistics = statisticsService.getNewUsersByMonth(year);
        return responseFactory.successSingle(statistics, "Statistics retrieved successfully");
    }

    @GetMapping("/packages/category-distribution")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(
            summary = "Get package distribution by knowledge category",
            description = "Returns the number of packages assigned to each knowledge category. " +
                          "The result is a map where the key is the category name and the value is the package count.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Statistics retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    public ResponseEntity<SingleResponse<Map<String, Long>>> getPackageDistributionByCategory() {
        log.info("Fetching package distribution by category");
        Map<String, Long> distribution = statisticsService.getPackageDistributionByCategory();
        return responseFactory.successSingle(distribution, "Statistics retrieved successfully");
    }
}
