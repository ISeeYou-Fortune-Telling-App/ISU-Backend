package com.iseeyou.fortunetelling.service.servicepackage.impl;

import com.iseeyou.fortunetelling.dto.request.servicepackage.ServicePackageUpsertRequest;
import com.iseeyou.fortunetelling.dto.response.ServicePackageDetailResponse;
import com.iseeyou.fortunetelling.entity.ServicePackage;
import com.iseeyou.fortunetelling.entity.ServicePackage.ServicePackageStatus;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.entity.user.SeerProfile;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.ServicePackageRepository;
import com.iseeyou.fortunetelling.repository.UserRepository;
import com.iseeyou.fortunetelling.service.servicepackage.ServicePackageService;
import com.iseeyou.fortunetelling.config.CloudinaryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicePackageServiceImpl implements ServicePackageService {

    private final ServicePackageRepository servicePackageRepository;
    private final CloudinaryConfig cloudinaryConfig;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackage> findAllAvailable(Pageable pageable) {
        return servicePackageRepository.findByStatus(ServicePackage.ServicePackageStatus.AVAILABLE, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServicePackage> findAvailableWithFilters(Double minPrice, Double maxPrice, Pageable pageable) {
        return servicePackageRepository.findAvailablePackagesWithPriceFilter(minPrice, maxPrice, pageable);
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
        if (StringUtils.hasText(request.getPackageId())) {
            Optional<ServicePackage> optional = servicePackageRepository.findById(UUID.fromString(request.getPackageId()));
            servicePackage = optional.orElseThrow(() -> new NotFoundException("Service package not found"));
        } else {
            servicePackage = new ServicePackage();
            servicePackage.setStatus(ServicePackageStatus.HIDDEN); // trạng thái chờ duyệt
            servicePackage.setRejectionReason(null);
        }
        servicePackage.setSeerId(seerId);
        servicePackage.setPackageTitle(request.getPackageTitle());
        servicePackage.setPackageContent(request.getPackageContent());
        servicePackage.setDurationMinutes(request.getDurationMinutes());
        servicePackage.setPrice(request.getPrice());
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

        // Lấy thông tin User và SeerProfile riêng biệt
        User seer = userRepository.findByIdWithSeerProfile(UUID.fromString(servicePackage.getSeerId()))
                .orElseThrow(() -> new NotFoundException("Seer not found with id: " + servicePackage.getSeerId()));

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

        // Tạo ServicePackageDetailResponse
        return ServicePackageDetailResponse.builder()
                .packageId(servicePackage.getId().toString())
                .packageTitle(servicePackage.getPackageTitle())
                .packageContent(servicePackage.getPackageContent())
                .imageUrl(servicePackage.getImageUrl())
                .durationMinutes(servicePackage.getDurationMinutes())
                .price(servicePackage.getPrice())
                .status(servicePackage.getStatus().toString())
                .rejectionReason(servicePackage.getRejectionReason())
                .createdAt(servicePackage.getCreatedAt())
                .updatedAt(servicePackage.getUpdatedAt())
                .seer(seerInfo)
                .build();
    }
}
