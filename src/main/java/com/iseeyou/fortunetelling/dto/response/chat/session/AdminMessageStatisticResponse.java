package com.iseeyou.fortunetelling.dto.response.chat.session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminMessageStatisticResponse {
    private Long totalUsers;
    private Long totalActives;
    private Long totalSentMessages;
    private Double readPercent;
}
