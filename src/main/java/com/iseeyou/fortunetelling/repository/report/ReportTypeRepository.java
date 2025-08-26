package com.iseeyou.fortunetelling.repository.report;

import com.iseeyou.fortunetelling.entity.report.ReportType;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportTypeRepository extends JpaRepository<ReportType, UUID>, JpaSpecificationExecutor<ReportType> {
    Optional<ReportType> findByName(Constants.ReportTypeEnum name);
}
