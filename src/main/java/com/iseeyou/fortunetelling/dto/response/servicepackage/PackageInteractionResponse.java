package com.iseeyou.fortunetelling.dto.response.servicepackage;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageInteractionResponse {
    private Long likeCount;
    private Long dislikeCount;
    private String userInteraction; // LIKE, DISLIKE, or null
}

