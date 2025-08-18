package com.iseeyou.fortunetelling.dto.response.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CustomerProfileResponse {
    private String zodiacSign;
    private String chineseZodiac;
    private String fiveElements;
}