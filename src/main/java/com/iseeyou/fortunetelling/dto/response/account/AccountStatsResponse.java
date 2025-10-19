package com.iseeyou.fortunetelling.dto.response.account;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatsResponse {
    private Long totalAccounts;
    private Long customerAccounts;
    private Long seerAccounts;
    private Long adminAccounts;
    private Long pendingAccounts; // UNVERIFIED_SEER
    private Long blockedAccounts;
}
