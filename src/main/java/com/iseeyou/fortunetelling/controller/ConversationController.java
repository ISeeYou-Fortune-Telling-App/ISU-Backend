package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.converstation.ChatHistoryFilterRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.converstation.ChatSessionResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.service.converstation.ConversationService;
import com.iseeyou.fortunetelling.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/conversations")
@Tag(name = "005. Conversation", description = "Conversation & Chat History API")
@Slf4j
public class ConversationController extends AbstractBaseController {

    private final ConversationService conversationService;

    @GetMapping("/history")
    @Operation(
            summary = "Get chat history with optional filters",
            description = "Admin: filter by participant name, conversation type, statuses. Non-admin: search by message content",
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
    @PreAuthorize("hasAnyAuthority('CUSTOMER', 'SEER', 'ADMIN')")
    public ResponseEntity<PageResponse<ChatSessionResponse>> getChatHistory(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "sessionEndTime") String sortBy,
            @Parameter(description = "[ADMIN] Search by participant name (seer or customer)")
            @RequestParam(required = false) String participantName,
            @Parameter(description = "[ADMIN] Filter by conversation type")
            @RequestParam(required = false) Constants.ConversationTypeEnum conversationType,
            @Parameter(description = "[ADMIN] Filter by status (can provide multiple)")
            @RequestParam(required = false) List<Constants.ConversationStatusEnum> statuses,
            @Parameter(description = "[NON-ADMIN] Search by message content")
            @RequestParam(required = false) String messageContent
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);

        // Build filter request
        ChatHistoryFilterRequest filter = ChatHistoryFilterRequest.builder()
                .participantName(participantName)
                .conversationType(conversationType)
                .statuses(statuses)
                .messageContent(messageContent)
                .build();

        Page<ChatSessionResponse> history = conversationService.getChatHistory(filter, pageable);
        return responseFactory.successPage(history, "Chat history retrieved successfully");
    }
}
