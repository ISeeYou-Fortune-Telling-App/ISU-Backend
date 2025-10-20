package com.iseeyou.fortunetelling.service.servicepackage;

import com.iseeyou.fortunetelling.dto.response.servicepackage.PackageInteractionResponse;
import com.iseeyou.fortunetelling.util.Constants;

import java.util.UUID;

public interface PackageInteractionService {
    /**
     * Toggle like/dislike on a service package
     * If user clicks LIKE:
     *   - No existing interaction: Create LIKE
     *   - Existing LIKE: Remove interaction
     *   - Existing DISLIKE: Change to LIKE
     * If user clicks DISLIKE:
     *   - No existing interaction: Create DISLIKE
     *   - Existing DISLIKE: Remove interaction
     *   - Existing LIKE: Change to DISLIKE
     */
    PackageInteractionResponse toggleInteraction(UUID packageId, Constants.InteractionTypeEnum interactionType);

    /**
     * Get interaction stats for a package (like count, dislike count, user's current interaction)
     */
    PackageInteractionResponse getInteractionStats(UUID packageId);

    /**
     * Update service package like/dislike counts
     */
    void updatePackageCounts(UUID packageId);
}

