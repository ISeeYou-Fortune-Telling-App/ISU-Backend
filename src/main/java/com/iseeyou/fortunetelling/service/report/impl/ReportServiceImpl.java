package com.iseeyou.fortunetelling.service.report.impl;

import com.iseeyou.fortunetelling.entity.chat.Conversation;
import com.iseeyou.fortunetelling.dto.request.report.ReportCreateRequest;
import com.iseeyou.fortunetelling.dto.request.report.ReportUpdateRequest;
import com.iseeyou.fortunetelling.dto.response.report.ReportStatsResponse;
import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.report.Report;
import com.iseeyou.fortunetelling.entity.report.ReportEvidence;
import com.iseeyou.fortunetelling.entity.report.ReportType;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.mapper.ReportMapper;
import com.iseeyou.fortunetelling.repository.booking.BookingRepository;
import com.iseeyou.fortunetelling.repository.chat.ConversationRepository;
import com.iseeyou.fortunetelling.repository.report.ReportEvidenceRepository;
import com.iseeyou.fortunetelling.repository.report.ReportRepository;
import com.iseeyou.fortunetelling.repository.report.ReportTypeRepository;
import com.iseeyou.fortunetelling.repository.servicepackage.ServicePackageRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
import com.iseeyou.fortunetelling.service.fileupload.CloudinaryService;
import com.iseeyou.fortunetelling.service.report.ReportService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportEvidenceRepository reportEvidenceRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final UserRepository userRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final BookingRepository bookingRepository;
    private final ConversationRepository conversationRepository;
    private final ReportMapper reportMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<Report> findAllReports(Pageable pageable, Constants.ReportStatusEnum status, String reportTypeName, Constants.TargetReportTypeEnum targetType) {
        // If no filters provided, use repository's optimized findAll (with @EntityGraph)
        if (status == null && (reportTypeName == null || reportTypeName.isBlank()) && targetType == null) {
            return reportRepository.findAll(pageable);
        }

        // Convert reportTypeName to enum if present
        Constants.ReportTypeEnum reportTypeEnum = null;
        if (reportTypeName != null && !reportTypeName.isBlank()) {
            reportTypeEnum = Constants.ReportTypeEnum.get(reportTypeName);
        }

        // Create final variables for lambda
        final Constants.ReportTypeEnum finalReportTypeEnum = reportTypeEnum;
        final Constants.TargetReportTypeEnum finalTargetType = targetType;

        Specification<Report> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            // Apply status filter
            if (status != null) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }

            // Apply report type filter
            if (finalReportTypeEnum != null) {
                var typeJoin = root.join("reportType", jakarta.persistence.criteria.JoinType.LEFT);
                predicates = cb.and(predicates, cb.equal(typeJoin.get("name"), finalReportTypeEnum));
            }

            // Apply target type filter
            if (finalTargetType != null) {
                predicates = cb.and(predicates, cb.equal(root.get("targetType"), finalTargetType));
            }

            // Fetch related entities eagerly to avoid LazyInitializationException
            // This is critical for proper DTO mapping
            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("reporter", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("reportedUser", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("reportType", jakarta.persistence.criteria.JoinType.LEFT);
                root.fetch("reportEvidences", jakarta.persistence.criteria.JoinType.LEFT);
                query.distinct(true);
            }

            return predicates;
        };

        return reportRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> findAllReportsByReporterId(UUID reporterId, Pageable pageable) {
        return reportRepository.findAllByReporter_Id(reporterId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> findAllReportsByReportedUserId(UUID reportedUserId, Pageable pageable) {
        return reportRepository.findAllByReportedUser_Id(reportedUserId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> findAllReportsByReportedTargetId(UUID reportedTargetId, Pageable pageable) {
        return reportRepository.findAllByTargetId(reportedTargetId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> findAllReportsByTargetType(Constants.TargetReportTypeEnum targetType, Pageable pageable) {
        return reportRepository.findAllByTargetType(targetType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Report> findAllReportsByStatus(Constants.ReportStatusEnum status, Pageable pageable) {
        return reportRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportType> findAllReportTypes(Pageable pageable) {
        return reportTypeRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Report findReportById(UUID id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found with id: " + id));
    }

    @Override
    @Transactional
    public Report createReport(ReportCreateRequest request) throws IOException {
        // Map DTO to Entity
        Report report = reportMapper.mapTo(request, Report.class);

        // Handle evidence image uploads
        Set<String> evidenceUrls = new HashSet<>();
        if (request.getImageFiles() != null) {
            for (MultipartFile imageFile : request.getImageFiles()) {
                if (!imageFile.isEmpty()) {
                    String imageUrl = cloudinaryService.uploadFile(imageFile, "report-evidence");
                    evidenceUrls.add(imageUrl);
                }
            }
        }

        // Set business logic fields
        report.setReporter(userService.getUser());
        report.setStatus(Constants.ReportStatusEnum.PENDING);
        report.setActionType(Constants.ReportActionEnum.NO_ACTION);

        // Automatically determine reported user based on target type and target ID
        User reportedUser = findReportedUserByTargetTypeAndId(report.getTargetType(), report.getTargetId());
        report.setReportedUser(reportedUser);

        // Find report type by enum
        ReportType reportType = reportTypeRepository.findByName(request.getReportType())
                .orElseThrow(() -> new NotFoundException("ReportType not found with name: " + request.getReportType()));
        report.setReportType(reportType);

        // Save the report first
        Report savedReport = reportRepository.save(report);

        // Handle evidence URLs
        if (!evidenceUrls.isEmpty()) {
            for (String evidenceUrl : evidenceUrls) {
                ReportEvidence evidence = ReportEvidence.builder()
                        .report(savedReport)
                        .evidenceImageUrl(evidenceUrl)
                        .build();
                reportEvidenceRepository.save(evidence);
            }
        }

        log.info("Created new report with id: {} for target type: {} with target id: {} reporting user: {} with report type: {}",
                savedReport.getId(), savedReport.getTargetType(), savedReport.getTargetId(), reportedUser.getId(), request.getReportType());

        return savedReport;
    }

    /**
     * Automatically find the reported user based on target type and target ID
     * - SEER: The seer user being reported
     * - SERVICE_PACKAGE: The seer who owns the package
     * - BOOKING: The seer in the booking (assuming customer reports seer)
     * - CHAT: The other participant in the conversation (not the reporter)
     */
    private User findReportedUserByTargetTypeAndId(Constants.TargetReportTypeEnum targetType, UUID targetId) {
        return switch (targetType) {
            case SEER -> {
                // Direct user report
                yield userRepository.findById(targetId)
                        .orElseThrow(() -> new NotFoundException("User not found with id: " + targetId));
            }
            case SERVICE_PACKAGE -> {
                // Report a service package - reported user is the seer who owns the package
                ServicePackage servicePackage = servicePackageRepository.findById(targetId)
                        .orElseThrow(() -> new NotFoundException("Service package not found with id: " + targetId));
                if (servicePackage.getSeer() == null) {
                    throw new NotFoundException("Seer not found for service package: " + targetId);
                }
                yield servicePackage.getSeer();
            }
            case BOOKING -> {
                // Report a booking - reported user is the seer in the booking
                Booking booking = bookingRepository.findById(targetId)
                        .orElseThrow(() -> new NotFoundException("Booking not found with id: " + targetId));
                if (booking.getServicePackage() == null || booking.getServicePackage().getSeer() == null) {
                    throw new NotFoundException("Seer not found for booking: " + targetId);
                }
                yield booking.getServicePackage().getSeer();
            }
            case CHAT -> {
                // Report a conversation - find the other participant (not the reporter)
                Conversation conversation = conversationRepository.findById(targetId)
                        .orElseThrow(() -> new NotFoundException("Conversation not found with id: " + targetId));
                if (conversation.getBooking() == null) {
                    throw new NotFoundException("Booking not found for conversation: " + targetId);
                }
                Booking booking = conversation.getBooking();
                User currentUser = userService.getUser();
                // If current user is customer, reported user is seer and vice versa
                if (booking.getCustomer() != null && booking.getCustomer().getId().equals(currentUser.getId())) {
                    if (booking.getServicePackage() == null || booking.getServicePackage().getSeer() == null) {
                        throw new NotFoundException("Seer not found for conversation: " + targetId);
                    }
                    yield booking.getServicePackage().getSeer();
                } else {
                    if (booking.getCustomer() == null) {
                        throw new NotFoundException("Customer not found for conversation: " + targetId);
                    }
                    yield booking.getCustomer();
                }
            }
        };
    }

    @Override
    @Transactional
    public Report deleteReport(UUID id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found with id: " + id));

        log.info("Deleting report with id: {}", id);
        reportRepository.delete(report);

        return report;
    }

    @Override
    @Transactional
    public Report updateReport(UUID id, ReportUpdateRequest request) {
        Report existingReport = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found with id: " + id));

        // Update fields from request
        if (request.getStatus() != null) {
            existingReport.setStatus(request.getStatus());
            if (request.getStatus() == Constants.ReportStatusEnum.RESOLVED && request.getNote() != null) {
                existingReport.setNote(request.getNote());
            }
        }
        if (request.getActionType() != null) {
            existingReport.setActionType(request.getActionType());
        }
        if (request.getNote() != null) {
            existingReport.setNote(request.getNote());
        }

        log.info("Updated report with id: {}", id);

        return reportRepository.save(existingReport);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportStatsResponse getStatistics() {
        log.info("Getting report statistics");

        long total = reportRepository.count();

        // Tính số báo cáo mới trong tháng này
        java.time.LocalDateTime startOfMonth = java.time.LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        long newThisMonth = reportRepository.countReportsCreatedSince(startOfMonth);

        // Số báo cáo đã giải quyết
        long resolved = reportRepository.countByStatus(Constants.ReportStatusEnum.RESOLVED);

        // Số báo cáo chưa giải quyết (PENDING + VIEWED)
        long pending = reportRepository.countByStatus(Constants.ReportStatusEnum.PENDING);
        long viewed = reportRepository.countByStatus(Constants.ReportStatusEnum.VIEWED);
        long unresolved = pending + viewed;

        return ReportStatsResponse.builder()
                .totalReports(total)
                .newReportsThisMonth(newThisMonth)
                .resolvedReports(resolved)
                .unresolvedReports(unresolved)
                .build();
    }
}
