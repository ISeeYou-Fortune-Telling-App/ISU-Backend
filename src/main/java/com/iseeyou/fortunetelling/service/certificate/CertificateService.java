package com.iseeyou.fortunetelling.service.certificate;

import com.iseeyou.fortunetelling.entity.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public interface CertificateService {
    Page<Certificate> findAll(Pageable pageable);
    Certificate findById(UUID id);
    Certificate create(Certificate certificate, Set<UUID> categoryIds);
    Certificate update(UUID id, Certificate certificate, Set<UUID> newCategoryIds) throws IOException;
    void delete(UUID id) throws IOException;
    Page<Certificate> findByUserId(UUID userId, Pageable pageable);
    Page<Certificate> findByUserIdAndCategoryId(UUID userId, UUID categoryId, Pageable pageable);
    Page<Certificate> findByCategoryId(UUID categoryId, Pageable pageable);
}
