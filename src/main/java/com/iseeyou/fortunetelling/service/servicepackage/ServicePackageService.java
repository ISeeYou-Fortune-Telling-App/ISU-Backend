package com.iseeyou.fortunetelling.service.servicepackage;

import com.iseeyou.fortunetelling.entity.ServicePackage;
import com.iseeyou.fortunetelling.entity.ServiceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public interface ServicePackageService {
    Page<ServicePackage> findAll(Pageable pageable);
    ServicePackage findById(UUID id);
    ServicePackage create(ServicePackage servicePackage, Set<UUID> categoryIds);
    ServicePackage update(UUID id, ServicePackage servicePackage, Set<UUID> newCategoryIds) throws IOException;
    void delete(UUID id) throws IOException;
    Page<ServicePackage> findByUserId(UUID userId, Pageable pageable);
    Page<ServicePackage> findByUserIdAndCategoryId(UUID userId, UUID categoryId, Pageable pageable);
    Page<ServicePackage> findByCategoryId(UUID categoryId, Pageable pageable);
    void interactPackage(UUID packageId, boolean isLike);
    void leaveComment(UUID packageId, UUID parentCommentId, String content);
    void deleteComment(UUID commentId) throws IOException;
    Page<ServiceReview> getCommentsByPackageId(UUID packageId, Pageable pageable);
    Page<ServiceReview> getRepliesByCommentId(UUID commentId, Pageable pageable);
}
