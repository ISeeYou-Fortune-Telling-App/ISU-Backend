package com.iseeyou.fortunetelling.repository.report;

import com.iseeyou.fortunetelling.entity.report.Report;
import com.iseeyou.fortunetelling.util.Constants;
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
public interface ReportRepository extends JpaRepository<Report, UUID>, JpaSpecificationExecutor<Report> {

    @Override
    @EntityGraph(attributePaths = {"reporter", "reportedUser", "reportType", "reportEvidences"})
    Page<Report> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"reporter", "reportedUser", "reportType", "reportEvidences"})
    Optional<Report> findById(UUID id);

    @EntityGraph(attributePaths = {"reporter", "reportedUser", "reportType", "reportEvidences"})
    Page<Report> findAllByReporter_Id(UUID reporterId, Pageable pageable);

    @EntityGraph(attributePaths = {"reporter", "reportedUser", "reportType", "reportEvidences"})
    Page<Report> findAllByReportedUser_Id(UUID reportedUserId, Pageable pageable);

    @EntityGraph(attributePaths = {"reporter", "reportedUser", "reportType", "reportEvidences"})
    Page<Report> findAllByTargetId(UUID targetId, Pageable pageable);

    @EntityGraph(attributePaths = {"reporter", "reportedUser", "reportType", "reportEvidences"})
    Page<Report> findAllByTargetType(Constants.TargetReportTypeEnum targetType, Pageable pageable);

    @EntityGraph(attributePaths = {"reporter", "reportedUser", "reportType", "reportEvidences"})
    Page<Report> findAllByStatus(Constants.ReportStatusEnum status, Pageable pageable);

    // Statistics methods
    long countByStatus(Constants.ReportStatusEnum status);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.createdAt >= :startOfMonth")
    long countReportsCreatedSince(@Param("startOfMonth") java.time.LocalDateTime startOfMonth);
}
