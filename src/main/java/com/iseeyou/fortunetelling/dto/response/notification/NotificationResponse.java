package com.iseeyou.fortunetelling.dto.response.notification;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class NotificationResponse extends AbstractBaseDataResponse {
    private Constants.NotificationTypeEnum notificationType;
    private String notificationTitle;
    private String notificationBody;
    private Boolean isRead;
    private RecipientInfo recipient;

    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    public static class RecipientInfo {
        private String fullName;
        private String avatarUrl;
        private String email;
    }
}
