package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.chat.ChatMessageRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.SuccessResponse;
import com.iseeyou.fortunetelling.dto.response.chat.session.ChatMessageResponse;
import com.iseeyou.fortunetelling.dto.response.chat.session.ConversationResponse;
import com.iseeyou.fortunetelling.service.chat.ConversationService;
import com.iseeyou.fortunetelling.service.chat.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@Tag(name = "010. Chat", description = "Chat API")
@Slf4j
public class ChatController extends AbstractBaseController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    @GetMapping("/conversations")
    @Operation(summary = "Get my conversations", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    public ResponseEntity<PageResponse<ConversationResponse>> getMyChatSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "desc") String sortType,
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ConversationResponse> conversations = conversationService.getMyChatSessions(pageable);
        return responseFactory.successPage(conversations, "Conversations retrieved successfully");
    }

    @GetMapping("/conversations/{conversationId}")
    @Operation(summary = "Get conversation by ID", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    public ResponseEntity<SingleResponse<ConversationResponse>> getConversation(@PathVariable UUID conversationId) {
        ConversationResponse conversation = conversationService.getConversation(conversationId);
        return responseFactory.successSingle(conversation, "Conversation retrieved successfully");
    }

    @GetMapping("/conversations/booking/{bookingId}")
    @Operation(summary = "Get conversation by booking ID", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    public ResponseEntity<SingleResponse<ConversationResponse>> getConversationByBookingId(@PathVariable UUID bookingId) {
        ConversationResponse conversation = conversationService.getChatSessionByBookingId(bookingId);
        return responseFactory.successSingle(conversation, "Conversation retrieved successfully");
    }

    @PostMapping("/conversations/booking/{bookingId}")
    @Operation(summary = "Create chat session", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    public ResponseEntity<SingleResponse<ConversationResponse>> createChatSession(@PathVariable UUID bookingId) {
        ConversationResponse conversation = conversationService.createChatSession(bookingId);
        return responseFactory.successSingle(conversation, "Chat session created successfully");
    }

    @PostMapping("/conversations/{conversationId}/end")
    @Operation(summary = "End chat session", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    public ResponseEntity<SuccessResponse> endChatSession(@PathVariable UUID conversationId) {
        conversationService.endChatSession(conversationId);
        return ResponseEntity.ok(SuccessResponse.builder().statusCode(200).message("Chat session ended successfully").build());
    }

    @PostMapping("/conversations/{conversationId}/extend")
    @Operation(summary = "Extend chat session", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    public ResponseEntity<SuccessResponse> extendChatSession(
            @PathVariable UUID conversationId,
            @RequestParam Integer additionalMinutes
    ) {
        conversationService.extendSession(conversationId, additionalMinutes);
        return ResponseEntity.ok(SuccessResponse.builder().statusCode(200).message("Chat session extended").build());
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "Get messages", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    public ResponseEntity<PageResponse<ChatMessageResponse>> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "desc") String sortType,
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ChatMessageResponse> messages = messageService.getMessages(conversationId, pageable);
        return responseFactory.successPage(messages, "Messages retrieved successfully");
    }

    @PostMapping(value = "/messages", consumes = {"multipart/form-data"})
    @Operation(summary = "Send message", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    public ResponseEntity<SingleResponse<ChatMessageResponse>> sendMessage(@Valid @ModelAttribute ChatMessageRequest request) {
        ChatMessageResponse message = messageService.sendMessage(request.getConversationId(), request);
        return responseFactory.successSingle(message, "Message sent successfully");
    }

    @PostMapping("/conversations/{conversationId}/mark-read")
    @Operation(summary = "Mark messages as read", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    public ResponseEntity<SuccessResponse> markMessagesAsRead(@PathVariable UUID conversationId) {
        messageService.markMessagesAsRead(conversationId);
        return ResponseEntity.ok(SuccessResponse.builder().statusCode(200).message("Messages marked as read").build());
    }

    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "Delete message", security = @SecurityRequirement(name = SECURITY_SCHEME_NAME))
    public ResponseEntity<SuccessResponse> deleteMessage(@PathVariable UUID messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok(SuccessResponse.builder().statusCode(200).message("Message deleted successfully").build());
    }
}

