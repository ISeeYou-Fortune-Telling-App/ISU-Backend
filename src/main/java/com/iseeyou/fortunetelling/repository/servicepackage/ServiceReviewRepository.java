package com.iseeyou.fortunetelling.repository.servicepackage;

import com.iseeyou.fortunetelling.entity.servicepackage.ServiceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceReviewRepository extends JpaRepository<ServiceReview, UUID>, JpaSpecificationExecutor<ServiceReview> {

    @EntityGraph(attributePaths = {"user", "servicePackage"})
    @Query("SELECT sr FROM ServiceReview sr WHERE sr.servicePackage.id = :packageId AND sr.parentReview IS NULL ORDER BY sr.createdAt DESC")
    Page<ServiceReview> findTopLevelCommentsByPackageId(@Param("packageId") UUID packageId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT sr FROM ServiceReview sr WHERE sr.parentReview.id = :commentId ORDER BY sr.createdAt ASC")
    Page<ServiceReview> findRepliesByParentCommentId(@Param("commentId") UUID commentId, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "servicePackage", "parentReview"})
    Optional<ServiceReview> findById(UUID id);
}
