package com.iseeyou.fortunetelling.service.dummydata.domain;

import com.iseeyou.fortunetelling.entity.certificate.Certificate;
import com.iseeyou.fortunetelling.entity.certificate.CertificateCategory;
import com.iseeyou.fortunetelling.entity.knowledge.ItemCategory;
import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeCategory;
import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeItem;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.repository.certificate.CertificateCategoryRepository;
import com.iseeyou.fortunetelling.repository.certificate.CertificateRepository;
import com.iseeyou.fortunetelling.repository.knowledge.ItemCategoryRepository;
import com.iseeyou.fortunetelling.repository.knowledge.KnowledgeCategoryRepository;
import com.iseeyou.fortunetelling.repository.knowledge.KnowledgeItemRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
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
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final ItemCategoryRepository itemCategoryRepository;

    public void createDummyData() {
        // Tạo các knowledge categories
        KnowledgeCategory knowledgeCategory1 = knowledgeCategoryRepository.save(new KnowledgeCategory("Cung Hoàng Đạo", "Thông tin về tính cách, tình duyên và sự nghiệp theo 12 cung hoàng đạo", null, null, null));
        KnowledgeCategory knowledgeCategory2 = knowledgeCategoryRepository.save(new KnowledgeCategory("Nhân Tướng Học", "Giải mã tính cách và vận mệnh qua khuôn mặt, dáng người", null, null, null));
        KnowledgeCategory knowledgeCategory3 = knowledgeCategoryRepository.save(new KnowledgeCategory("Ngũ Hành", "Phân tích sự tương sinh, tương khắc của Kim - Mộc - Thủy - Hỏa - Thổ", null, null, null));
        KnowledgeCategory knowledgeCategory4 = knowledgeCategoryRepository.save(new KnowledgeCategory("Chỉ Tay", "Xem vận mệnh, tình duyên và sự nghiệp qua đường chỉ tay", null, null, null));
        KnowledgeCategory knowledgeCategory5 = knowledgeCategoryRepository.save(new KnowledgeCategory("Tarot", "Giải bài tarot để tìm lời khuyên và định hướng cho cuộc sống", null, null, null));
        KnowledgeCategory knowledgeCategory6 = knowledgeCategoryRepository.save(new KnowledgeCategory("Khác", "Các hình thức xem bói và dự đoán khác", null, null, null));

        log.info("Dummy knowledge categories created successfully.");

        // Lấy danh sách các seer
        List<User> verifiedSeers = userRepository.findAllByRole(Constants.RoleEnum.SEER);
        List<User> unverifiedSeers = userRepository.findAllByRole(Constants.RoleEnum.UNVERIFIED_SEER);

        // Tạo danh sách tất cả categories
        List<KnowledgeCategory> allCategories = Arrays.asList(
                knowledgeCategory1, knowledgeCategory2, knowledgeCategory3,
                knowledgeCategory4, knowledgeCategory5, knowledgeCategory6
        );

        // Tạo knowledge items
        createKnowledgeItems(allCategories, 30);
        log.info("Dummy knowledge items created successfully.");

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

    private void createKnowledgeItems(List<KnowledgeCategory> categories, int count) {
        Random random = new Random();

        // Danh sách tiêu đề
        List<String> titles = Arrays.asList(
                "Ý nghĩa của lá bài The Fool trong Tarot",
                "Hướng dẫn đọc đường chỉ tay cơ bản",
                "Tính cách của cung Bạch Dương",
                "Cách phân biệt mệnh Kim, Mộc, Thủy, Hỏa, Thổ",
                "Bí mật về đường tình duyên trên bàn tay",
                "Ảnh hưởng của sao Hỏa đối với cung Song Ngư",
                "10 dấu hiệu nhận biết người có số mệnh đặc biệt",
                "Giải mã đường công danh sự nghiệp qua tướng mặt",
                "Cách thức đọc hiểu lá bài The Tower",
                "Ngũ hành tương sinh và ứng dụng trong cuộc sống",
                "Bí quyết cân bằng năng lượng trong phong thủy",
                "Các dấu hiệu trên khuôn mặt báo hiệu vận may",
                "Thời điểm hoàng đạo thuận lợi cho các quyết định lớn",
                "Giải mã giấc mơ và ý nghĩa của chúng",
                "Phương pháp tính toán số mệnh theo ngày sinh",
                "Bí ẩn đằng sau các đường vân tay",
                "Ý nghĩa của các vị trí trên lòng bàn tay",
                "Cách hóa giải vận hạn xấu theo phong thủy",
                "12 cung hoàng đạo và đặc điểm tương thích trong tình yêu",
                "Phương pháp phân tích cung mệnh toàn diện"
        );

        // Nội dung mẫu
        List<String> contentTemplates = Arrays.asList(
                "Trong %s, chúng ta thường thấy nhiều yếu tố quan trọng liên quan đến vận mệnh và tính cách. Những nghiên cứu gần đây chỉ ra rằng %s có thể giúp chúng ta hiểu rõ hơn về bản thân và tương lai. Điều này đặc biệt quan trọng khi chúng ta đang tìm kiếm phương hướng trong cuộc sống.",
                "%s là một trong những khía cạnh thú vị nhất của nghệ thuật bói toán cổ xưa. Nguồn gốc của nó có thể được truy nguyên từ hàng ngàn năm trước và vẫn có giá trị ứng dụng trong đời sống hiện đại. Khi nghiên cứu sâu về %s, chúng ta sẽ thấy những mối liên hệ đáng ngạc nhiên với khoa học hiện đại.",
                "Có một câu nói cổ: '%s là chiếc chìa khóa mở cánh cửa số phận'. Điều này đặc biệt đúng khi chúng ta xem xét cách %s ảnh hưởng đến các quyết định hàng ngày. Những người thông thạo trong lĩnh vực này có thể nhìn thấy các mẫu hình và xu hướng mà người khác bỏ qua."
        );

        // Tạo các knowledge items
        for (int i = 0; i < count; i++) {
            String title;

            if (i < titles.size()) {
                title = titles.get(i);
            } else {
                // Nếu đã dùng hết danh sách tiêu đề có sẵn, tạo tiêu đề ngẫu nhiên
                title = "Bí ẩn về " + titles.get(random.nextInt(titles.size())).toLowerCase();
            }

            // Tạo nội dung
            String contentTemplate = contentTemplates.get(random.nextInt(contentTemplates.size()));
            String topic = categories.get(random.nextInt(categories.size())).getName();
            String content = String.format(contentTemplate, topic, title.toLowerCase()) + " " +
                    String.format(contentTemplate, title.toLowerCase(), topic);

            // Đảm bảo nội dung không quá dài
            if (content.length() > 900) {
                content = content.substring(0, 900);
            }

            // Tạo trạng thái ngẫu nhiên
            Constants.KnowledgeItemStatusEnum status = random.nextBoolean() ?
                    Constants.KnowledgeItemStatusEnum.PUBLISHED : Constants.KnowledgeItemStatusEnum.DRAFT;

            // Tạo số lượt xem ngẫu nhiên
            long viewCount = random.nextInt(5000);

            // Tạo item
            KnowledgeItem knowledgeItem = KnowledgeItem.builder()
                    .title(title)
                    .content(content)
                    .imageUrl("https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755679772/default_rxtl0p.jpg")
                    .viewCount(viewCount)
                    .status(status)
                    .itemCategories(new HashSet<>())
                    .build();

            knowledgeItem = knowledgeItemRepository.save(knowledgeItem);

            // Thêm 1-3 categories ngẫu nhiên
            int categoryCount = 1 + random.nextInt(3);
            Set<KnowledgeCategory> selectedCategories = new HashSet<>();

            while (selectedCategories.size() < categoryCount) {
                KnowledgeCategory category = categories.get(random.nextInt(categories.size()));

                if (selectedCategories.add(category)) {
                    ItemCategory itemCategory = ItemCategory.builder()
                            .knowledgeItem(knowledgeItem)
                            .knowledgeCategory(category)
                            .build();

                    itemCategoryRepository.save(itemCategory);
                }
            }
        }
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