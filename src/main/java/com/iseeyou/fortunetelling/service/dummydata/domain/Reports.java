package com.iseeyou.fortunetelling.service.dummydata.domain;

import com.iseeyou.fortunetelling.entity.ServicePackage;
import com.iseeyou.fortunetelling.entity.report.Report;
import com.iseeyou.fortunetelling.entity.report.ReportEvidence;
import com.iseeyou.fortunetelling.entity.report.ReportType;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.repository.ReportEvidenceRepository;
import com.iseeyou.fortunetelling.repository.ReportRepository;
import com.iseeyou.fortunetelling.repository.ReportTypeRepository;
import com.iseeyou.fortunetelling.repository.ServicePackageRepository;
import com.iseeyou.fortunetelling.repository.UserRepository;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class Reports {

    private final ReportRepository reportRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final ReportEvidenceRepository reportEvidenceRepository;
    private final UserRepository userRepository;
    private final ServicePackageRepository servicePackageRepository;

    private final Random random = new Random();

    public void createDummyData() {
        log.info("Bắt đầu tạo dummy data cho Reports...");

        // Create report types first
        createReportTypes();

        // Get existing data
        List<User> allUsers = userRepository.findAll();
        List<User> customers = userRepository.findAllByRole(Constants.RoleEnum.CUSTOMER);
        List<User> seers = userRepository.findAllByRole(Constants.RoleEnum.SEER);
        List<ServicePackage> servicePackages = servicePackageRepository.findAll();
        List<ReportType> reportTypes = reportTypeRepository.findAll();

        if (allUsers.size() < 2 || reportTypes.isEmpty()) {
            log.warn("Không đủ users hoặc report types để tạo reports");
            return;
        }

        // Create reports
        createReports(customers, seers, servicePackages, reportTypes);

        log.info("Hoàn thành tạo dummy data cho Reports");
    }

    private void createReportTypes() {
        log.info("Tạo report types...");

        // Create report types based on the enum values
        Constants.ReportTypeEnum[] reportTypeEnums = Constants.ReportTypeEnum.values();

        for (Constants.ReportTypeEnum reportTypeEnum : reportTypeEnums) {
            if (reportTypeRepository.findByName(reportTypeEnum).isEmpty()) {
                ReportType reportType = ReportType.builder()
                        .name(reportTypeEnum)
                        .description(reportTypeEnum.getValue())
                        .build();

                reportTypeRepository.save(reportType);
                log.info("Đã tạo report type: {}", reportTypeEnum.name());
            }
        }
    }

    private void createReports(List<User> customers, List<User> seers, List<ServicePackage> servicePackages, List<ReportType> reportTypes) {
        log.info("Tạo reports...");

        String[] descriptions = {
                "Người dùng này đã gửi spam nhiều lần trong các cuộc trò chuyện",
                "Nội dung không phù hợp với quy định của cộng đồng",
                "Có hành vi quấy rối và làm phiền người khác",
                "Sử dụng ngôn từ thù ghét và kỳ thị",
                "Có biểu hiện bạo lực trong nội dung",
                "Đăng tải nội dung nhạy cảm không phù hợp",
                "Vi phạm bản quyền của người khác",
                "Giả mạo danh tính và thông tin cá nhân",
                "Có dấu hiệu lừa đảo và gian lận",
                "Nội dung không phù hợp với mục đích sử dụng"
        };

        String[] notes = {
                "Đã kiểm tra và xác nhận vi phạm",
                "Cần theo dõi thêm trước khi có hành động",
                "Đã gửi cảnh báo cho người dùng",
                "Vi phạm nghiêm trọng, cần xử lý ngay",
                "Đã xóa nội dung vi phạm",
                "Tạm thời đình chỉ tài khoản",
                "Không đủ bằng chứng đ��� xử lý",
                "Đã giải quyết xong vấn đề"
        };

        String[] evidenceUrls = {
                "https://example.com/evidence1.jpg",
                "https://example.com/evidence2.jpg",
                "https://example.com/evidence3.jpg",
                "https://example.com/evidence4.jpg",
                "https://example.com/evidence5.jpg"
        };

        Constants.ReportStatusEnum[] statuses = Constants.ReportStatusEnum.values();
        Constants.ReportActionEnum[] actionTypes = Constants.ReportActionEnum.values();
        Constants.TargetReportTypeEnum[] targetTypes = Constants.TargetReportTypeEnum.values();

        // Create 20 sample reports
        for (int i = 0; i < 20; i++) {
            User reporter = customers.get(random.nextInt(customers.size()));
            User reportedUser = seers.get(random.nextInt(seers.size()));
            ReportType reportType = reportTypes.get(random.nextInt(reportTypes.size()));
            Constants.TargetReportTypeEnum targetType = targetTypes[random.nextInt(targetTypes.length)];
            Constants.ReportActionEnum actionType = actionTypes[random.nextInt(actionTypes.length)];

            UUID targetId;
            if (targetType == Constants.TargetReportTypeEnum.SERVICE_PACKAGE && !servicePackages.isEmpty()) {
                targetId = servicePackages.get(random.nextInt(servicePackages.size())).getId();
            } else if (targetType == Constants.TargetReportTypeEnum.SEER) {
                targetId = reportedUser.getId();
            } else {
                // For CHAT and BOOKING, generate random UUID (assuming they exist in other systems)
                targetId = UUID.randomUUID();
            }

            Report report = Report.builder()
                    .targetType(targetType)
                    .targetId(targetId)
                    .reportDescription(descriptions[random.nextInt(descriptions.length)])
                    .status(statuses[random.nextInt(statuses.length)])
                    .actionType(actionType)
                    .note(notes[random.nextInt(notes.length)])
                    .reporter(reporter)
                    .reportedUser(reportedUser)
                    .reportType(reportType)
                    .build();

            Report savedReport = reportRepository.save(report);

            // Add 1-3 evidence images for each report
            int evidenceCount = random.nextInt(3) + 1;
            for (int j = 0; j < evidenceCount; j++) {
                ReportEvidence evidence = ReportEvidence.builder()
                        .report(savedReport)
                        .evidenceImageUrl(evidenceUrls[random.nextInt(evidenceUrls.length)])
                        .build();

                reportEvidenceRepository.save(evidence);
            }

            log.info("Đã tạo report {} với {} evidence images", i + 1, evidenceCount);
        }
    }
}
