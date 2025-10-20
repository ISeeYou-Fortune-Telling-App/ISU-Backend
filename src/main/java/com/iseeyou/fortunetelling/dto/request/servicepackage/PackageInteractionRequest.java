package com.iseeyou.fortunetelling.dto.request.servicepackage;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageInteractionRequest {
    @NotNull(message = "Interaction type is required")
    @Pattern(regexp = "LIKE|DISLIKE", message = "Interaction type must be either LIKE or DISLIKE")
    private String interactionType;
}

