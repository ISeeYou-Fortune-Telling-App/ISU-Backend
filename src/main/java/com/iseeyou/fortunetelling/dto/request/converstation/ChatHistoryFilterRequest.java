package com.iseeyou.fortunetelling.dto.request.converstation;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryFilterRequest {
    private String participantName;// Search by participant name (seer or customer)
    private Constants.ConversationTypeEnum conversationType; // BOOKING_SESSION or AI SUPPORT
    private List<Constants.ConversationStatusEnum> statuses; // ACTIVE, ENDED, CANCELLED
    private String messageContent;
}
