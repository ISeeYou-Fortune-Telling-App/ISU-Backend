package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.message.ChatMessageRequest;
import com.iseeyou.fortunetelling.dto.request.message.MessageDeleteRequest;
import com.iseeyou.fortunetelling.dto.request.message.MessageRecallRequest;
import com.iseeyou.fortunetelling.dto.response.ChatMessageResponse;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.service.message.MessageService;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/conversations/{conversationId}/messages")
@Tag(name = "005. Message", description = "Message API")
@Slf4j
public class MessageController extends AbstractBaseController {

    private final MessageService messageService;

    @PostMapping
    @Operation(
            summary = "Send a message in a conversation",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Message sent successfully"),
                    @ApiResponse(responseCode = "404", description = "Conversation not found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<SingleResponse<ChatMessageResponse>> sendMessage(
            @PathVariable UUID conversationId,
            @RequestBody @Valid ChatMessageRequest request
    ) {
        ChatMessageResponse response = messageService.sendMessage(conversationId, request);
        return responseFactory.successSingle(response, "Message sent successfully");
    }

    @GetMapping
    @Operation(
            summary = "Get visible messages (Redis-filtered deleted messages)",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<PageResponse<ChatMessageResponse>> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "desc") String sortType,
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ChatMessageResponse> messages = messageService.getMessages(conversationId, pageable);
        return responseFactory.successPage(messages, "Messages retrieved successfully");
    }

    @PatchMapping("/{messageId}/read")
    @Operation(
            summary = "Mark message as read",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<SingleResponse<String>> markAsRead(
            @PathVariable UUID conversationId,
            @PathVariable UUID messageId
    ) {
        messageService.markMessageAsRead(messageId);
        return responseFactory.successSingle("Message marked as read", "Message marked as read");
    }

    @DeleteMapping("/delete")
    @Operation(
            summary = "Soft delete messages (cached in Redis for 30s undo)",
            description = "Delete messages and cache in Redis. Auto-expires after 30 seconds.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<SingleResponse<Map<String, Object>>> deleteMessages(
            @PathVariable UUID conversationId,
            @RequestBody @Valid MessageDeleteRequest request
    ) {
        messageService.softDeleteMessages(conversationId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("deletedCount", request.getMessageIds().size());
        result.put("canUndo", true);
        result.put("undoTimeoutSeconds", 30);
        result.put("message", "Messages deleted. You can undo within 30 seconds.");

        return responseFactory.successSingle(result, "Messages deleted successfully");
    }

    @PostMapping("/undo-delete")
    @Operation(
            summary = "Undo delete (retrieves from Redis cache)",
            description = "Restore recently deleted messages using Redis cache. Only works within 30 seconds.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<SingleResponse<String>> undoDeleteMessages(
            @PathVariable UUID conversationId
    ) {
        messageService.undoDeleteMessages(conversationId);
        return responseFactory.successSingle(
                "Messages restored successfully",
                "Undo completed"
        );
    }

    @GetMapping("/undo-status")
    @Operation(
            summary = "Check undo availability and remaining time",
            description = "Check if user can undo delete and how much time remains",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<SingleResponse<Map<String, Object>>> getUndoStatus(
            @PathVariable UUID conversationId
    ) {
        Long remainingSeconds = messageService.getRemainingUndoTime(conversationId);

        Map<String, Object> status = new HashMap<>();
        status.put("canUndo", remainingSeconds > 0);
        status.put("remainingSeconds", remainingSeconds);

        return responseFactory.successSingle(status, "Undo status retrieved");
    }

    @DeleteMapping("/batch-delete")
    @Operation(
            summary = "Batch delete (merges with existing Redis cache)",
            description = "Delete multiple messages. If cache exists, append and refresh TTL.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<SingleResponse<Map<String, Object>>> batchDeleteMessages(
            @PathVariable UUID conversationId,
            @RequestBody @Valid MessageDeleteRequest request
    ) {
        messageService.batchSoftDeleteMessages(conversationId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("deletedCount", request.getMessageIds().size());
        result.put("canUndo", true);
        result.put("undoTimeoutSeconds", 30);

        return responseFactory.successSingle(result, "Batch delete completed");
    }

    @PostMapping("/recall")
    @Operation(
            summary = "Recall messages (Delete for Everyone)",
            description = "Permanently delete messages for ALL participants. " +
                    "Only sender can recall their own messages within time limit (default: 15 minutes).",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Messages recalled successfully",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Cannot recall (not sender, too old, or already recalled)",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Messages not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<Map<String, Object>>> recallMessages(
            @PathVariable UUID conversationId,
            @RequestBody @Valid MessageRecallRequest request
    ) {
        messageService.recallMessages(conversationId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("recalledCount", request.getMessageIds().size());
        result.put("message", "Messages recalled successfully. All participants can no longer see these messages.");

        return responseFactory.successSingle(result, "Messages recalled successfully");
    }
}
