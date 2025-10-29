package com.iseeyou.fortunetelling.dto.request.servicepackage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ServiceReviewRequest {
    @NotBlank(message = "Comment is required")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;

    // If present, this review is a reply to another review
    private UUID parentReviewId;
}

