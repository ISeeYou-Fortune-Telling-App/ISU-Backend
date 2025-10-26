package com.iseeyou.fortunetelling.dto.request.notification;

import com.iseeyou.fortunetelling.util.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class NotificationCreateRequest {
    @NotNull(message = "Notification type is required")
    private Constants.NotificationTypeEnum notificationType;

    @NotBlank(message = "Notification title is required")
    private String notificationTitle;

    @NotBlank(message = "Notification body is required")
    private String notificationBody;

    @NotNull(message = "Recipient ID is required")
    private UUID recipientId;
}
