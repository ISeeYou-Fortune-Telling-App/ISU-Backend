package com.iseeyou.fortunetelling.service.dummydata.domain;

import com.iseeyou.fortunetelling.entity.*;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.repository.*;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@RequiredArgsConstructor
@Service
public class ServicePackages {

    private final ServicePackageRepository servicePackageRepository;
    private final PackageCategoryRepository packageCategoryRepository;
    private final ServiceReviewRepository serviceReviewRepository;
    private final PackageInteractionRepository packageInteractionRepository;
    private final UserRepository userRepository;
    private final KnowledgeCategoryRepository knowledgeCategoryRepository;

    private final Random random = new Random();

    public void createDummyData() {
        log.info("Bắt đầu tạo dummy data cho Service Packages...");

        // Get existing users and categories
        List<User> seers = userRepository.findAllByRole(Constants.RoleEnum.SEER);
        List<User> customers = userRepository.findAllByRole(Constants.RoleEnum.CUSTOMER);
        List<KnowledgeCategory> categories = knowledgeCategoryRepository.findAll();

        if (seers.isEmpty() || categories.isEmpty()) {
            log.warn("Không có seer hoặc knowledge category để tạo service packages");
            return;
        }

        // Create service packages
        createServicePackages(seers, categories);

        // Create package categories (N:N relationships)
        createPackageCategories();

        // Create package interactions (likes/dislikes)
        createPackageInteractions(customers);

        // Create service reviews (comments)
        createServiceReviews(customers);

        log.info("Hoàn thành tạo dummy data cho Service Packages.");
    }

    private void createServicePackages(List<User> seers, List<KnowledgeCategory> categories) {
        String[] packageTitles = {
            "Xem Tướng Tổng Quát - Giải Mã Vận Mệnh",
            "Tư Vấn Tình Duyên Theo Cung Hoàng Đạo",
            "Phong Thủy Nhà Ở - Hướng Dẫn Bố Trí",
            "Xem Chỉ Tay - Đọc Vận Mệnh Tương Lai",
            "Tarot Tình Yêu - Giải Đáp Thắc Mắc",
            "Xem Tướng Sự Nghiệp - Định Hướng Tương Lai",
            "Tư Vấn Hôn Nhân - Hạnh Phúc Gia Đình",
            "Xem Ngày Tốt - Chọn Thời Điểm Phù Hợp",
            "Giải Mộng - Ý Nghĩa Giấc Mơ",
            "Xem Tướng Trẻ Em - Phát Triển Tương Lai"
        };

        String[] packageContents = {
            "Phân tích toàn diện về tính cách, vận mệnh và tương lai của bạn thông qua việc xem tướng khuôn mặt và đặc điểm cơ thể.",
            "Tìm hiểu về tình duyên, tình yêu và mối quan hệ của bạn dựa trên cung hoàng đạo và các yếu tố thiên văn.",
            "Hướng dẫn chi tiết về cách bố trí nhà cửa, văn phòng theo phong thủy để thu hút may mắn và thịnh vượng.",
            "Đọc các đường chỉ tay để dự đoán về sức khỏe, tình yêu, sự nghiệp và tuổi thọ của bạn.",
            "Sử dụng bộ bài Tarot để giải đáp các câu hỏi về tình yêu, mối quan hệ và hướng đi trong tương lai.",
            "Phân tích khả năng, tài năng và định hướng sự nghiệp phù hợp nhất với bạn thông qua xem tướng.",
            "Tư vấn về hôn nhân, gia đình và cách xây dựng mối quan hệ hạnh phúc bền vững.",
            "Chọn ngày tốt cho các sự kiện quan trọng như cưới hỏi, khai trương, khởi công dựa trên lịch âm.",
            "Giải thích ý nghĩa các giấc mơ và những điềm báo trong cuộc sống hàng ngày của bạn.",
            "Xem tướng và tư vấn cho trẻ em về tính cách, khả năng và hướng phát triển tương lai."
        };

        String[] imageUrls = {
            "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg",
            "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg",
            "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg",
            "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg",
            "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg"
        };

        for (int i = 0; i < packageTitles.length; i++) {
            User seer = seers.get(random.nextInt(seers.size()));

            ServicePackage servicePackage = ServicePackage.builder()
                    .packageTitle(packageTitles[i])
                    .packageContent(packageContents[i])
                    .imageUrl(imageUrls[random.nextInt(imageUrls.length)])
                    .durationMinutes(30 + random.nextInt(61)) // 30-90 minutes
                    .price(100000.0 + random.nextInt(400000)) // 100k-500k VND
                    .status(Constants.PackageStatusEnum.AVAILABLE)
                    .rejectionReason(null)
                    .likeCount(random.nextLong(50))
                    .dislikeCount(random.nextLong(10))
                    .commentCount(random.nextLong(20))
                    .seer(seer)
                    .build();

            servicePackageRepository.save(servicePackage);
            log.info("Đã tạo service package: {}", packageTitles[i]);
        }
    }

    private void createPackageCategories() {
        List<ServicePackage> packages = servicePackageRepository.findAll();
        List<KnowledgeCategory> categories = knowledgeCategoryRepository.findAll();

        for (ServicePackage servicePackage : packages) {
            // Each package should have 1-3 categories
            int numCategories = 1 + random.nextInt(3);

            for (int i = 0; i < numCategories; i++) {
                KnowledgeCategory category = categories.get(random.nextInt(categories.size()));

                // Check if relationship already exists
                if (packageCategoryRepository.findByServicePackage_IdAndKnowledgeCategory_Id(
                        servicePackage.getId(), category.getId()).isEmpty()) {

                    PackageCategory packageCategory = PackageCategory.builder()
                            .servicePackage(servicePackage)
                            .knowledgeCategory(category)
                            .build();

                    packageCategoryRepository.save(packageCategory);
                }
            }
        }

        log.info("Đã tạo các mối quan hệ package-category");
    }

    private void createPackageInteractions(List<User> customers) {
        if (customers.isEmpty()) {
            log.warn("Không có customer để tạo package interactions");
            return;
        }

        List<ServicePackage> packages = servicePackageRepository.findAll();

        for (ServicePackage servicePackage : packages) {
            // Each package gets interactions from 5-15 random customers
            int numInteractions = 5 + random.nextInt(11);

            for (int i = 0; i < numInteractions; i++) {
                User customer = customers.get(random.nextInt(customers.size()));

                // Check if interaction already exists
                if (packageInteractionRepository.findByUser_IdAndServicePackage_Id(
                        customer.getId(), servicePackage.getId()).isEmpty()) {

                    PackageInteraction interaction = PackageInteraction.builder()
                            .user(customer)
                            .servicePackage(servicePackage)
                            .isLike(random.nextBoolean()) // Random like/dislike
                            .build();

                    packageInteractionRepository.save(interaction);
                }
            }
        }

        log.info("Đã tạo các package interactions (likes/dislikes)");
    }

    private void createServiceReviews(List<User> customers) {
        if (customers.isEmpty()) {
            log.warn("Không có customer để tạo service reviews");
            return;
        }

        List<ServicePackage> packages = servicePackageRepository.findAll();

        String[] comments = {
            "Dịch vụ rất tốt, thầy xem rất chính xác!",
            "Tư vấn chi tiết và hữu ích, cảm ơn thầy!",
            "Rất hài lòng với kết quả xem tướng.",
            "Thầy giải thích rất rõ ràng và dễ hiểu.",
            "Dịch vụ chuyên nghiệp, sẽ quay lại lần sau.",
            "Xem rất chuẩn, đúng như thực tế!",
            "Cảm ơn thầy đã tư vấn tận tình.",
            "Giá cả hợp lý, chất lượng tốt.",
            "Thầy rất nhiệt tình và chu đáo.",
            "Kết quả vượt ngoài mong đợi!"
        };

        String[] replies = {
            "Cảm ơn bạn đã tin tưởng sử dụng dịch vụ!",
            "Rất vui khi giúp được bạn!",
            "Chúc bạn may mắn và thành công!",
            "Hẹn gặp lại bạn lần sau!",
            "Cảm ơn feedback tích cực của bạn!"
        };

        for (ServicePackage servicePackage : packages) {
            // Each package gets 3-8 comments
            int numComments = 3 + random.nextInt(6);

            for (int i = 0; i < numComments; i++) {
                User customer = customers.get(random.nextInt(customers.size()));

                // Create main comment
                ServiceReview comment = ServiceReview.builder()
                        .comment(comments[random.nextInt(comments.length)])
                        .servicePackage(servicePackage)
                        .user(customer)
                        .parentReview(null)
                        .build();

                ServiceReview savedComment = serviceReviewRepository.save(comment);

                // 30% chance to have a reply from the seer
                if (random.nextFloat() < 0.3) {
                    ServiceReview reply = ServiceReview.builder()
                            .comment(replies[random.nextInt(replies.length)])
                            .servicePackage(servicePackage)
                            .user(servicePackage.getSeer())
                            .parentReview(savedComment)
                            .build();

                    serviceReviewRepository.save(reply);
                }
            }
        }

        log.info("Đã tạo các service reviews (comments và replies)");
    }
}
