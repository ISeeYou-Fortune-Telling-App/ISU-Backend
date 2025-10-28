package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.chat.AdminCreateConversationRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.chat.session.ConversationResponse;
import com.iseeyou.fortunetelling.service.chat.ConversationService;
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

import java.util.UUID;

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

    @GetMapping
    @Operation(
            summary = "Get all admin conversations",
            description = "Get paginated list of all admin conversations",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<PageResponse<ConversationResponse>> getMyAdminConversations(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        log.info("Admin getting all conversations");

        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ConversationResponse> conversations = conversationService.getMyChatSessions(pageable);

        return responseFactory.successPage(conversations, "Conversations retrieved successfully");
    }

    @GetMapping("/{conversationId}")
    @Operation(
            summary = "Get admin conversation by ID",
            description = "Get detailed information of a specific admin conversation",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<SingleResponse<ConversationResponse>> getAdminConversation(
            @Parameter(description = "Conversation ID")
            @PathVariable UUID conversationId) {

        log.info("Admin getting conversation: {}", conversationId);

        ConversationResponse conversation = conversationService.getConversation(conversationId);

        return responseFactory.successSingle(conversation, "Conversation retrieved successfully");
    }
}

