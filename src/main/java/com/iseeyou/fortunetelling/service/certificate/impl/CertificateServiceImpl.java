package com.iseeyou.fortunetelling.service.certificate.impl;

import com.iseeyou.fortunetelling.dto.request.certificate.CertificateApprovalRequest;
import com.iseeyou.fortunetelling.dto.request.certificate.CertificateCreateRequest;
import com.iseeyou.fortunetelling.dto.request.certificate.CertificateUpdateRequest;
import com.iseeyou.fortunetelling.entity.certificate.Certificate;
import com.iseeyou.fortunetelling.entity.certificate.CertificateCategory;
import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeCategory;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.mapper.CertificateMapper;
import com.iseeyou.fortunetelling.repository.certificate.CertificateCategoryRepository;
import com.iseeyou.fortunetelling.repository.certificate.CertificateRepository;
import com.iseeyou.fortunetelling.service.certificate.CertificateService;
import com.iseeyou.fortunetelling.service.fileupload.CloudinaryService;
import com.iseeyou.fortunetelling.service.knowledgecategory.KnowledgeCategoryService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateCategoryRepository certificateCategoryRepository;
    private final KnowledgeCategoryService knowledgeCategoryService;
    private final CloudinaryService cloudinaryService;
    private final UserService userService;
    private final CertificateMapper certificateMapper;

    public CertificateServiceImpl(
            CertificateRepository certificateRepository,
            CertificateCategoryRepository certificateCategoryRepository,
            KnowledgeCategoryService knowledgeCategoryService,
            CloudinaryService cloudinaryService,
            @Lazy UserService userService,
            CertificateMapper certificateMapper) {
        this.certificateRepository = certificateRepository;
        this.certificateCategoryRepository = certificateCategoryRepository;
        this.knowledgeCategoryService = knowledgeCategoryService;
        this.cloudinaryService = cloudinaryService;
        this.userService = userService;
        this.certificateMapper = certificateMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Certificate> findAll(Pageable pageable) {
        return certificateRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Certificate findById(UUID id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Certificate not found with id: " + id));
    }

    @Override
    @Transactional
    public Certificate create(CertificateCreateRequest request) throws IOException {
        // Map DTO to Entity
        Certificate certificate = certificateMapper.mapTo(request, Certificate.class);
        
        // Defensive validation in service layer to avoid Hibernate PropertyValueException
        if (certificate.getIssuedBy() == null || certificate.getIssuedBy().trim().isEmpty()) {
            throw new IllegalArgumentException("issuedBy must not be null or empty");
        }
        if (certificate.getIssuedAt() == null) {
            throw new IllegalArgumentException("issuedAt must not be null");
        }

        // Upload certificate file to Cloudinary
        if (request.getCertificateFile() != null && !request.getCertificateFile().isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(request.getCertificateFile(), "certificates");
            certificate.setCertificateUrl(imageUrl);
        }
        
        // Set business logic fields
        certificate.setSeer(userService.getUser());
        certificate.setStatus(Constants.CertificateStatusEnum.PENDING);
        Certificate newCertificate = certificateRepository.save(certificate);
        
        // Handle categories if provided
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<KnowledgeCategory> categories = knowledgeCategoryService.findAllByIds(request.getCategoryIds());

            Set<CertificateCategory> certificateCategories = new HashSet<>();
            for (KnowledgeCategory category : categories) {
                CertificateCategory certificateCategory = CertificateCategory.builder()
                        .certificate(newCertificate)
                        .knowledgeCategory(category)
                        .build();

                certificateCategories.add(certificateCategory);
            }

            certificateCategoryRepository.saveAll(certificateCategories);
            newCertificate.setCertificateCategories(certificateCategories);
        }

        return newCertificate;
    }

    @Override
    @Transactional
    public Certificate createForUser(CertificateCreateRequest request, User user) throws IOException {
        // Map DTO to Entity
        Certificate certificate = certificateMapper.mapTo(request, Certificate.class);

        // Defensive validation in service layer to avoid Hibernate PropertyValueException
        if (certificate.getIssuedBy() == null || certificate.getIssuedBy().trim().isEmpty()) {
            throw new IllegalArgumentException("issuedBy must not be null or empty");
        }
        if (certificate.getIssuedAt() == null) {
            throw new IllegalArgumentException("issuedAt must not be null");
        }

        // Upload certificate file to Cloudinary
        if (request.getCertificateFile() != null && !request.getCertificateFile().isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(request.getCertificateFile(), "certificates");
            certificate.setCertificateUrl(imageUrl);
        }

        // Set business logic fields - sử dụng User được truyền vào thay vì lấy từ security context
        certificate.setSeer(user);
        certificate.setStatus(Constants.CertificateStatusEnum.PENDING);
        Certificate newCertificate = certificateRepository.save(certificate);

        // Handle categories if provided
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<KnowledgeCategory> categories = knowledgeCategoryService.findAllByIds(request.getCategoryIds());

            Set<CertificateCategory> certificateCategories = new HashSet<>();
            for (KnowledgeCategory category : categories) {
                CertificateCategory certificateCategory = CertificateCategory.builder()
                        .certificate(newCertificate)
                        .knowledgeCategory(category)
                        .build();

                certificateCategories.add(certificateCategory);
            }

            certificateCategoryRepository.saveAll(certificateCategories);
            newCertificate.setCertificateCategories(certificateCategories);
        }

        return newCertificate;
    }

    @Override
    @Transactional
    public Certificate update(UUID id, CertificateUpdateRequest request) throws IOException {
        Certificate existingCertificate = certificateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Certificate not found with id: " + id));

        // Handle file upload if new file provided
        if (request.getCertificateFile() != null && !request.getCertificateFile().isEmpty()) {
            // Delete old file from Cloudinary if exists
            if (existingCertificate.getCertificateUrl() != null) {
                cloudinaryService.deleteFile(existingCertificate.getCertificateUrl());
            }
            // Upload new file
            String imageUrl = cloudinaryService.uploadFile(request.getCertificateFile(), "certificates");
            existingCertificate.setCertificateUrl(imageUrl);
        }

        // Update fields from request
        if (request.getCertificateName() != null) {
            existingCertificate.setCertificateName(request.getCertificateName());
        }
        if (request.getCertificateDescription() != null) {
            existingCertificate.setCertificateDescription(request.getCertificateDescription());
        }
        if (request.getIssuedBy() != null) {
            existingCertificate.setIssuedBy(request.getIssuedBy());
        }
        if (request.getIssuedAt() != null) {
            existingCertificate.setIssuedAt(request.getIssuedAt());
        }
        if (request.getExpirationDate() != null) {
            existingCertificate.setExpirationDate(request.getExpirationDate());
        }
        if (request.getStatus() != null) {
            existingCertificate.setStatus(request.getStatus());
        }
        if (request.getDecisionDate() != null) {
            existingCertificate.setDecisionDate(request.getDecisionDate());
        }
        if (request.getDecisionReason() != null) {
            existingCertificate.setDecisionReason(request.getDecisionReason());
        }

        // Update categories if provided
        if (request.getCategoryIds() != null) {
            updateCertificateCategories(existingCertificate, request.getCategoryIds());
        }

        return certificateRepository.save(existingCertificate);
    }

    private void updateCertificateCategories(Certificate certificate, Set<UUID> newCategoryIds) {
        // Lấy danh sách categories hiện tại
        Set<UUID> currentCategoryIds = certificate.getCertificateCategories().stream()
                .map(cc -> cc.getKnowledgeCategory().getId())
                .collect(Collectors.toSet());

        // Tìm categories cần xóa và cần thêm
        Set<UUID> categoriesToRemove = currentCategoryIds.stream()
                .filter(id -> !newCategoryIds.contains(id))
                .collect(Collectors.toSet());

        Set<UUID> categoriesToAdd = newCategoryIds.stream()
                .filter(id -> !currentCategoryIds.contains(id))
                .collect(Collectors.toSet());

        // Xóa relationships không còn cần thiết
        if (!categoriesToRemove.isEmpty()) {
            certificateCategoryRepository.deleteAllByCertificate_IdAndKnowledgeCategory_IdIn(
                    certificate.getId(), categoriesToRemove);
            // Cập nhật collection trong entity
            certificate.getCertificateCategories().removeIf(
                    cc -> categoriesToRemove.contains(cc.getKnowledgeCategory().getId()));
        }

        // Thêm relationships mới
        if (!categoriesToAdd.isEmpty()) {
            // Lấy tất cả categories trong một query
            List<KnowledgeCategory> categoriesToAddEntities = knowledgeCategoryService
                    .findAllByIds(categoriesToAdd);

            // Tạo các relationships mới
            Set<CertificateCategory> newRelationships = new HashSet<>();
            for (KnowledgeCategory category : categoriesToAddEntities) {
                CertificateCategory relationship = CertificateCategory.builder()
                        .certificate(certificate)
                        .knowledgeCategory(category)
                        .build();

                newRelationships.add(relationship);
            }

            // Lưu tất cả relationships mới
            certificateCategoryRepository.saveAll(newRelationships);

            // Cập nhật collection trong entity
            certificate.getCertificateCategories().addAll(newRelationships);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) throws IOException {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Certificate not found with id: " + id));
        if (certificate.getCertificateUrl() != null) {
            cloudinaryService.deleteFile(certificate.getCertificateUrl());
        }
        certificateRepository.delete(certificate);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Certificate> findByUserId(UUID userId, Pageable pageable) {
        return certificateRepository.findAllBySeer_Id(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Certificate> findByUserIdAndCategoryId(UUID seerId, UUID categoryId, Pageable pageable) {
        return certificateRepository.findBySeerIdAndCategoryId(seerId, categoryId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Certificate> findByCategoryId(UUID categoryId, Pageable pageable) {
        return certificateRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    @Transactional
    public Certificate approveCertificate(UUID certificateId, CertificateApprovalRequest request) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new NotFoundException("Certificate not found with id: " + certificateId));

        // Kiểm tra action phải là APPROVED hoặc REJECTED
        if (request.getAction() != Constants.CertificateStatusEnum.APPROVED
                && request.getAction() != Constants.CertificateStatusEnum.REJECTED) {
            throw new IllegalArgumentException("Action must be APPROVED or REJECTED");
        }

        // Nếu action là REJECTED, phải có decision reason
        if (request.getAction() == Constants.CertificateStatusEnum.REJECTED) {
            if (request.getDecisionReason() == null || request.getDecisionReason().trim().isEmpty()) {
                throw new IllegalArgumentException("Decision reason is required when rejecting a certificate");
            }
        }

        // Nếu status thay đổi, cập nhật status và decision date
        boolean statusChanged = certificate.getStatus() != request.getAction();
        if (statusChanged) {
            certificate.setStatus(request.getAction());
            certificate.setDecisionDate(LocalDateTime.now());
        }

        // Nếu có decision reason (bất kể action), cập nhật reason và cập nhật decisionDate
        if (request.getDecisionReason() != null) {
            certificate.setDecisionReason(request.getDecisionReason());
            // Nếu decisionDate chưa được cập nhật ở trên (status không đổi), cập nhật decisionDate để phản ánh thay đổi lý do
            if (!statusChanged) {
                certificate.setDecisionDate(LocalDateTime.now());
            }
        }

        Certificate saved = certificateRepository.save(certificate);

        // Reload the saved certificate with categories and seer eagerly fetched to avoid LazyInitializationException
        return certificateRepository.findByIdWithCategories(saved.getId())
                .orElse(saved);
    }
}
