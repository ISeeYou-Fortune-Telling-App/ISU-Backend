package com.iseeyou.fortunetelling.service.dummydata.domain;

import com.iseeyou.fortunetelling.entity.Certificate;
import com.iseeyou.fortunetelling.entity.CertificateCategory;
import com.iseeyou.fortunetelling.entity.KnowledgeCategory;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.repository.CertificateCategoryRepository;
import com.iseeyou.fortunetelling.repository.CertificateRepository;
import com.iseeyou.fortunetelling.repository.KnowledgeCategoryRepository;
import com.iseeyou.fortunetelling.repository.UserRepository;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class Knowledge {
    private final KnowledgeCategoryRepository knowledgeCategoryRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateCategoryRepository certificateCategoryRepository;
    private final UserRepository userRepository;

    public void createDummyData() {
        // Tạo các knowledge categories
        KnowledgeCategory knowledgeCategory1 = knowledgeCategoryRepository.save(new KnowledgeCategory("Cung Hoàng Đạo", "Thông tin về tính cách, tình duyên và sự nghiệp theo 12 cung hoàng đạo", null));
        KnowledgeCategory knowledgeCategory2 = knowledgeCategoryRepository.save(new KnowledgeCategory("Nhân Tướng Học", "Giải mã tính cách và vận mệnh qua khuôn mặt, dáng người", null));
        KnowledgeCategory knowledgeCategory3 = knowledgeCategoryRepository.save(new KnowledgeCategory("Ngũ Hành", "Phân tích sự tương sinh, tương khắc của Kim - Mộc - Thủy - Hỏa - Thổ", null));
        KnowledgeCategory knowledgeCategory4 = knowledgeCategoryRepository.save(new KnowledgeCategory("Chỉ Tay", "Xem vận mệnh, tình duyên và sự nghiệp qua đường chỉ tay", null));
        KnowledgeCategory knowledgeCategory5 = knowledgeCategoryRepository.save(new KnowledgeCategory("Tarot", "Giải bài tarot để tìm lời khuyên và định hướng cho cuộc sống", null));
        KnowledgeCategory knowledgeCategory6 = knowledgeCategoryRepository.save(new KnowledgeCategory("Khác", "Các hình thức xem bói và dự đoán khác", null));

        log.info("Dummy knowledge categories created successfully.");

        // Lấy danh sách các seer
        List<User> verifiedSeers = userRepository.findAllByRole(Constants.RoleEnum.SEER);
        List<User> unverifiedSeers = userRepository.findAllByRole(Constants.RoleEnum.UNVERIFIED_SEER);

        // Tạo danh sách tất cả categories
        List<KnowledgeCategory> allCategories = Arrays.asList(
                knowledgeCategory1, knowledgeCategory2, knowledgeCategory3,
                knowledgeCategory4, knowledgeCategory5, knowledgeCategory6
        );

        // Tạo certificates cho verified seers
        for (User seer : verifiedSeers) {
            createCertificatesForSeer(seer, allCategories, Constants.CertificateStatusEnum.APPROVED, 2, 5);
        }

        // Tạo certificates cho unverified seers
        for (User seer : unverifiedSeers) {
            // Ngẫu nhiên chọn trạng thái (PENDING hoặc REJECTED)
            Constants.CertificateStatusEnum status = Math.random() > 0.5 ?
                    Constants.CertificateStatusEnum.PENDING : Constants.CertificateStatusEnum.REJECTED;
            createCertificatesForSeer(seer, allCategories, status, 1, 3);
        }

        log.info("Dummy certificates created successfully.");
    }

    private void createCertificatesForSeer(User seer, List<KnowledgeCategory> allCategories,
                                           Constants.CertificateStatusEnum defaultStatus,
                                           int minCertificates, int maxCertificates) {
        Random random = new Random();
        int certificateCount = minCertificates + random.nextInt(maxCertificates - minCertificates + 1);

        // Danh sách các tổ chức cấp chứng chỉ
        List<String> issuers = Arrays.asList(
                "Học viện Chiêm tinh Quốc tế",
                "Hiệp hội Tarot Toàn cầu",
                "Trung tâm Nghiên cứu Nhân tướng học",
                "Viện Ngũ hành Phương Đông",
                "Hội Chiêm tinh học Việt Nam"
        );

        // Danh sách tên chứng chỉ
        List<String> certificateNames = Arrays.asList(
                "Chứng chỉ Chiêm tinh học Cơ bản",
                "Chứng chỉ Tarot Chuyên nghiệp",
                "Chứng chỉ Nhân tướng học Ứng dụng",
                "Chứng chỉ Phong thủy và Ngũ hành",
                "Chứng chỉ Xem chỉ tay",
                "Chứng chỉ Tử vi Đẩu số",
                "Chứng chỉ Numerology",
                "Chứng chỉ Astrology Nâng cao"
        );

        for (int i = 0; i < certificateCount; i++) {
            // Tạo ngày cấp ngẫu nhiên (trong vòng 5 năm trở lại đây)
            LocalDateTime issuedAt = LocalDateTime.now().minusDays(random.nextInt(1825));

            // Tạo ngày hết hạn (50% có, 50% không)
            LocalDateTime expirationDate = null;
            if (random.nextBoolean()) {
                expirationDate = issuedAt.plusYears(1 + random.nextInt(3));
            }

            // Tạo ngày quyết định (nếu status là APPROVED hoặc REJECTED)
            LocalDateTime decisionDate = null;
            String decisionReason = null;

            if (defaultStatus != Constants.CertificateStatusEnum.PENDING) {
                decisionDate = issuedAt.plusDays(1 + random.nextInt(30));
                if (defaultStatus == Constants.CertificateStatusEnum.REJECTED) {
                    decisionReason = "Hồ sơ không đủ điều kiện theo quy định";
                }
            }

            // Tạo certificate
            Certificate certificate = Certificate.builder()
                    .certificateName(certificateNames.get(random.nextInt(certificateNames.size())))
                    .certificateDescription("Chứng chỉ chuyên môn được cấp bởi tổ chức uy tín")
                    .issuedBy(issuers.get(random.nextInt(issuers.size())))
                    .issuedAt(issuedAt)
                    .expirationDate(expirationDate)
                    .certificateUrl("https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755679772/default_rxtl0p.jpg")
                    .status(defaultStatus)
                    .decisionDate(decisionDate)
                    .decisionReason(decisionReason)
                    .seer(seer)
                    .certificateCategories(new HashSet<>())
                    .build();

            certificate = certificateRepository.save(certificate);

            // Thêm categories ngẫu nhiên (1-3 categories)
            int categoryCount = 1 + random.nextInt(3);
            Set<KnowledgeCategory> selectedCategories = new HashSet<>();

            while (selectedCategories.size() < categoryCount) {
                KnowledgeCategory category = allCategories.get(random.nextInt(allCategories.size()));
                if (selectedCategories.add(category)) {
                    CertificateCategory certificateCategory = CertificateCategory.builder()
                            .certificate(certificate)
                            .knowledgeCategory(category)
                            .build();
                    certificateCategoryRepository.save(certificateCategory);
                }
            }
        }
    }
}