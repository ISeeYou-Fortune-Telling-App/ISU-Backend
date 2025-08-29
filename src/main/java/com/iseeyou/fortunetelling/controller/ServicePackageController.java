package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.servicepackage.ServicePackageCreateRequest;
import com.iseeyou.fortunetelling.dto.request.servicepackage.ServicePackageUpdateRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.servicepackage.ServicePackageResponse;
import com.iseeyou.fortunetelling.dto.response.servicepackage.ServiceReviewResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.entity.servicepackage.ServiceReview;
import com.iseeyou.fortunetelling.mapper.ServicePackageMapper;
import com.iseeyou.fortunetelling.service.fileupload.CloudinaryService;
import com.iseeyou.fortunetelling.service.servicepackage.ServicePackageService;
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

import java.io.IOException;
import java.util.UUID;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/service-packages")
@Tag(name = "008. Service Package", description = "Service Package API")
@Slf4j
public class ServicePackageController extends AbstractBaseController {

    private final ServicePackageService servicePackageService;
    private final ServicePackageMapper servicePackageMapper;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    @Operation(
            summary = "Get all service packages with pagination",
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
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ServicePackage> servicePackages = servicePackageService.findAll(pageable);
        Page<ServicePackageResponse> response = servicePackageMapper.mapToPage(servicePackages, ServicePackageResponse.class);
        return responseFactory.successPage(response, "Service packages retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get service package by ID",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
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
    public ResponseEntity<SingleResponse<ServicePackageResponse>> getServicePackageById(
            @Parameter(description = "Service package ID", required = true)
            @PathVariable UUID id
    ) {
        ServicePackage servicePackage = servicePackageService.findById(id);
        ServicePackageResponse response = servicePackageMapper.mapTo(servicePackage, ServicePackageResponse.class);
        return responseFactory.successSingle(response, "Service package retrieved successfully");
    }

    @PostMapping
    @Operation(
            summary = "Create a new service package",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Service package created successfully",
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
    public ResponseEntity<SingleResponse<ServicePackageResponse>> createServicePackage(
            @Parameter(description = "Service package data to create", required = true)
            @ModelAttribute @Valid ServicePackageCreateRequest request
    ) throws IOException {
        ServicePackage servicePackageToCreate = servicePackageMapper.mapTo(request, ServicePackage.class);

        // Handle image upload if provided
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(request.getImageFile(), "service-packages");
            servicePackageToCreate.setImageUrl(imageUrl);
        }

        ServicePackage createdServicePackage = servicePackageService.create(servicePackageToCreate, request.getCategoryIds());
        ServicePackageResponse response = servicePackageMapper.mapTo(createdServicePackage, ServicePackageResponse.class);
        return responseFactory.successSingle(response, "Service package created successfully");
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update service package",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Service package updated successfully",
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
    public ResponseEntity<SingleResponse<ServicePackageResponse>> updateServicePackage(
            @Parameter(description = "Service package ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated service package data", required = true)
            @ModelAttribute @Valid ServicePackageUpdateRequest request
    ) throws IOException {
        ServicePackage servicePackageToUpdate = servicePackageMapper.mapTo(request, ServicePackage.class);

        // Handle image upload if provided
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(request.getImageFile(), "service-packages");
            servicePackageToUpdate.setImageUrl(imageUrl);
        }

        ServicePackage updatedServicePackage = servicePackageService.update(id, servicePackageToUpdate, request.getCategoryIds());
        ServicePackageResponse response = servicePackageMapper.mapTo(updatedServicePackage, ServicePackageResponse.class);
        return responseFactory.successSingle(response, "Service package updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete service package",
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
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<Object>> deleteServicePackage(
            @Parameter(description = "Service package ID", required = true)
            @PathVariable UUID id
    ) throws IOException {
        servicePackageService.delete(id);
        return responseFactory.successSingle(null, "Service package deleted successfully");
    }

    @GetMapping("/seerId/{seerId}")
    @Operation(
            summary = "Get service packages by seer ID",
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
    public ResponseEntity<PageResponse<ServicePackageResponse>> getServicePackagesBySeerId(
            @Parameter(description = "Seer ID", required = true)
            @PathVariable UUID seerId,
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
        Page<ServicePackage> servicePackages = servicePackageService.findByUserId(seerId, pageable);
        Page<ServicePackageResponse> response = servicePackageMapper.mapToPage(servicePackages, ServicePackageResponse.class);
        return responseFactory.successPage(response, "User service packages retrieved successfully");
    }

    @GetMapping("/seer/{seerId}/category/{categoryId}")
    @Operation(
            summary = "Get service packages by seer ID and category ID",
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
    public ResponseEntity<PageResponse<ServicePackageResponse>> getServicePackagesBySeerIdAndCategoryId(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID seerId,
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
        Page<ServicePackage> servicePackages = servicePackageService.findByUserIdAndCategoryId(seerId, categoryId, pageable);
        Page<ServicePackageResponse> response = servicePackageMapper.mapToPage(servicePackages, ServicePackageResponse.class);
        return responseFactory.successPage(response, "User service packages by category retrieved successfully");
    }

    @GetMapping("/category/{categoryId}")
    @Operation(
            summary = "Get service packages by category ID",
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
    public ResponseEntity<PageResponse<ServicePackageResponse>> getServicePackagesByCategoryId(
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
        Page<ServicePackage> servicePackages = servicePackageService.findByCategoryId(categoryId, pageable);
        Page<ServicePackageResponse> response = servicePackageMapper.mapToPage(servicePackages, ServicePackageResponse.class);
        return responseFactory.successPage(response, "Service packages by category retrieved successfully");
    }

    @PostMapping("/{id}/interact")
    @Operation(
            summary = "Like or dislike a service package",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Interaction recorded successfully",
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
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<Object>> interactWithServicePackage(
            @Parameter(description = "Service package ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Interaction type: true for like, false for dislike", required = true)
            @RequestParam boolean isLike
    ) {
        servicePackageService.interactPackage(id, isLike);
        String message = isLike ? "Service package liked successfully" : "Service package disliked successfully";
        return responseFactory.successSingle(null, message);
    }

    @PostMapping("/{id}/comments")
    @Operation(
            summary = "Leave a comment on a service package",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Comment added successfully",
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
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<Object>> leaveComment(
            @Parameter(description = "Service package ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Comment content", required = true)
            @RequestParam String content,
            @Parameter(description = "Parent comment ID (for replies)")
            @RequestParam(required = false) UUID parentCommentId
    ) {
        servicePackageService.leaveComment(id, parentCommentId, content);
        String message = parentCommentId != null ? "Reply added successfully" : "Comment added successfully";
        return responseFactory.successSingle(null, message);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(
            summary = "Delete a comment",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Comment deleted successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Comment not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - You can only delete your own comments",
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
    public ResponseEntity<SingleResponse<Object>> deleteComment(
            @Parameter(description = "Comment ID", required = true)
            @PathVariable UUID commentId
    ) throws IOException {
        servicePackageService.deleteComment(commentId);
        return responseFactory.successSingle(null, "Comment deleted successfully");
    }

    @GetMapping("/{id}/comments")
    @Operation(
            summary = "Get comments for a service package",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Comments retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
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
    public ResponseEntity<PageResponse<ServiceReviewResponse>> getCommentsByPackageId(
            @Parameter(description = "Service package ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ServiceReview> comments = servicePackageService.getCommentsByPackageId(id, pageable);
        Page<ServiceReviewResponse> response = servicePackageMapper.mapToPage(comments, ServiceReviewResponse.class);
        return responseFactory.successPage(response, "Comments retrieved successfully");
    }

    @GetMapping("/comments/{commentId}/replies")
    @Operation(
            summary = "Get replies to a specific comment",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Replies retrieved successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Comment not found",
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
    public ResponseEntity<PageResponse<ServiceReviewResponse>> getRepliesByCommentId(
            @Parameter(description = "Comment ID", required = true)
            @PathVariable UUID commentId,
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "asc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ServiceReview> replies = servicePackageService.getRepliesByCommentId(commentId, pageable);
        Page<ServiceReviewResponse> response = servicePackageMapper.mapToPage(replies, ServiceReviewResponse.class);
        return responseFactory.successPage(response, "Replies retrieved successfully");
    }
}
