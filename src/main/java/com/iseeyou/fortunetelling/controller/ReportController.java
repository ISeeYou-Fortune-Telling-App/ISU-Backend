package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.report.ReportCreateRequest;
import com.iseeyou.fortunetelling.dto.request.report.ReportUpdateRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.dto.response.report.ReportResponse;
import com.iseeyou.fortunetelling.dto.response.report.ReportTypeResponse;
import com.iseeyou.fortunetelling.entity.report.Report;
import com.iseeyou.fortunetelling.entity.report.ReportType;
import com.iseeyou.fortunetelling.mapper.ReportMapper;
import com.iseeyou.fortunetelling.service.fileupload.CloudinaryService;
import com.iseeyou.fortunetelling.service.report.ReportService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
@Tag(name = "007. Report", description = "Report API")
@Slf4j
public class ReportController extends AbstractBaseController {

    private final ReportService reportService;
    private final ReportMapper reportMapper;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    @Operation(
            summary = "Get all reports with pagination",
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
    public ResponseEntity<PageResponse<ReportResponse>> getAllReports(
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
        Page<Report> reports = reportService.findAllReports(pageable);
        Page<ReportResponse> response = reportMapper.mapToPage(reports, ReportResponse.class);
        return responseFactory.successPage(response, "Reports retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get report by ID",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ReportResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Report not found",
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
    public ResponseEntity<SingleResponse<ReportResponse>> getReportById(
            @Parameter(description = "Report ID", required = true)
            @PathVariable UUID id
    ) {
        Report report = reportService.findReportById(id);
        ReportResponse response = reportMapper.mapTo(report, ReportResponse.class);
        return responseFactory.successSingle(response, "Report retrieved successfully");
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create a new report with image evidence upload",
            description = "Create a new report. You can upload multiple image files as evidence. " +
                         "All files will be uploaded to cloud storage and URLs will be stored in the database.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = ReportCreateRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Report created successfully",
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
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'SEER', 'GUEST', 'ADMIN')")
    public ResponseEntity<SingleResponse<ReportResponse>> createReport(
            @ModelAttribute @Valid ReportCreateRequest request
    ) throws IOException {
        Report reportToCreate = reportMapper.mapTo(request, Report.class);

        // Handle evidence image uploads
        Set<String> evidenceUrls = new HashSet<>();
        if (request.getImageFiles() != null) {
            for (MultipartFile imageFile : request.getImageFiles()) {
                if (!imageFile.isEmpty()) {
                    String imageUrl = cloudinaryService.uploadFile(imageFile, "report-evidence");
                    evidenceUrls.add(imageUrl);
                }
            }
        }

        // Create report with enum report type
        Report createdReport = reportService.createReport(reportToCreate, request.getReportType(), evidenceUrls);
        ReportResponse response = reportMapper.mapTo(createdReport, ReportResponse.class);
        return responseFactory.successSingle(response, "Report created successfully");
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update report status",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Report updated successfully",
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
                            description = "Report not found",
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
    public ResponseEntity<SingleResponse<ReportResponse>> updateReport(
            @Parameter(description = "Report ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated report data", required = true)
            @RequestBody @Valid ReportUpdateRequest request
    ) {
        Report reportToUpdate = reportMapper.mapTo(request, Report.class);
        Report updatedReport = reportService.updateReport(id, reportToUpdate);
        ReportResponse response = reportMapper.mapTo(updatedReport, ReportResponse.class);
        return responseFactory.successSingle(response, "Report updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete report",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Report deleted successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Report not found",
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
    public ResponseEntity<SingleResponse<Object>> deleteReport(
            @Parameter(description = "Report ID", required = true)
            @PathVariable UUID id
    ) {
        reportService.deleteReport(id);
        return responseFactory.successSingle(null, "Report deleted successfully");
    }

    @GetMapping("/reporter/{reporterId}")
    @Operation(
            summary = "Get reports by reporter ID",
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
    public ResponseEntity<PageResponse<ReportResponse>> getReportsByReporterId(
            @Parameter(description = "Reporter ID", required = true)
            @PathVariable UUID reporterId,
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
        Page<Report> reports = reportService.findAllReportsByReporterId(reporterId, pageable);
        Page<ReportResponse> response = reportMapper.mapToPage(reports, ReportResponse.class);
        return responseFactory.successPage(response, "Reports by reporter retrieved successfully");
    }

    @GetMapping("/reported-user/{reportedUserId}")
    @Operation(
            summary = "Get reports by reported user ID",
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
    public ResponseEntity<PageResponse<ReportResponse>> getReportsByReportedUserId(
            @Parameter(description = "Reported User ID", required = true)
            @PathVariable UUID reportedUserId,
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
        Page<Report> reports = reportService.findAllReportsByReportedUserId(reportedUserId, pageable);
        Page<ReportResponse> response = reportMapper.mapToPage(reports, ReportResponse.class);
        return responseFactory.successPage(response, "Reports by reported user retrieved successfully");
    }

    @GetMapping("/target/{targetId}")
    @Operation(
            summary = "Get reports by target ID",
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
    public ResponseEntity<PageResponse<ReportResponse>> getReportsByTargetId(
            @Parameter(description = "Target ID", required = true)
            @PathVariable UUID targetId,
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
        Page<Report> reports = reportService.findAllReportsByReportedTargetId(targetId, pageable);
        Page<ReportResponse> response = reportMapper.mapToPage(reports, ReportResponse.class);
        return responseFactory.successPage(response, "Reports by target retrieved successfully");
    }

    @GetMapping("/target-type/{targetType}")
    @Operation(
            summary = "Get reports by target type",
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
    public ResponseEntity<PageResponse<ReportResponse>> getReportsByTargetType(
            @Parameter(description = "Target Type", required = true)
            @PathVariable Constants.TargetReportTypeEnum targetType,
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
        Page<Report> reports = reportService.findAllReportsByTargetType(targetType, pageable);
        Page<ReportResponse> response = reportMapper.mapToPage(reports, ReportResponse.class);
        return responseFactory.successPage(response, "Reports by target type retrieved successfully");
    }

    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get reports by status",
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
    public ResponseEntity<PageResponse<ReportResponse>> getReportsByStatus(
            @Parameter(description = "Report Status", required = true)
            @PathVariable Constants.ReportStatusEnum status,
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
        Page<Report> reports = reportService.findAllReportsByStatus(status, pageable);
        Page<ReportResponse> response = reportMapper.mapToPage(reports, ReportResponse.class);
        return responseFactory.successPage(response, "Reports by status retrieved successfully");
    }

    @GetMapping("/types")
    @Operation(
            summary = "Get all report types",
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
    public ResponseEntity<PageResponse<ReportTypeResponse>> getAllReportTypes(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "asc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ReportType> reportTypes = reportService.findAllReportTypes(pageable);
        Page<ReportTypeResponse> response = reportMapper.mapToPage(reportTypes, ReportTypeResponse.class);
        return responseFactory.successPage(response, "Report types retrieved successfully");
    }
}
