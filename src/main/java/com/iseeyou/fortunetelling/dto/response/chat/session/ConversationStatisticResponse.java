package com.iseeyou.fortunetelling.dto.response.chat.session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationStatisticResponse {
    private Integer bookingConversations;
    private Integer supportConversations;
    private Integer adminConversations;
    private Integer totalActives;
    private Long totalMessages;
}
