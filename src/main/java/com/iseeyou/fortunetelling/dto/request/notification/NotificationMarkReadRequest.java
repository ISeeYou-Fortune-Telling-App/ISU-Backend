package com.iseeyou.fortunetelling.dto.request.notification;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class NotificationMarkReadRequest {
    @NotNull(message = "Notification IDs are required")
    private List<UUID> notificationIds;
}
