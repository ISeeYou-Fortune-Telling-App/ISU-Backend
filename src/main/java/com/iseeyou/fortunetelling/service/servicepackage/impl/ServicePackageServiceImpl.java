package com.iseeyou.fortunetelling.service.servicepackage.impl;

import com.iseeyou.fortunetelling.dto.request.servicepackage.ServicePackageUpsertRequest;
import com.iseeyou.fortunetelling.dto.response.ServicePackageDetailResponse;
import com.iseeyou.fortunetelling.dto.response.servicepackage.ServicePackageResponse;
import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.entity.servicepackage.PackageCategory;
import com.iseeyou.fortunetelling.entity.servicepackage.PackageInteraction;
import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeCategory;
import com.iseeyou.fortunetelling.entity.servicepackage.ServiceReview;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.entity.user.SeerProfile;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.mapper.ServicePackageMapper;
import com.iseeyou.fortunetelling.repository.booking.BookingRepository;
import com.iseeyou.fortunetelling.repository.servicepackage.ServicePackageRepository;
import com.iseeyou.fortunetelling.repository.servicepackage.ServicePackageSpecification;
import com.iseeyou.fortunetelling.repository.servicepackage.PackageInteractionRepository;
import com.iseeyou.fortunetelling.repository.knowledge.KnowledgeCategoryRepository;
import com.iseeyou.fortunetelling.repository.servicepackage.ServiceReviewRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
import com.iseeyou.fortunetelling.service.fileupload.CloudinaryService;
import com.iseeyou.fortunetelling.service.servicepackage.ServicePackageService;
import com.iseeyou.fortunetelling.service.booking.BookingService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.config.CloudinaryConfig;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ServicePackageServiceImpl implements ServicePackageService {

    private final ServicePackageRepository servicePackageRepository;
    private final KnowledgeCategoryRepository knowledgeCategoryRepository;
    private final CloudinaryService cloudinaryService;
    private final PackageInteractionRepository interactionRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ServicePackageMapper servicePackageMapper;
    private final BookingService bookingService;
    private final TransactionTemplate transactionTemplate;
    private final ServiceReviewRepository serviceReviewRepository;

    // Constructor with @Lazy for BookingService to prevent circular dependency
    public ServicePackageServiceImpl(
            ServicePackageRepository servicePackageRepository,
            KnowledgeCategoryRepository knowledgeCategoryRepository,
            CloudinaryService cloudinaryService,
            PackageInteractionRepository interactionRepository,
            BookingRepository bookingRepository,
            UserService userService,
            ServicePackageMapper servicePackageMapper,
            @Lazy BookingService bookingService,
            TransactionTemplate transactionTemplate,
            ServiceReviewRepository serviceReviewRepository) {
        this.servicePackageRepository = servicePackageRepository;
        this.knowledgeCategoryRepository = knowledgeCategoryRepository;
        this.cloudinaryService = cloudinaryService;
        this.interactionRepository = interactionRepository;
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.servicePackageMapper = servicePackageMapper;
        this.bookingService = bookingService;
        this.transactionTemplate = transactionTemplate;
        this.serviceReviewRepository = serviceReviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackage> findAllAvailable(Pageable pageable) {
        Specification<ServicePackage> spec = ServicePackageSpecification.availableOnly();
        return servicePackageRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackage> findAvailableWithFilters(String name, String categoryIds, Double minPrice, Double maxPrice, Integer minDuration, Integer maxDuration, Pageable pageable) {
        Specification<ServicePackage> spec = ServicePackageSpecification.withFilters(
                name, categoryIds, minPrice, maxPrice, minDuration, maxDuration);
        return servicePackageRepository.findAll(spec, pageable);
    }

    @Override
    public Page<ServicePackage> findAvailableByCategoryWithFilters(Constants.ServiceCategoryEnum category, Double minPrice, Double maxPrice, Pageable pageable) {
        Specification<ServicePackage> spec = (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            // Status filter
            predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.equal(root.get("status"), Constants.PackageStatusEnum.AVAILABLE));

            // Category filter through packageCategories relationship
            if (category != null) {
                var categoryJoin = root.join("packageCategories");
                var knowledgeCategoryJoin = categoryJoin.join("knowledgeCategory");
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(knowledgeCategoryJoin.get("name"), category.getValue()));
            }

            // Price filters
            if (minPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return predicates;
        };
        return servicePackageRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicePackage findById(String id) {
        return servicePackageRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Service package not found with id: " + id));
    }

    @Override
    @Transactional
    public ServicePackage createOrUpdatePackage(ServicePackageUpsertRequest request) throws IOException {
        // Create new package (no ID provided)
        return createOrUpdatePackageInternal(null, request);
    }

    @Override
    @Transactional
    public ServicePackage createOrUpdatePackage(String id, ServicePackageUpsertRequest request) throws IOException {
        // Update existing package (ID provided)
        return createOrUpdatePackageInternal(id, request);
    }

    private ServicePackage createOrUpdatePackageInternal(String packageId, ServicePackageUpsertRequest request) throws IOException {
        ServicePackage servicePackage;
        User seer = userService.getUser(); // Get current user from JWT

        if (StringUtils.hasText(packageId)) {
            // Update existing package
            Optional<ServicePackage> optional = servicePackageRepository.findById(UUID.fromString(packageId));
            servicePackage = optional.orElseThrow(() -> new NotFoundException("Service package not found"));

            // Verify that the current user owns this package
            if (!servicePackage.getSeer().getId().equals(seer.getId())) {
                throw new IllegalArgumentException("You can only update your own service packages");
            }
        } else {
            // Create new package
            servicePackage = new ServicePackage();
            servicePackage.setStatus(Constants.PackageStatusEnum.HIDDEN); // trạng thái chờ duyệt
            servicePackage.setRejectionReason(null);
            servicePackage.setLikeCount(0L);
            servicePackage.setDislikeCount(0L);
            servicePackage.setCommentCount(0L);
            servicePackage.setPackageCategories(new HashSet<>());
        }

        servicePackage.setSeer(seer);
        servicePackage.setPackageTitle(request.getPackageTitle());
        servicePackage.setPackageContent(request.getPackageContent());
        servicePackage.setDurationMinutes(request.getDurationMinutes());
        servicePackage.setPrice(request.getPrice());

        // Handle multiple categories assignment
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            // Clear existing categories if updating
            servicePackage.getPackageCategories().clear();

            // Add all categories from the list
            for (String categoryIdStr : request.getCategoryIds()) {
                UUID categoryId = UUID.fromString(categoryIdStr);

                // Find the knowledge category by ID
                KnowledgeCategory knowledgeCategory = knowledgeCategoryRepository
                        .findById(categoryId)
                        .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryIdStr));

                // Create package category relationship
                PackageCategory packageCategory = PackageCategory.builder()
                        .servicePackage(servicePackage)
                        .knowledgeCategory(knowledgeCategory)
                        .build();

                servicePackage.getPackageCategories().add(packageCategory);
            }

            log.info("Added {} categories to service package", request.getCategoryIds().size());
        }

        // Handle optional image upload - only update if new image is provided
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imageUrl = uploadImage(request.getImage());
            servicePackage.setImageUrl(imageUrl);
        }
        return servicePackageRepository.save(servicePackage);
    }

    @Override
    public String uploadImage(MultipartFile image) throws IOException {
        return cloudinaryService.uploadFile(image, "service_packages");
    }

    // ===== Service review methods (merged) =====
    @Override
    @Transactional(readOnly = true)
    public Page<ServiceReview> getTopLevelReviewsByPackage(java.util.UUID packageId, Pageable pageable) {
        return serviceReviewRepository.findTopLevelCommentsByPackageId(packageId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceReview getReviewById(java.util.UUID id) {
        return serviceReviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service review not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceReview> getReplies(java.util.UUID reviewId, Pageable pageable) {
        return serviceReviewRepository.findRepliesByParentCommentId(reviewId, pageable);
    }

    @Override
    @Transactional
    public ServiceReview createReview(java.util.UUID packageId, ServiceReview review) {
        ServicePackage servicePackage = findById(packageId.toString());
        review.setServicePackage(servicePackage);

        if (review.getUser() == null) {
            review.setUser(userService.getUser());
        }

        if (review.getParentReview() != null && review.getParentReview().getId() != null) {
            java.util.UUID parentId = review.getParentReview().getId();
            ServiceReview parent = serviceReviewRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Parent review not found with id: " + parentId));
            if (!parent.getServicePackage().getId().equals(servicePackage.getId())) {
                throw new IllegalArgumentException("Parent review belongs to a different package");
            }
            review.setParentReview(parent);
        }

        ServiceReview saved = serviceReviewRepository.save(review);

        // update comment count on package if top-level
        if (saved.getParentReview() == null) {
            servicePackage.setCommentCount(servicePackage.getCommentCount() == null ? 1L : servicePackage.getCommentCount() + 1);
            servicePackageRepository.save(servicePackage);
        }

        return saved;
    }

    @Override
    @Transactional
    public ServiceReview updateReview(java.util.UUID id, ServiceReview review) {
        ServiceReview existing = serviceReviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service review not found with id: " + id));

        User current = userService.getUser();
        boolean isOwner = existing.getUser() != null && existing.getUser().getId().equals(current.getId());
        boolean isAdmin = current.getRole().equals(Constants.RoleEnum.ADMIN);

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the comment owner or admin can update this review");
        }

        if (review.getComment() != null) existing.setComment(review.getComment());

        return serviceReviewRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteReview(java.util.UUID id) {
        ServiceReview existing = serviceReviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service review not found with id: " + id));

        User current = userService.getUser();
        boolean isOwner = existing.getUser() != null && existing.getUser().getId().equals(current.getId());
        boolean isAdmin = current.getRole().equals(Constants.RoleEnum.ADMIN);

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Only the comment owner or admin can delete this review");
        }

        ServicePackage sp = existing.getServicePackage();
        boolean wasTopLevel = existing.getParentReview() == null;

        serviceReviewRepository.delete(existing);

        if (wasTopLevel && sp != null) {
            sp.setCommentCount(sp.getCommentCount() == null || sp.getCommentCount() <= 0 ? 0L : sp.getCommentCount() - 1);
            servicePackageRepository.save(sp);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ServicePackageDetailResponse findDetailById(String id) {
        ServicePackage servicePackage = servicePackageRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Service package not found with id: " + id));

        User seer = servicePackage.getSeer();
        if (seer == null) {
            throw new NotFoundException("Seer not found for service package: " + id);
        }

        SeerProfile seerProfile = seer.getSeerProfile();

        // Tạo SeerInfo
        ServicePackageDetailResponse.SeerInfo seerInfo = ServicePackageDetailResponse.SeerInfo.builder()
                .seerId(seer.getId().toString())
                .fullName(seer.getFullName())
                .email(seer.getEmail())
                .phone(seer.getPhone())
                .avatarUrl(seer.getAvatarUrl())
                .coverUrl(seer.getCoverUrl())
                .profileDescription(seer.getProfileDescription())
                .avgRating(seerProfile != null ? seerProfile.getAvgRating() : 0.0)
                .totalRates(seerProfile != null ? seerProfile.getTotalRates() : 0)
                .paymentInfo(seerProfile != null ? seerProfile.getPaymentInfo() : null)
                .build();

        // Get category information from packageCategories
        Constants.ServiceCategoryEnum categoryEnum = servicePackage.getPackageCategories().stream()
                .findFirst()
                .map(pc -> {
                    String categoryName = pc.getKnowledgeCategory().getName();
                    try {
                        return Constants.ServiceCategoryEnum.get(categoryName);
                    } catch (IllegalArgumentException e) {
                        log.warn("Unknown category name: {}", categoryName);
                        return null;
                    }
                })
                .orElse(null);

        // Get review statistics
        UUID packageId = servicePackage.getId();
        Long totalReviews = bookingRepository.countReviewsByServicePackageId(packageId);
        Double avgRating = bookingRepository.getAverageRatingByServicePackageId(packageId);

        // Get reviews (latest 10 reviews by default)
        Pageable reviewPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "reviewedAt"));
        Page<Booking> reviewsPage = bookingRepository.findReviewsByServicePackageId(packageId, reviewPageable);

        // Tạo ServicePackageDetailResponse
        return ServicePackageDetailResponse.builder()
                .packageId(servicePackage.getId().toString())
                .packageTitle(servicePackage.getPackageTitle())
                .packageContent(servicePackage.getPackageContent())
                .imageUrl(servicePackage.getImageUrl())
                .durationMinutes(servicePackage.getDurationMinutes())
                .price(servicePackage.getPrice())
                .category(categoryEnum)
                .status(servicePackage.getStatus().getValue())
                .rejectionReason(servicePackage.getRejectionReason())
                .createdAt(servicePackage.getCreatedAt())
                .updatedAt(servicePackage.getUpdatedAt())
                .avgRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .seer(seerInfo)
                .build();
    }

    @Override
    @Transactional
    public void deleteServicePackage(String id) {
        log.info("Starting soft delete process for service package {}", id);

        // 1. Find service package
        ServicePackage servicePackage = servicePackageRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException("Service package not found with id: " + id));

        // 2. Check if already deleted
        if (servicePackage.getDeletedAt() != null) {
            throw new IllegalArgumentException("Service package is already deleted");
        }

        // 3. Validate permission - seer can only delete their own packages, admin can delete any
        User currentUser = userService.getUser();
        boolean isSeerOwner = servicePackage.getSeer() != null &&
                servicePackage.getSeer().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().equals(Constants.RoleEnum.ADMIN);

        if (!isSeerOwner && !isAdmin) {
            throw new IllegalArgumentException(
                    "Access denied: Only the package owner or admin can delete this service package"
            );
        }

        // 4. Find all bookings for this service package that are not completed
        List<Booking> incompleteBookings = servicePackage.getBookings().stream()
                .filter(booking -> !booking.getStatus().equals(Constants.BookingStatusEnum.COMPLETED))
                .collect(Collectors.toList());

        log.info("Found {} incomplete bookings for service package {}", incompleteBookings.size(), id);

        // 5. Refund incomplete bookings
        // Use separate transactions for each refund to prevent rollback contamination
        int successfulRefunds = 0;
        int failedRefunds = 0;
        List<String> refundErrors = new java.util.ArrayList<>();

        for (Booking booking : incompleteBookings) {
            log.info("Processing refund for booking {} (status: {})", booking.getId(), booking.getStatus());

            // Check if booking is already canceled or failed
            if (booking.getStatus().equals(Constants.BookingStatusEnum.CANCELED) ||
                    booking.getStatus().equals(Constants.BookingStatusEnum.FAILED)) {
                log.info("Booking {} already in terminal state {}, skipping refund",
                        booking.getId(), booking.getStatus());
                continue;
            }

            // Try to refund the booking in a separate transaction using TransactionTemplate
            // This creates a new transaction that won't affect the parent transaction if it fails
            try {
                // Create a new TransactionTemplate with REQUIRES_NEW propagation
                TransactionTemplate newTransactionTemplate = new TransactionTemplate(transactionTemplate.getTransactionManager());
                newTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                newTransactionTemplate.execute(status -> {
                    bookingService.refundBooking(booking.getId());
                    return null;
                });
                successfulRefunds++;
                log.info("Successfully refunded booking {}", booking.getId());
            } catch (Exception e) {
                failedRefunds++;
                String errorMsg = String.format("Failed to refund booking %s: %s",
                        booking.getId(), e.getMessage());
                log.error(errorMsg, e);
                refundErrors.add(errorMsg);

                // Continue with other bookings even if one fails
                // We still want to delete the package
            }
        }

        log.info("Refund summary for service package {}: {} successful, {} failed out of {} incomplete bookings",
                id, successfulRefunds, failedRefunds, incompleteBookings.size());

        // Log any refund errors
        if (!refundErrors.isEmpty()) {
            log.warn("Refund errors for service package {}: {}", id, String.join("; ", refundErrors));
        }

        // 6. Perform soft delete - repository.delete() will trigger @SQLDelete annotation
        // which sets deleted_at = NOW()
        // IMPORTANT: Bookings and BookingPayments are NOT deleted (no cascade)
        // They are preserved for reporting and transaction history
        // Only the ServicePackage is soft-deleted (deleted_at is set)
        servicePackageRepository.delete(servicePackage);

        log.info("Service package {} soft deleted successfully by user {} (role: {}). " +
                        "Refunded {} bookings, {} failed. " +
                        "All bookings and payments are preserved for historical records.",
                id, currentUser.getId(), currentUser.getRole(), successfulRefunds, failedRefunds);
    }

    // ============ Interaction methods (merged from PackageInteractionService) ============

    @Override
    @Transactional
    public ServicePackageResponse toggleInteraction(UUID packageId, Constants.InteractionTypeEnum interactionType) {
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

        return getPackageWithInteractions(packageId);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicePackageResponse getPackageWithInteractions(UUID packageId) {
        ServicePackage servicePackage = servicePackageRepository.findById(packageId)
                .orElseThrow(() -> new EntityNotFoundException("Service package not found with id: " + packageId));

        // Map service package to response
        ServicePackageResponse response = servicePackageMapper.mapTo(servicePackage, ServicePackageResponse.class);

        // Get all interactions with user info
        List<PackageInteraction> interactions = interactionRepository.findAllByServicePackage_IdWithUser(packageId);

        List<ServicePackageResponse.UserInteractionInfo> userInteractions = interactions.stream()
                .map(interaction -> ServicePackageResponse.UserInteractionInfo.builder()
                        .userId(interaction.getUser().getId())
                        .name(interaction.getUser().getFullName())
                        .avatar(interaction.getUser().getAvatarUrl())
                        .typeInteract(interaction.getInteractionType().getValue())
                        .build())
                .collect(Collectors.toList());

        response.setUserInteractions(userInteractions);

        return response;
    }

    private void updatePackageCounts(UUID packageId) {
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

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackageResponse> getAllPackagesWithInteractions(Pageable pageable,
                                                                       String searchText,
                                                                       Double minPrice, Double maxPrice,
                                                                       List<UUID> packageCategoryIds,
                                                                       List<UUID> seerSpecialityIds,
                                                                       Integer minTime, Integer maxTime,
                                                                       Constants.PackageStatusEnum status) {
        // Convert empty lists to null to avoid JPQL issues
        List<UUID> categoryIdsFilter = (packageCategoryIds != null && packageCategoryIds.isEmpty())
                ? null
                : packageCategoryIds;
        List<UUID> specialityIdsFilter = (seerSpecialityIds != null && seerSpecialityIds.isEmpty())
                ? null
                : seerSpecialityIds;

        Page<ServicePackage> servicePackages = servicePackageRepository.findAllWithFilters(
                status,
                minPrice, maxPrice,
                categoryIdsFilter,
                specialityIdsFilter,
                minTime, maxTime,
                searchText,
                pageable);

        return enrichWithInteractions(servicePackages);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackageResponse> getPackagesByCategoryWithInteractions(
            Constants.ServiceCategoryEnum category, Pageable pageable, Double minPrice, Double maxPrice, Constants.PackageStatusEnum status) {
        // If no status provided, default to AVAILABLE for public endpoints to keep existing behavior
        Constants.PackageStatusEnum statusToUse = status != null ? status : Constants.PackageStatusEnum.AVAILABLE;

        Specification<ServicePackage> spec = (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            // Status filter
            predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.equal(root.get("status"), statusToUse));

            // Category filter through packageCategories relationship
            if (category != null) {
                var categoryJoin = root.join("packageCategories");
                var knowledgeCategoryJoin = categoryJoin.join("knowledgeCategory");
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.equal(knowledgeCategoryJoin.get("name"), category.getValue()));
            }

            // Price filters
            if (minPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates = criteriaBuilder.and(predicates,
                        criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Make distinct when joining categories
            if (category != null) {
                query.distinct(true);
            }

            return predicates;
        };

        Page<ServicePackage> servicePackages = servicePackageRepository.findAll(spec, pageable);
        return enrichWithInteractions(servicePackages);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackageResponse> getMyPackages(Pageable pageable) {
        User currentSeer = userService.getUser();
        Page<ServicePackage> mySeerPackages = servicePackageRepository.findAllBySeer_Id(currentSeer.getId(), pageable);
        return enrichWithInteractions(mySeerPackages);
    }

    /**
     * Helper method to enrich service packages with user interaction data
     */
    private Page<ServicePackageResponse> enrichWithInteractions(Page<ServicePackage> servicePackages) {
        return servicePackages.map(pkg -> {
            // Map basic package info
            ServicePackageResponse response = servicePackageMapper.mapTo(pkg, ServicePackageResponse.class);

            // Get all interactions for this package
            List<PackageInteraction> interactions = interactionRepository.findAllByServicePackage_IdWithUser(pkg.getId());

            // Map interactions to UserInteractionInfo
            List<ServicePackageResponse.UserInteractionInfo> userInteractions = interactions.stream()
                    .map(interaction -> ServicePackageResponse.UserInteractionInfo.builder()
                            .userId(interaction.getUser().getId())
                            .name(interaction.getUser().getFullName())
                            .avatar(interaction.getUser().getAvatarUrl())
                            .typeInteract(interaction.getInteractionType().getValue())
                            .build())
                    .collect(Collectors.toList());

            response.setUserInteractions(userInteractions);

            // Get review statistics from bookings
            Long totalReviews = bookingRepository.countReviewsByServicePackageId(pkg.getId());
            Double avgRating = bookingRepository.getAverageRatingByServicePackageId(pkg.getId());

            response.setTotalReviews(totalReviews != null ? totalReviews : 0L);
            response.setAvgRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null); // Round to 1 decimal place

            return response;
        });
    }

    // ============ Admin methods ============

    @Override
    @Transactional
    public ServicePackage confirmServicePackage(String packageId, Constants.PackageStatusEnum status, String rejectionReason) {
        log.info("Admin confirming service package {} with status: {}", packageId, status);

        ServicePackage servicePackage = servicePackageRepository.findById(UUID.fromString(packageId))
                .orElseThrow(() -> new NotFoundException("Service package not found with id: " + packageId));

        // Validate status transition
        if (status == Constants.PackageStatusEnum.REJECTED &&
                (rejectionReason == null || rejectionReason.trim().isEmpty())) {
            throw new IllegalArgumentException("Rejection reason is required when rejecting a service package");
        }

        // Update status and rejection reason
        servicePackage.setStatus(status);

        if (status == Constants.PackageStatusEnum.REJECTED) {
            servicePackage.setRejectionReason(rejectionReason);
            log.info("Service package {} rejected with reason: {}", packageId, rejectionReason);
        } else {
            // Clear rejection reason if status is not REJECTED
            servicePackage.setRejectionReason(null);
            log.info("Service package {} status changed to: {}", packageId, status);
        }

        return servicePackageRepository.save(servicePackage);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackageResponse> getAllHiddenPackages(Pageable pageable) {
        log.info("Fetching all hidden service packages");

        Specification<ServicePackage> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), Constants.PackageStatusEnum.HIDDEN);

        Page<ServicePackage> hiddenPackages = servicePackageRepository.findAll(spec, pageable);

        // Enrich with interactions and review statistics
        return enrichWithInteractions(hiddenPackages);
    }

    @Transactional(readOnly = true)
    public ServicePackageStats getAllPackagesStats() {
        long total = servicePackageRepository.count();
        long reported = servicePackageRepository.countPackagesWithReports(Constants.TargetReportTypeEnum.SERVICE_PACKAGE);
        long hidden = servicePackageRepository.countByStatus(Constants.PackageStatusEnum.HIDDEN);
        long totalInteractions = interactionRepository.count();
        
        return ServicePackageStats.builder()
                .totalPackages(total)
                .reportedPackages(reported)
                .hiddenPackages(hidden)
                .totalInteractions(totalInteractions)
                .build();
    }

    @lombok.Getter
    @lombok.Builder
    @lombok.AllArgsConstructor
    public static class ServicePackageStats {
        private Long totalPackages;
        private Long reportedPackages;
        private Long hiddenPackages;
        private Long totalInteractions;
    }
}
