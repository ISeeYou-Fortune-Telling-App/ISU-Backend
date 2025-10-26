package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.notification.NotificationCreateRequest;
import com.iseeyou.fortunetelling.dto.request.notification.NotificationMarkReadRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.dto.response.notification.NotificationResponse;
import com.iseeyou.fortunetelling.entity.Notification;
import com.iseeyou.fortunetelling.mapper.NotificationMapper;
import com.iseeyou.fortunetelling.service.notification.NotificationService;
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

import java.util.Map;
import java.util.UUID;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
@Tag(name = "006. Notification", description = "Notification API")
@Slf4j
public class NotificationController extends AbstractBaseController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    @GetMapping
    @Operation(
            summary = "Get my notifications with pagination and filters (UC6.1)",
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
    public ResponseEntity<PageResponse<NotificationResponse>> getMyNotifications(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "15") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Filter by notification type")
            @RequestParam(required = false) Constants.NotificationTypeEnum type,
            @Parameter(description = "Filter by read status")
            @RequestParam(required = false) Boolean isRead
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<Notification> notifications;

        if (type != null && isRead != null) {
            notifications = notificationService.getMyNotificationsByTypeAndReadStatus(type, isRead, pageable);
        } else if (type != null) {
            notifications = notificationService.getMyNotificationsByType(type, pageable);
        } else if (isRead != null) {
            notifications = notificationService.getMyNotificationsByReadStatus(isRead, pageable);
        } else {
            notifications = notificationService.getMyNotifications(pageable);
        }

        Page<NotificationResponse> response = notificationMapper.mapToPage(notifications, NotificationResponse.class);
        return responseFactory.successPage(response, "Notifications retrieved successfully");
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get notification by ID",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Notification not found",
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
    public ResponseEntity<SingleResponse<NotificationResponse>> getNotificationById(
            @Parameter(description = "Notification ID", required = true)
            @PathVariable UUID id
    ) {
        Notification notification = notificationService.findById(id);

        // Auto-mark as read when viewing detail
        if (!notification.getIsRead()) {
            notificationService.markAsRead(id);
        }

        NotificationResponse response = notificationMapper.mapTo(notification, NotificationResponse.class);
        return responseFactory.successSingle(response, "Notification retrieved successfully");
    }

    @GetMapping("/unread-count")
    @Operation(
            summary = "Get unread notification count",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
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
    public ResponseEntity<SingleResponse<Long>> getUnreadCount() {
        Long count = notificationService.getUnreadCount();
        return responseFactory.successSingle(count, "Unread count retrieved successfully");
    }

    @PatchMapping("/{id}/mark-read")
    @Operation(
            summary = "Mark notification as read",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Notification marked as read",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Notification not found",
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
    public ResponseEntity<SingleResponse<String>> markNotificationAsRead(
            @Parameter(description = "Notification ID", required = true)
            @PathVariable UUID id
    ) {
        notificationService.markAsRead(id);
        return responseFactory.successSingle("Notification marked as read", "Notification marked as read successfully");
    }

    @PatchMapping("/mark-read-batch")
    @Operation(
            summary = "Mark multiple notifications as read",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Notifications marked as read",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
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
    public ResponseEntity<SingleResponse<String>> markMultipleNotificationsAsRead(
            @Parameter(description = "List of notification IDs to mark as read", required = true)
            @RequestBody @Valid NotificationMarkReadRequest request
    ) {
        notificationService.markMultipleAsRead(request.getNotificationIds());
        return responseFactory.successSingle("Notifications marked as read", "Notifications marked as read successfully");
    }

    @PatchMapping("/mark-all-read")
    @Operation(
            summary = "Mark all notifications as read with undo support",
            description = "Mark all unread notifications as read. Returns an undo token valid for 10 seconds.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "All notifications marked as read",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
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
    public ResponseEntity<SingleResponse<Map<String, String>>> markAllNotificationsAsRead() {
        String undoToken = notificationService.markAllAsReadWithUndo();

        Map<String, String> result = Map.of(
                "message", "All notifications marked as read",
                "undoToken", undoToken != null ? undoToken : "",
                "undoExpiresIn", "10 seconds"
        );

        return responseFactory.successSingle(result, "All notifications marked as read successfully");
    }

    @PostMapping("/undo-mark-all")
    @Operation(
            summary = "Undo mark all as read",
            description = "Undo the mark all as read operation within 10 seconds",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Undo successful",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SingleResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Undo token expired or invalid",
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
    public ResponseEntity<SingleResponse<String>> undoMarkAllAsRead(
            @Parameter(description = "Undo token received from mark-all-read endpoint", required = true)
            @RequestParam String undoToken
    ) {
        notificationService.undoMarkAllAsRead(undoToken);
        return responseFactory.successSingle("Undo successful", "Notifications restored to unread status");
    }

    // FOR ADMIN
    @GetMapping("/admin/all")
    @Operation(
            summary = "[ADMIN] Get all notifications",
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
                            responseCode = "403",
                            description = "Forbidden - Admin only",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<PageResponse<NotificationResponse>> getAllNotifications(
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
        Page<Notification> notifications = notificationService.getAllNotifications(pageable);
        Page<NotificationResponse> response = notificationMapper.mapToPage(notifications, NotificationResponse.class);
        return responseFactory.successPage(response, "All notifications retrieved successfully");
    }

    @GetMapping("/admin/user/{userId}")
    @Operation(
            summary = "[ADMIN] Get notifications by user ID",
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
                            responseCode = "403",
                            description = "Forbidden - Admin only",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<PageResponse<NotificationResponse>> getNotificationsByUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId,
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
        Page<Notification> notifications = notificationService.getAllNotificationsByRecipient(userId, pageable);
        Page<NotificationResponse> response = notificationMapper.mapToPage(notifications, NotificationResponse.class);
        return responseFactory.successPage(response, "User notifications retrieved successfully");
    }

    @PostMapping("/admin/create")
    @Operation(
            summary = "[ADMIN] Create notification for a user",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Notification created successfully",
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
                            responseCode = "403",
                            description = "Forbidden - Admin only",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<NotificationResponse>> createNotification(
            @Parameter(description = "Notification data", required = true)
            @RequestBody @Valid NotificationCreateRequest request
    ) {
        Notification notification = notificationService.createNotification(request);
        NotificationResponse response = notificationMapper.mapTo(notification, NotificationResponse.class);
        return responseFactory.successSingle(response, "Notification created successfully");
    }
}
