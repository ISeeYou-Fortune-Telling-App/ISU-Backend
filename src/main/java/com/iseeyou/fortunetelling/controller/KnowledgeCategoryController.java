package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.knowledgecategory.KnowledgeCategoryCreateRequest;
import com.iseeyou.fortunetelling.dto.request.knowledgecategory.KnowledgeCategoryUpdateRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.dto.response.knowledgecategory.KnowledgeCategoryResponse;
import com.iseeyou.fortunetelling.entity.KnowledgeCategory;
import com.iseeyou.fortunetelling.mapper.SimpleMapper;
import com.iseeyou.fortunetelling.service.knowledgecategory.KnowledgeCategoryService;
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
@RequestMapping("/knowledge-categories")
@Tag(name = "003. Knowledge Categories", description = "Knowledge Categories API")
@Slf4j
public class KnowledgeCategoryController extends AbstractBaseController {
    private final KnowledgeCategoryService knowledgeCategoryService;
    private final SimpleMapper simpleMapper;

    @GetMapping
    @Operation(
            summary = "Get all knowledge categories with pagination",
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
    public ResponseEntity<PageResponse<KnowledgeCategoryResponse>> getAllKnowledgeCategories(
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
        Page<KnowledgeCategory> categories = knowledgeCategoryService.findAll(pageable);
        Page<KnowledgeCategoryResponse> response = simpleMapper.mapToPage(categories, KnowledgeCategoryResponse.class);
        return responseFactory.successPage(response, "Knowledge categories retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get knowledge category by ID",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = KnowledgeCategory.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Knowledge category not found",
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
    public ResponseEntity<SingleResponse<KnowledgeCategoryResponse>> getKnowledgeCategoryById(
            @Parameter(description = "Knowledge Category ID", required = true)
            @PathVariable UUID id
    ) {
        KnowledgeCategory category = knowledgeCategoryService.findById(id);
        KnowledgeCategoryResponse response = simpleMapper.mapTo(category, KnowledgeCategoryResponse.class);
        return responseFactory.successSingle(response, "Knowledge category retrieved successfully");
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update knowledge category",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Knowledge category updated successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = KnowledgeCategory.class)
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
                            description = "Knowledge category not found",
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
    public ResponseEntity<SingleResponse<KnowledgeCategoryResponse>> updateKnowledgeCategory(
            @Parameter(description = "Knowledge Category ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated knowledge category data", required = true)
            @RequestBody @Valid KnowledgeCategoryUpdateRequest request
    ) {
        KnowledgeCategory categoryToUpdate = simpleMapper.mapTo(request, KnowledgeCategory.class);
        KnowledgeCategory updatedCategory = knowledgeCategoryService.update(id, categoryToUpdate);
        KnowledgeCategoryResponse response = simpleMapper.mapTo(updatedCategory, KnowledgeCategoryResponse.class);
        return responseFactory.successSingle(response, "Knowledge category updated successfully");
    }

    @GetMapping("/by-name")
    @Operation(
            summary = "Find knowledge category by name",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = KnowledgeCategory.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Knowledge category not found",
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
    public ResponseEntity<SingleResponse<KnowledgeCategoryResponse>> getKnowledgeCategoryByName(
            @Parameter(description = "Knowledge Category name", required = true)
            @RequestParam String name
    ) {
        KnowledgeCategory category = knowledgeCategoryService.findByName(name);
        KnowledgeCategoryResponse response = simpleMapper.mapTo(category, KnowledgeCategoryResponse.class);
        return responseFactory.successSingle(response, "Knowledge category retrieved successfully");
    }

    @PostMapping
    @Operation(
            summary = "Create a new knowledge category",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Knowledge category created successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = KnowledgeCategory.class)
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
    public ResponseEntity<SingleResponse<KnowledgeCategoryResponse>> createKnowledgeCategory(
            @Parameter(description = "Knowledge category data to create", required = true)
            @RequestBody @Valid KnowledgeCategoryCreateRequest request
    ) {
        KnowledgeCategory categoryToCreate = simpleMapper.mapTo(request, KnowledgeCategory.class);
        KnowledgeCategory createdCategory = knowledgeCategoryService.create(categoryToCreate);
        KnowledgeCategoryResponse response = simpleMapper.mapTo(createdCategory, KnowledgeCategoryResponse.class);
        return responseFactory.successSingle(response, "Knowledge category created successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete knowledge category",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Knowledge category deleted successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Knowledge category not found",
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
    public ResponseEntity<SingleResponse<Void>> deleteKnowledgeCategory(
            @Parameter(description = "Knowledge Category ID", required = true)
            @PathVariable String id
    ) {
        knowledgeCategoryService.delete(id);
        return responseFactory.successSingle(null, "Knowledge category deleted successfully");
    }
}