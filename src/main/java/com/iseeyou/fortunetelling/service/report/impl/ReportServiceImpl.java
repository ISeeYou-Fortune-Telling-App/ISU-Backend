package com.iseeyou.fortunetelling.service.report.impl;

import com.iseeyou.fortunetelling.entity.chat.Conversation;
import com.iseeyou.fortunetelling.entity.booking.Booking;
import com.iseeyou.fortunetelling.entity.report.Report;
import com.iseeyou.fortunetelling.entity.report.ReportEvidence;
import com.iseeyou.fortunetelling.entity.report.ReportType;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional(readOnly = true)
    public Page<Report> findAllReports(Pageable pageable) {
        return reportRepository.findAll(pageable);
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
    public Report createReport(Report report, Constants.ReportTypeEnum reportTypeEnum, Set<String> evidenceUrls) {
        // Set the reporter to current user
        report.setReporter(userService.getUser());
        report.setStatus(Constants.ReportStatusEnum.PENDING);
        report.setActionType(Constants.ReportActionEnum.NO_ACTION);

        // Automatically determine reported user based on target type and target ID
        User reportedUser = findReportedUserByTargetTypeAndId(report.getTargetType(), report.getTargetId());
        report.setReportedUser(reportedUser);

        // Find report type by enum
        ReportType reportType = reportTypeRepository.findByName(reportTypeEnum)
                .orElseThrow(() -> new NotFoundException("ReportType not found with name: " + reportTypeEnum));
        report.setReportType(reportType);

        // Save the report first
        Report savedReport = reportRepository.save(report);

        // Handle evidence URLs
        if (evidenceUrls != null && !evidenceUrls.isEmpty()) {
            for (String evidenceUrl : evidenceUrls) {
                ReportEvidence evidence = ReportEvidence.builder()
                        .report(savedReport)
                        .evidenceImageUrl(evidenceUrl)
                        .build();
                reportEvidenceRepository.save(evidence);
            }
        }

        log.info("Created new report with id: {} for target type: {} with target id: {} reporting user: {} with report type: {}",
                savedReport.getId(), report.getTargetType(), report.getTargetId(), reportedUser.getId(), reportTypeEnum);

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
    public Report updateReport(UUID id, Report report) {
        Report existingReport = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found with id: " + id));

        existingReport.setStatus(report.getStatus());
        if (report.getStatus() == Constants.ReportStatusEnum.RESOLVED) {
            existingReport.setNote(report.getNote());
        }
        existingReport.setActionType(report.getActionType());

        log.info("Updated report with id: {}", id);

        return reportRepository.save(existingReport);
    }
}
