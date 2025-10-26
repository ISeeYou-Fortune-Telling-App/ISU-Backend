package com.iseeyou.fortunetelling.service.servicepackage.impl;

import com.iseeyou.fortunetelling.dto.request.servicepackage.ServicePackageUpsertRequest;
import com.iseeyou.fortunetelling.dto.response.ServicePackageDetailResponse;
import com.iseeyou.fortunetelling.dto.response.servicepackage.ServicePackageResponse;
import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.entity.servicepackage.PackageCategory;
import com.iseeyou.fortunetelling.entity.servicepackage.PackageInteraction;
import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeCategory;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.entity.user.SeerProfile;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.mapper.ServicePackageMapper;
import com.iseeyou.fortunetelling.repository.booking.BookingRepository;
import com.iseeyou.fortunetelling.repository.servicepackage.ServicePackageRepository;
import com.iseeyou.fortunetelling.repository.servicepackage.PackageInteractionRepository;
import com.iseeyou.fortunetelling.repository.knowledge.KnowledgeCategoryRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

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
    private final CloudinaryConfig cloudinaryConfig;
    private final UserRepository userRepository;
    private final PackageInteractionRepository interactionRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ServicePackageMapper servicePackageMapper;
    private final BookingService bookingService;

    // Constructor with @Lazy for BookingService to prevent circular dependency
    public ServicePackageServiceImpl(
            ServicePackageRepository servicePackageRepository,
            KnowledgeCategoryRepository knowledgeCategoryRepository,
            CloudinaryConfig cloudinaryConfig,
            UserRepository userRepository,
            PackageInteractionRepository interactionRepository,
            BookingRepository bookingRepository,
            UserService userService,
            ServicePackageMapper servicePackageMapper,
            @Lazy BookingService bookingService) {
        this.servicePackageRepository = servicePackageRepository;
        this.knowledgeCategoryRepository = knowledgeCategoryRepository;
        this.cloudinaryConfig = cloudinaryConfig;
        this.userRepository = userRepository;
        this.interactionRepository = interactionRepository;
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.servicePackageMapper = servicePackageMapper;
        this.bookingService = bookingService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackage> findAllAvailable(Pageable pageable) {
        Specification<ServicePackage> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), Constants.PackageStatusEnum.AVAILABLE);
        return servicePackageRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackage> findAvailableWithFilters(Double minPrice, Double maxPrice, Pageable pageable) {
        Specification<ServicePackage> spec = (root, query, criteriaBuilder) -> {
            var predicates = criteriaBuilder.conjunction();

            // Status filter
            predicates = criteriaBuilder.and(predicates,
                    criteriaBuilder.equal(root.get("status"), Constants.PackageStatusEnum.AVAILABLE));

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
    public ServicePackage createOrUpdatePackage(String seerId, ServicePackageUpsertRequest request) {
        ServicePackage servicePackage;
        User seer = userRepository.findById(UUID.fromString(seerId))
                .orElseThrow(() -> new NotFoundException("Seer not found with id: " + seerId));

        if (StringUtils.hasText(request.getPackageId())) {
            Optional<ServicePackage> optional = servicePackageRepository.findById(UUID.fromString(request.getPackageId()));
            servicePackage = optional.orElseThrow(() -> new NotFoundException("Service package not found"));
        } else {
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

        // Handle category assignment
        if (request.getCategory() != null) {
            // Clear existing categories if updating
            servicePackage.getPackageCategories().clear();

            // Find or create the knowledge category
            KnowledgeCategory knowledgeCategory = knowledgeCategoryRepository
                    .findByName(request.getCategory().getValue())
                    .orElseGet(() -> {
                        KnowledgeCategory newCategory = KnowledgeCategory.builder()
                                .name(request.getCategory().getValue())
                                .description("Auto-generated category for " + request.getCategory().getValue())
                                .build();
                        return knowledgeCategoryRepository.save(newCategory);
                    });

            // Create package category relationship
            PackageCategory packageCategory = PackageCategory.builder()
                    .servicePackage(servicePackage)
                    .knowledgeCategory(knowledgeCategory)
                    .build();

            servicePackage.getPackageCategories().add(packageCategory);
        }

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imageUrl = cloudinaryConfig.uploadImage(request.getImage());
            servicePackage.setImageUrl(imageUrl);
        }
        return servicePackageRepository.save(servicePackage);
    }

    @Override
    public String uploadImage(MultipartFile image) {
        return cloudinaryConfig.uploadImage(image);
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
        
        List<ServicePackageDetailResponse.ReviewInfo> reviews = reviewsPage.getContent().stream()
                .map(booking -> ServicePackageDetailResponse.ReviewInfo.builder()
                        .bookingId(booking.getId())
                        .rating(booking.getRating())
                        .comment(booking.getComment())
                        .reviewedAt(booking.getReviewedAt())
                        .customer(ServicePackageDetailResponse.CustomerInfo.builder()
                                .customerId(booking.getCustomer().getId())
                                .customerName(booking.getCustomer().getFullName())
                                .customerAvatar(booking.getCustomer().getAvatarUrl())
                                .build())
                        .build())
                .collect(Collectors.toList());

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
                .reviews(reviews)
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
        int successfulRefunds = 0;
        int failedRefunds = 0;
        List<String> refundErrors = new java.util.ArrayList<>();
        
        for (Booking booking : incompleteBookings) {
            try {
                log.info("Processing refund for booking {} (status: {})", booking.getId(), booking.getStatus());
                
                // Check if booking is already canceled or failed
                if (booking.getStatus().equals(Constants.BookingStatusEnum.CANCELED) ||
                    booking.getStatus().equals(Constants.BookingStatusEnum.FAILED)) {
                    log.info("Booking {} already in terminal state {}, skipping refund", 
                            booking.getId(), booking.getStatus());
                    continue;
                }
                
                // Try to refund the booking
                bookingService.refundBooking(booking.getId());
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
        servicePackageRepository.delete(servicePackage);
        
        log.info("Service package {} soft deleted successfully by user {} (role: {}). " +
                "Refunded {} bookings, {} failed", 
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

    @Transactional
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
    public Page<ServicePackageResponse> getAllPackagesWithInteractions(Pageable pageable, Double minPrice, Double maxPrice) {
        Page<ServicePackage> servicePackages;
        
        if (minPrice != null || maxPrice != null) {
            servicePackages = findAvailableWithFilters(minPrice, maxPrice, pageable);
        } else {
            servicePackages = findAllAvailable(pageable);
        }
        
        return enrichWithInteractions(servicePackages);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackageResponse> getPackagesByCategoryWithInteractions(
            Constants.ServiceCategoryEnum category, Pageable pageable, Double minPrice, Double maxPrice) {
        Page<ServicePackage> servicePackages = findAvailableByCategoryWithFilters(category, minPrice, maxPrice, pageable);
        return enrichWithInteractions(servicePackages);
    }

    /**
     * Helper method to enrich service packages with user interaction data and review statistics
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
}
