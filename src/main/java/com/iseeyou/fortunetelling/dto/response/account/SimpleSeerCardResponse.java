package com.iseeyou.fortunetelling.dto.response.account;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleSeerCardResponse {
    private UUID id;
    private String name;
    private String avatarUrl;
    private Double rating;
    private Double totalRates;
    private String profileDescription;
    private List<String> specialities;
}
