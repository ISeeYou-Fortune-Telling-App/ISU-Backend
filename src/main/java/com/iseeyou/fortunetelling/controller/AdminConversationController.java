package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.chat.session.AdminCreateConversationRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.chat.session.ConversationResponse;
import com.iseeyou.fortunetelling.dto.response.chat.session.ConversationStatisticResponse;
import com.iseeyou.fortunetelling.service.chat.ConversationService;
import com.iseeyou.fortunetelling.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequestMapping("/admin/conversations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "011. Admin Conversation", description = "Admin conversation management APIs")
public class AdminConversationController extends AbstractBaseController {

    private final ConversationService conversationService;

    @PostMapping
    @Operation(
            summary = "Create admin conversation with any user",
            description = "Admin can create a conversation with any customer or seer",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<SingleResponse<ConversationResponse>> createAdminConversation(
            @Valid @RequestBody AdminCreateConversationRequest request) {

        log.info("Admin creating conversation with user: {}", request.getTargetUserId());

        ConversationResponse conversation = conversationService.createAdminConversation(
                request.getTargetUserId(),
                request.getInitialMessage()
        );

        return responseFactory.successSingle(conversation, "Admin conversation created successfully");
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search all conversations with filters",
            description = "Admin can search conversations by participant name (customer or seer), type, and status",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<PageResponse<ConversationResponse>> getAllChatSessionsWithFilters(
            @Parameter(description = "Page number (1-indexed)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "sessionStartTime") String sortBy,
            @Parameter(description = "Participant name (customer or seer) - partial match") @RequestParam(required = false) String participantName,
            @Parameter(description = "Conversation type (BOOKING_SESSION or ADMIN_CHAT)") @RequestParam(required = false) String type,
            @Parameter(description = "Conversation status") @RequestParam(required = false) String status) {

        log.info("Admin searching conversations: participantName={}, type={}, status={}",
                participantName, type, status);

        Pageable pageable = createPageable(page, size, sortType, sortBy);

        // Parse enum values
        Constants.ConversationTypeEnum typeEnum = null;
        if (type != null && !type.trim().isEmpty()) {
            try {
                typeEnum = Constants.ConversationTypeEnum.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid conversation type: {}", type);
            }
        }

        Constants.ConversationStatusEnum statusEnum = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                statusEnum = Constants.ConversationStatusEnum.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid conversation status: {}", status);
            }
        }

        Page<ConversationResponse> conversations = conversationService.getAllChatSessionsWithFilters(
                pageable,
                participantName,
                typeEnum,
                statusEnum
        );

        return responseFactory.successPage(conversations, "Conversations retrieved successfully");
    }

    @GetMapping("/statistics")
    @Operation(
            summary = "Get conversation statistics",
            description = "Admin can view statistics about conversations including booking, admin, and support conversations",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<SingleResponse<ConversationStatisticResponse>> getConversationStatistics() {
        log.info("Admin retrieving conversation statistics");

        ConversationStatisticResponse statistics = conversationService.getConversationStatistics();

        return responseFactory.successSingle(statistics, "Conversation statistics retrieved successfully");
    }

}

