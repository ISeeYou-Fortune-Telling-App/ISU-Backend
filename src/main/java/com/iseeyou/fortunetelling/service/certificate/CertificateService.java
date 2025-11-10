package com.iseeyou.fortunetelling.service.certificate;

import com.iseeyou.fortunetelling.dto.request.certificate.CertificateCreateRequest;
import com.iseeyou.fortunetelling.dto.request.certificate.CertificateUpdateRequest;
import com.iseeyou.fortunetelling.dto.request.certificate.CertificateApprovalRequest;
import com.iseeyou.fortunetelling.entity.certificate.Certificate;
import com.iseeyou.fortunetelling.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.UUID;

public interface CertificateService {
    Page<Certificate> findAll(Pageable pageable);
    Certificate findById(UUID id);
    Certificate create(CertificateCreateRequest request) throws IOException;
    Certificate createForUser(CertificateCreateRequest request, User user) throws IOException;
    Certificate update(UUID id, CertificateUpdateRequest request) throws IOException;
    void delete(UUID id) throws IOException;
    Page<Certificate> findByUserId(UUID userId, Pageable pageable);
    Page<Certificate> findByUserIdAndCategoryId(UUID userId, UUID categoryId, Pageable pageable);
    Page<Certificate> findByCategoryId(UUID categoryId, Pageable pageable);
    Certificate approveCertificate(UUID certificateId, CertificateApprovalRequest request);
}
