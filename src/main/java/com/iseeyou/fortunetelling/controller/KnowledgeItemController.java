package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.knowledgeitem.KnowledgeItemCreateRequest;
import com.iseeyou.fortunetelling.dto.request.knowledgeitem.KnowledgeItemUpdateRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.dto.response.knowledgeitem.KnowledgeItemResponse;
import com.iseeyou.fortunetelling.entity.KnowledgeItem;
import com.iseeyou.fortunetelling.mapper.KnowledgeItemMapper;
import com.iseeyou.fortunetelling.service.fileupload.CloudinaryService;
import com.iseeyou.fortunetelling.service.knowledgeitem.KnowledgeItemService;
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

import java.io.IOException;
import java.util.UUID;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/knowledge-items")
@Tag(name = "005. Knowledge Item", description = "Knowledge Item API")
@Slf4j
public class KnowledgeItemController extends AbstractBaseController {
    private final KnowledgeItemService knowledgeItemService;
    private final KnowledgeItemMapper knowledgeItemMapper;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    @Operation(
            summary = "Get all knowledge items with pagination",
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
    public ResponseEntity<PageResponse<KnowledgeItemResponse>> getAllKnowledgeItems(
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
        Page<KnowledgeItem> knowledgeItems = knowledgeItemService.findAll(pageable);
        Page<KnowledgeItemResponse> response = knowledgeItemMapper.mapToPage(knowledgeItems, KnowledgeItemResponse.class);
        return responseFactory.successPage(response, "Knowledge items retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get knowledge item by ID",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = KnowledgeItemResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Knowledge item not found",
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
    public ResponseEntity<SingleResponse<KnowledgeItemResponse>> getKnowledgeItemById(
            @Parameter(description = "Knowledge Item ID", required = true)
            @PathVariable UUID id
    ) {
        KnowledgeItem knowledgeItem = knowledgeItemService.findById(id);
        KnowledgeItemResponse response = knowledgeItemMapper.mapTo(knowledgeItem, KnowledgeItemResponse.class);
        return responseFactory.successSingle(response, "Knowledge item retrieved successfully");
    }

    @PostMapping("/{id}/view")
    @Operation(
            summary = "Increment view count for knowledge item",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "View count incremented successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Knowledge item not found",
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
    public ResponseEntity<SingleResponse<Void>> incrementViewCount(
            @Parameter(description = "Knowledge Item ID", required = true)
            @PathVariable UUID id
    ) {
        knowledgeItemService.view(id);
        return responseFactory.successSingle(null, "Knowledge item view count incremented successfully");
    }

    @PostMapping
    @Operation(
            summary = "Create a new knowledge item",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Knowledge item created successfully",
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
    public ResponseEntity<SingleResponse<KnowledgeItemResponse>> createKnowledgeItem(
            @Parameter(description = "Knowledge item data to create", required = true)
            @ModelAttribute @Valid KnowledgeItemCreateRequest request
    ) throws IOException {
        KnowledgeItem knowledgeItemToCreate = knowledgeItemMapper.mapTo(request, KnowledgeItem.class);

        if (request.getImageFile() != null) {
            String imageUrl = cloudinaryService.uploadFile(request.getImageFile(), "knowledge-items");
            knowledgeItemToCreate.setImageUrl(imageUrl);
        }

        KnowledgeItem createdKnowledgeItem = knowledgeItemService.create(knowledgeItemToCreate, request.getCategoryIds());
        KnowledgeItemResponse response = knowledgeItemMapper.mapTo(createdKnowledgeItem, KnowledgeItemResponse.class);
        return responseFactory.successSingle(response, "Knowledge item created successfully");
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update knowledge item",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Knowledge item updated successfully",
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
                            description = "Knowledge item not found",
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
    public ResponseEntity<SingleResponse<KnowledgeItemResponse>> updateKnowledgeItem(
            @Parameter(description = "Knowledge Item ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated knowledge item data", required = true)
            @ModelAttribute @Valid KnowledgeItemUpdateRequest request
    ) throws IOException {
        KnowledgeItem knowledgeItemToUpdate = knowledgeItemMapper.mapTo(request, KnowledgeItem.class);

        if (request.getImageFile() != null) {
            String imageUrl = cloudinaryService.uploadFile(request.getImageFile(), "knowledge-items");
            knowledgeItemToUpdate.setImageUrl(imageUrl);
        }

        KnowledgeItem updatedKnowledgeItem = knowledgeItemService.update(id, knowledgeItemToUpdate, request.getCategoryIds());
        KnowledgeItemResponse response = knowledgeItemMapper.mapTo(updatedKnowledgeItem, KnowledgeItemResponse.class);
        return responseFactory.successSingle(response, "Knowledge item updated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete knowledge item",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Knowledge item deleted successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Knowledge item not found",
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
    public ResponseEntity<SingleResponse<Void>> deleteKnowledgeItem(
            @Parameter(description = "Knowledge Item ID", required = true)
            @PathVariable UUID id
    ) throws IOException {
        knowledgeItemService.delete(id);
        return responseFactory.successSingle(null, "Knowledge item deleted successfully");
    }

    @GetMapping("/by-status/{status}")
    @Operation(
            summary = "Find knowledge items by status",
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
    public ResponseEntity<PageResponse<KnowledgeItemResponse>> getKnowledgeItemsByStatus(
            @Parameter(description = "Status", required = true)
            @PathVariable Constants.KnowledgeItemStatusEnum status,
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
        Page<KnowledgeItem> knowledgeItems = knowledgeItemService.findAllByStatus(status, pageable);
        Page<KnowledgeItemResponse> response = knowledgeItemMapper.mapToPage(knowledgeItems, KnowledgeItemResponse.class);
        return responseFactory.successPage(response, "Knowledge items retrieved successfully");
    }

    @GetMapping("/by-category/{categoryId}")
    @Operation(
            summary = "Find knowledge items by category ID",
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
    public ResponseEntity<PageResponse<KnowledgeItemResponse>> getKnowledgeItemsByCategoryId(
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
        Page<KnowledgeItem> knowledgeItems = knowledgeItemService.findAllByKnowledgeCategoryId(categoryId, pageable);
        Page<KnowledgeItemResponse> response = knowledgeItemMapper.mapToPage(knowledgeItems, KnowledgeItemResponse.class);
        return responseFactory.successPage(response, "Knowledge items retrieved successfully");
    }
}