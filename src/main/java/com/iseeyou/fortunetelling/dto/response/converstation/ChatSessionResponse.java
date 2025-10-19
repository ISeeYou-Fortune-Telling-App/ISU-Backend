package com.iseeyou.fortunetelling.dto.response.converstation;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionResponse extends AbstractBaseDataResponse {
    private UUID bookingId;
    private String seerName;
    private String customerName;
    private LocalDateTime sessionStartTime;
    private LocalDateTime sessionEndTime;
    private Integer sessionDurationMinutes;
    private Constants.ConversationStatusEnum status;
    private String servicePackageName;
}
