package com.iseeyou.fortunetelling.service.servicepackage.impl;

import com.iseeyou.fortunetelling.dto.response.servicepackage.PackageInteractionResponse;
import com.iseeyou.fortunetelling.entity.servicepackage.PackageInteraction;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.repository.servicepackage.PackageInteractionRepository;
import com.iseeyou.fortunetelling.repository.servicepackage.ServicePackageRepository;
import com.iseeyou.fortunetelling.service.servicepackage.PackageInteractionService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageInteractionServiceImpl implements PackageInteractionService {
    private final PackageInteractionRepository interactionRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final UserService userService;

    @Override
    @Transactional
    public PackageInteractionResponse toggleInteraction(UUID packageId, Constants.InteractionTypeEnum interactionType) {
        User currentUser = userService.getUser();
        ServicePackage servicePackage = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new EntityNotFoundException("Service package not found with id: " + packageId));

        Optional<PackageInteraction> existingInteraction = 
                interactionRepository.findByUser_IdAndServicePackage_Id(currentUser.getId(), packageId);

        if (existingInteraction.isPresent()) {
            PackageInteraction interaction = existingInteraction.get();
            
            if (interaction.getInteractionType() == interactionType) {
                // Same type clicked again -> Remove interaction (toggle off)
                log.info("User {} removing {} from package {}", currentUser.getId(), interactionType, packageId);
                interactionRepository.delete(interaction);
            } else {
                // Different type clicked -> Change interaction
                log.info("User {} changing interaction from {} to {} on package {}", 
                        currentUser.getId(), interaction.getInteractionType(), interactionType, packageId);
                interaction.setInteractionType(interactionType);
                interactionRepository.save(interaction);
            }
        } else {
            // No existing interaction -> Create new one
            log.info("User {} adding {} to package {}", currentUser.getId(), interactionType, packageId);
            PackageInteraction newInteraction = PackageInteraction.builder()
                    .user(currentUser)
                    .servicePackage(servicePackage)
                    .interactionType(interactionType)
                    .build();
            interactionRepository.save(newInteraction);
        }

        // Update counts in service package
        updatePackageCounts(packageId);

        return getInteractionStats(packageId);
    }

    @Override
    @Transactional(readOnly = true)
    public PackageInteractionResponse getInteractionStats(UUID packageId) {
        if (!servicePackageRepository.existsById(packageId)) {
            throw new EntityNotFoundException("Service package not found with id: " + packageId);
        }

        User currentUser;
        String userInteraction = null;
        
        try {
            currentUser = userService.getUser();
            Optional<PackageInteraction> interaction = 
                    interactionRepository.findByUser_IdAndServicePackage_Id(currentUser.getId(), packageId);
            
            if (interaction.isPresent()) {
                userInteraction = interaction.get().getInteractionType().getValue();
            }
        } catch (Exception e) {
            // User not authenticated, just return counts
            log.debug("User not authenticated, returning only counts");
        }

        long likeCount = interactionRepository.countByServicePackage_IdAndInteractionType(
                packageId, Constants.InteractionTypeEnum.LIKE);
        long dislikeCount = interactionRepository.countByServicePackage_IdAndInteractionType(
                packageId, Constants.InteractionTypeEnum.DISLIKE);

        return PackageInteractionResponse.builder()
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .userInteraction(userInteraction)
                .build();
    }

    @Override
    @Transactional
    public void updatePackageCounts(UUID packageId) {
        ServicePackage servicePackage = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new EntityNotFoundException("Service package not found with id: " + packageId));

        long likeCount = interactionRepository.countByServicePackage_IdAndInteractionType(
                packageId, Constants.InteractionTypeEnum.LIKE);
        long dislikeCount = interactionRepository.countByServicePackage_IdAndInteractionType(
                packageId, Constants.InteractionTypeEnum.DISLIKE);

        servicePackage.setLikeCount(likeCount);
        servicePackage.setDislikeCount(dislikeCount);
        servicePackageRepository.save(servicePackage);

        log.debug("Updated package {} counts: {} likes, {} dislikes", packageId, likeCount, dislikeCount);
    }
}

