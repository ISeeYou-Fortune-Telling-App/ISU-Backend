package com.iseeyou.fortunetelling.repository.report;

import com.iseeyou.fortunetelling.entity.report.ReportEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportEvidenceRepository extends JpaRepository<ReportEvidence, UUID>, JpaSpecificationExecutor<ReportEvidence> {
}
