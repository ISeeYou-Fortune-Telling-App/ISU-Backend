package com.iseeyou.fortunetelling.service.servicepackage.impl;

import com.iseeyou.fortunetelling.entity.*;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.PackageCategoryRepository;
import com.iseeyou.fortunetelling.repository.PackageInteractionRepository;
import com.iseeyou.fortunetelling.repository.ServicePackageRepository;
import com.iseeyou.fortunetelling.repository.ServiceReviewRepository;
import com.iseeyou.fortunetelling.service.fileupload.CloudinaryService;
import com.iseeyou.fortunetelling.service.knowledgecategory.KnowledgeCategoryService;
import com.iseeyou.fortunetelling.service.servicepackage.ServicePackageService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicePackageImpl implements ServicePackageService {

    private final ServicePackageRepository servicePackageRepository;
    private final PackageCategoryRepository packageCategoryRepository;
    private final PackageInteractionRepository packageInteractionRepository;
    private final ServiceReviewRepository serviceReviewRepository;
    private final KnowledgeCategoryService knowledgeCategoryService;
    private final CloudinaryService cloudinaryService;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackage> findAll(Pageable pageable) {
        return servicePackageRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicePackage findById(UUID id) {
        return servicePackageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ServicePackage not found with id: " + id));
    }

    @Override
    @Transactional
    public ServicePackage create(ServicePackage servicePackage, Set<UUID> categoryIds) {
        servicePackage.setSeer(userService.getUser());
        servicePackage.setStatus(Constants.PackageStatusEnum.AVAILABLE);
        servicePackage.setLikeCount(0L);
        servicePackage.setDislikeCount(0L);

        ServicePackage newServicePackage = servicePackageRepository.save(servicePackage);

        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<KnowledgeCategory> categories = knowledgeCategoryService.findAllByIds(categoryIds);

            Set<PackageCategory> packageCategories = new HashSet<>();
            for (KnowledgeCategory category : categories) {
                PackageCategory packageCategory = PackageCategory.builder()
                        .servicePackage(newServicePackage)
                        .knowledgeCategory(category)
                        .build();

                packageCategories.add(packageCategory);
            }

            packageCategoryRepository.saveAll(packageCategories);
            newServicePackage.setPackageCategories(packageCategories);
        }

        return newServicePackage;
    }

    @Override
    @Transactional
    public ServicePackage update(UUID id, ServicePackage servicePackage, Set<UUID> newCategoryIds) throws IOException {
        ServicePackage existingServicePackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ServicePackage not found with id: " + id));

        // Delete old image if a new one is being set
        if (existingServicePackage.getImageUrl() != null &&
            servicePackage.getImageUrl() != null &&
            !existingServicePackage.getImageUrl().equals(servicePackage.getImageUrl())) {
            cloudinaryService.deleteFile(existingServicePackage.getImageUrl());
        }

        updateServicePackageFields(existingServicePackage, servicePackage);

        if (newCategoryIds != null) {
            updatePackageCategories(existingServicePackage, newCategoryIds);
        }

        return existingServicePackage;
    }

    private void updateServicePackageFields(ServicePackage existing, ServicePackage updated) {
        existing.setPackageTitle(updated.getPackageTitle());
        existing.setPackageContent(updated.getPackageContent());
        existing.setImageUrl(updated.getImageUrl());
        existing.setDurationMinutes(updated.getDurationMinutes());
        existing.setPrice(updated.getPrice());
        existing.setStatus(updated.getStatus());
        existing.setRejectionReason(updated.getRejectionReason());
        existing.setLikeCount(updated.getLikeCount());
        existing.setDislikeCount(updated.getDislikeCount());
    }

    private void updatePackageCategories(ServicePackage servicePackage, Set<UUID> newCategoryIds) {
        // Get current category IDs
        Set<UUID> currentCategoryIds = servicePackage.getPackageCategories().stream()
                .map(pc -> pc.getKnowledgeCategory().getId())
                .collect(Collectors.toSet());

        // Find categories to remove and add
        Set<UUID> categoriesToRemove = currentCategoryIds.stream()
                .filter(id -> !newCategoryIds.contains(id))
                .collect(Collectors.toSet());

        Set<UUID> categoriesToAdd = newCategoryIds.stream()
                .filter(id -> !currentCategoryIds.contains(id))
                .collect(Collectors.toSet());

        // Remove relationships that are no longer needed
        if (!categoriesToRemove.isEmpty()) {
            packageCategoryRepository.deleteAllByServicePackage_IdAndKnowledgeCategory_IdIn(
                    servicePackage.getId(), categoriesToRemove);
            // Update collection in entity
            servicePackage.getPackageCategories().removeIf(
                    pc -> categoriesToRemove.contains(pc.getKnowledgeCategory().getId()));
        }

        // Add new relationships
        if (!categoriesToAdd.isEmpty()) {
            // Get all categories in one query
            List<KnowledgeCategory> categoriesToAddEntities = knowledgeCategoryService
                    .findAllByIds(categoriesToAdd);

            // Create new relationships
            Set<PackageCategory> newRelationships = new HashSet<>();
            for (KnowledgeCategory category : categoriesToAddEntities) {
                PackageCategory relationship = PackageCategory.builder()
                        .servicePackage(servicePackage)
                        .knowledgeCategory(category)
                        .build();

                newRelationships.add(relationship);
            }

            // Save all new relationships
            packageCategoryRepository.saveAll(newRelationships);

            // Update collection in entity
            servicePackage.getPackageCategories().addAll(newRelationships);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) throws IOException {
        ServicePackage servicePackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ServicePackage not found with id: " + id));

        // Delete the image from Cloudinary before deleting the service package
        if (servicePackage.getImageUrl() != null) {
            cloudinaryService.deleteFile(servicePackage.getImageUrl());
        }

        servicePackageRepository.delete(servicePackage);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackage> findByUserId(UUID userId, Pageable pageable) {
        return servicePackageRepository.findAllBySeer_Id(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackage> findByUserIdAndCategoryId(UUID userId, UUID categoryId, Pageable pageable) {
        return servicePackageRepository.findBySeerIdAndCategoryId(userId, categoryId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackage> findByCategoryId(UUID categoryId, Pageable pageable) {
        return servicePackageRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    @Transactional
    public void interactPackage(UUID packageId, boolean isLike) {
        ServicePackage servicePackage = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new NotFoundException("ServicePackage not found with id: " + packageId));

        User currentUser = userService.getUser();

        // Check if user has already interacted with this package
        Optional<PackageInteraction> existingInteraction = packageInteractionRepository
                .findByUser_IdAndServicePackage_Id(currentUser.getId(), packageId);

        if (existingInteraction.isPresent()) {
            PackageInteraction interaction = existingInteraction.get();
            boolean previousIsLike = interaction.isLike();

            // If the interaction type is different, update counters accordingly
            if (previousIsLike != isLike) {
                if (previousIsLike) {
                    // Was like, now dislike
                    servicePackage.setLikeCount(servicePackage.getLikeCount() - 1);
                    servicePackage.setDislikeCount(servicePackage.getDislikeCount() + 1);
                } else {
                    // Was dislike, now like
                    servicePackage.setDislikeCount(servicePackage.getDislikeCount() - 1);
                    servicePackage.setLikeCount(servicePackage.getLikeCount() + 1);
                }

                // Update the existing interaction
                interaction.setLike(isLike);
                packageInteractionRepository.save(interaction);
            }
            // If same interaction type, do nothing (user can't like/dislike twice)
        } else {
            // Create new interaction
            PackageInteraction newInteraction = PackageInteraction.builder()
                    .user(currentUser)
                    .servicePackage(servicePackage)
                    .isLike(isLike)
                    .build();

            packageInteractionRepository.save(newInteraction);

            // Update counters
            if (isLike) {
                servicePackage.setLikeCount(servicePackage.getLikeCount() + 1);
            } else {
                servicePackage.setDislikeCount(servicePackage.getDislikeCount() + 1);
            }
        }

        servicePackageRepository.save(servicePackage);
    }

    @Override
    @Transactional
    public void leaveComment(UUID packageId, UUID parentCommentId, String content) {
        // Validate that the service package exists
        ServicePackage servicePackage = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new NotFoundException("ServicePackage not found with id: " + packageId));

        User currentUser = userService.getUser();

        ServiceReview.ServiceReviewBuilder reviewBuilder = ServiceReview.builder()
                .comment(content)
                .servicePackage(servicePackage)
                .user(currentUser);

        // If parentCommentId is provided, validate it exists and set as parent
        if (parentCommentId != null) {
            ServiceReview parentComment = serviceReviewRepository.findById(parentCommentId)
                    .orElseThrow(() -> new NotFoundException("Parent comment not found with id: " + parentCommentId));

            reviewBuilder.parentReview(parentComment);
        }

        ServiceReview newReview = reviewBuilder.build();
        serviceReviewRepository.save(newReview);

        servicePackage.setCommentCount(servicePackage.getCommentCount() + 1);
        servicePackageRepository.save(servicePackage);
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) throws IOException {
        ServiceReview comment = serviceReviewRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        User currentUser = userService.getUser();

        // Check if the current user is the owner of the comment or admin
        if (!comment.getUser().getId().equals(currentUser.getId()) &&
            !currentUser.getRole().equals(Constants.RoleEnum.ADMIN)) {
            throw new IllegalArgumentException("You can only delete your own comments");
        }

        // Delete the comment (cascade will handle replies)
        serviceReviewRepository.delete(comment);
        log.info("Comment with id {} deleted by user {}", commentId, currentUser.getId());

        ServicePackage servicePackage = comment.getServicePackage();
        servicePackage.setCommentCount(servicePackage.getCommentCount() - 1);
        servicePackageRepository.save(servicePackage);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceReview> getCommentsByPackageId(UUID packageId, Pageable pageable) {
        // Validate that the service package exists
        servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new NotFoundException("ServicePackage not found with id: " + packageId));

        // Return only top-level comments (no parent) for the package
        return serviceReviewRepository.findTopLevelCommentsByPackageId(packageId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceReview> getRepliesByCommentId(UUID commentId, Pageable pageable) {
        // Validate that the parent comment exists
        serviceReviewRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        // Return all replies to the specified comment
        return serviceReviewRepository.findRepliesByParentCommentId(commentId, pageable);
    }
}
