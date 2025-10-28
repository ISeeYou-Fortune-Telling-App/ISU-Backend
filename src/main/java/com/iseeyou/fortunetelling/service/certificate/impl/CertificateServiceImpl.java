package com.iseeyou.fortunetelling.service.certificate.impl;

import com.iseeyou.fortunetelling.entity.certificate.Certificate;
import com.iseeyou.fortunetelling.entity.certificate.CertificateCategory;
import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeCategory;
import com.iseeyou.fortunetelling.exception.NotFoundException;
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

    public CertificateServiceImpl(
            CertificateRepository certificateRepository,
            CertificateCategoryRepository certificateCategoryRepository,
            KnowledgeCategoryService knowledgeCategoryService,
            CloudinaryService cloudinaryService,
            @Lazy UserService userService) {
        this.certificateRepository = certificateRepository;
        this.certificateCategoryRepository = certificateCategoryRepository;
        this.knowledgeCategoryService = knowledgeCategoryService;
        this.cloudinaryService = cloudinaryService;
        this.userService = userService;
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
    public Certificate create(Certificate certificate, Set<UUID> categoryIds) {
        certificate.setSeer(userService.getUser());
        certificate.setStatus(Constants.CertificateStatusEnum.PENDING);
        Certificate newCertificate = certificateRepository.save(certificate);
        List<KnowledgeCategory> categories = knowledgeCategoryService.findAllByIds(categoryIds);

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

        return newCertificate;
    }

    @Override
    @Transactional
    public Certificate update(UUID id, Certificate certificate, Set<UUID> newCategoryIds) throws IOException {
        Certificate existingCertificate = certificateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Certificate not found with id: " + id));

        if (existingCertificate.getCertificateUrl() != null) {
            // Nếu có URL cũ, xóa nó khỏi Cloudinary
            cloudinaryService.deleteFile(existingCertificate.getCertificateUrl());
            existingCertificate.setCertificateUrl(null);
        }

        updateCertificateFields(existingCertificate, certificate);

        if (newCategoryIds != null) {
            updateCertificateCategories(existingCertificate, newCategoryIds);
        }

        return existingCertificate;
    }

    private void updateCertificateFields(Certificate existing, Certificate updated) {
        existing.setCertificateName(updated.getCertificateName());
        existing.setCertificateDescription(updated.getCertificateDescription());
        existing.setIssuedBy(updated.getIssuedBy());
        existing.setIssuedAt(updated.getIssuedAt());
        existing.setExpirationDate(updated.getExpirationDate());
        existing.setCertificateUrl(updated.getCertificateUrl());
        existing.setStatus(updated.getStatus());
        existing.setDecisionDate(updated.getDecisionDate());
        existing.setDecisionReason(updated.getDecisionReason());
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
}
