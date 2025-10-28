package com.iseeyou.fortunetelling.service.dummydata.domain;

import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeCategory;
import com.iseeyou.fortunetelling.entity.servicepackage.PackageCategory;
import com.iseeyou.fortunetelling.entity.servicepackage.PackageInteraction;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.entity.servicepackage.ServiceReview;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.repository.knowledge.KnowledgeCategoryRepository;
import com.iseeyou.fortunetelling.repository.servicepackage.PackageCategoryRepository;
import com.iseeyou.fortunetelling.repository.servicepackage.PackageInteractionRepository;
import com.iseeyou.fortunetelling.repository.servicepackage.ServicePackageRepository;
import com.iseeyou.fortunetelling.repository.servicepackage.ServiceReviewRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
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
        
        // Update like/dislike counts based on actual interactions
        updatePackageCounts();

        // Create service reviews (comments)
        createServiceReviews(customers);

        log.info("Hoàn thành tạo dummy data cho Service Packages.");
    }

    private void createServicePackages(List<User> seers, List<KnowledgeCategory> categories) {
        int packageCount = 0;

        // Tạo gói dịch vụ cho từng seer dựa trên email/chuyên môn
        for (User seer : seers) {
            List<PackageData> packages = getPackagesForSeer(seer.getEmail());

            for (PackageData packageData : packages) {
                ServicePackage servicePackage = ServicePackage.builder()
                        .packageTitle(packageData.title)
                        .packageContent(packageData.content)
                        .imageUrl(packageData.imageUrl)
                        .durationMinutes(packageData.duration)
                        .price(packageData.price)
                        .status(Constants.PackageStatusEnum.AVAILABLE)
                        .rejectionReason(null)
                        .likeCount(0L)
                        .dislikeCount(0L)
                        .commentCount(0L)
                        .seer(seer)
                        .build();

                servicePackageRepository.save(servicePackage);
                packageCount++;
                log.info("Đã tạo service package {} cho seer {}: {}", packageCount, seer.getFullName(), packageData.title);
            }
        }

        log.info("Tổng số gói dịch vụ đã tạo: {}", packageCount);
    }

    private List<PackageData> getPackagesForSeer(String email) {
        List<PackageData> packages = new java.util.ArrayList<>();

        switch (email) {
            case "thayminh@iseeyou.com": // Chuyên gia Tarot
                packages.add(new PackageData("Tarot Tình Yêu - Giải Đáp Thắc Mắc",
                    "Sử dụng bộ bài Tarot để giải đáp các câu hỏi về tình yêu, mối quan hệ và hướng đi trong tương lai. Phân tích sâu về cảm xúc và xu hướng tình cảm.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 45, 250000.0));
                packages.add(new PackageData("Tarot Sự Nghiệp - Định Hướng Tương Lai",
                    "Giải bài Tarot để tìm ra con đường sự nghiệp phù hợp, cơ hội thăng tiến và những thách thức cần vượt qua.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 60, 300000.0));
                packages.add(new PackageData("Tarot Tài Chính - Dự Đoán Vận May",
                    "Phân tích tình hình tài chính, đầu tư và cơ hội kiếm tiền qua lá bài Tarot.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 45, 280000.0));
                packages.add(new PackageData("Tarot Tổng Quát - Xem Vận Mệnh Năm",
                    "Xem tổng quan vận mệnh cả năm qua các lá bài Tarot, dự đoán các sự kiện quan trọng sắp tới.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 90, 450000.0));
                packages.add(new PackageData("Tarot Hằng Ngày - Lời Khuyên Mỗi Ngày",
                    "Rút một lá bài Tarot để nhận lời khuyên và định hướng cho ngày mới.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 15, 100000.0));
                packages.add(new PackageData("Tarot Tâm Linh - Kết Nối Nội Tâm",
                    "Khám phá thế giới tâm linh và kết nối với năng lượng vũ trụ qua Tarot.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 60, 320000.0));
                packages.add(new PackageData("Tarot Yes/No - Trả Lời Nhanh",
                    "Giải đáp các câu hỏi đơn giản với câu trả lời Yes/No qua Tarot.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 20, 150000.0));
                packages.add(new PackageData("Tarot Quá Khứ - Hiện Tại - Tương Lai",
                    "Xem lại quá khứ, hiểu rõ hiện tại và dự đoán tương lai qua spread 3 lá bài.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 50, 270000.0));
                packages.add(new PackageData("Tarot Hôn Nhân - Tư Vấn Gia Đình",
                    "Phân tích mối quan hệ hôn nhân, giải quyết mâu thuẫn và xây dựng hạnh phúc gia đình.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 60, 290000.0));
                packages.add(new PackageData("Tarot Sức Khỏe - Chăm Sóc Bản Thân",
                    "Tư vấn về sức khỏe thể chất và tinh thần qua lá bài Tarot.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 40, 240000.0));
                break;

            case "thayphuong@iseeyou.com": // Chuyên gia Cung Hoàng Đạo
                packages.add(new PackageData("Tư Vấn Tình Duyên Theo Cung Hoàng Đạo",
                    "Phân tích tình yêu và mối quan hệ dựa trên cung hoàng đạo và vị trí các vì sao.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 50, 280000.0));
                packages.add(new PackageData("Xem Tính Cách Theo 12 Cung Hoàng Đạo",
                    "Khám phá tính cách, điểm mạnh và điểm yếu qua cung hoàng đạo của bạn.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 45, 250000.0));
                packages.add(new PackageData("Độ Tương Thích Cặp Đôi - Horoscope",
                    "Phân tích mức độ tương thích giữa hai cung hoàng đạo trong tình yêu và hôn nhân.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 60, 320000.0));
                packages.add(new PackageData("Vận Hành Tinh Hằng Tháng",
                    "Dự báo vận mệnh hằng tháng dựa trên chuyển động của các hành tinh.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 40, 220000.0));
                packages.add(new PackageData("Biểu Đồ Sao (Birth Chart) - Phân Tích Chuyên Sâu",
                    "Tạo và phân tích biểu đồ sao chi tiết từ ngày giờ sinh để hiểu rõ về bản thân.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 90, 500000.0));
                packages.add(new PackageData("Tư Vấn Sự Nghiệp Theo Hoàng Đạo",
                    "Định hướng nghề nghiệp phù hợp dựa trên đặc điểm cung hoàng đạo.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 55, 300000.0));
                packages.add(new PackageData("Vận Mệnh Hằng Năm - Yearly Horoscope",
                    "Dự đoán vận mệnh cả năm cho cung hoàng đạo của bạn.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 75, 400000.0));
                packages.add(new PackageData("Thời Điểm Thuận Lợi - Transit Analysis",
                    "Xác định thời điểm tốt nhất cho các quyết định quan trọng dựa trên transit.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 50, 270000.0));
                packages.add(new PackageData("Moon Phase - Ảnh Hưởng Mặt Trăng",
                    "Phân tích ảnh hưởng của chu kỳ mặt trăng đến cảm xúc và cuộc sống.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 35, 200000.0));
                packages.add(new PackageData("Tư Vấn Con Cái Theo Hoàng Đạo",
                    "Hiểu rõ tính cách và cách nuôi dạy con dựa trên cung hoàng đạo của trẻ.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 60, 310000.0));
                break;

            case "thaytuan@iseeyou.com": // Chuyên gia Tử vi Đẩu số, Ngũ Hành
                packages.add(new PackageData("Xem Tử Vi Trọn Đời",
                    "Phân tích chi tiết vận mệnh từ khi sinh đến cuối đời qua lá số tử vi.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 120, 600000.0));
                packages.add(new PackageData("Phong Thủy Nhà Ở - Ngũ Hành Hài Hòa",
                    "Tư vấn bố trí nhà cửa, văn phòng theo nguyên lý ngũ hành để thu hút tài lộc.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 90, 450000.0));
                packages.add(new PackageData("Xem Ngày Tốt - Chọn Giờ Hoàng Đạo",
                    "Chọn ngày giờ tốt cho cưới hỏi, khai trương, khởi công, xuất hành.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 45, 250000.0));
                packages.add(new PackageData("Giải Mộng - Thông Điệp Từ Giấc Ngủ",
                    "Giải thích ý nghĩa giấc mơ và những điềm báo trong cuộc sống.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 30, 180000.0));
                packages.add(new PackageData("Tư Vấn Vận Hạn Theo Năm",
                    "Xem vận hạn từng năm và cách hóa giải những điều không may.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 60, 320000.0));
                packages.add(new PackageData("Xem Cung Mệnh - Đoán Tính Cách",
                    "Phân tích tính cách, khả năng và số phận qua cung mệnh tử vi.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 75, 380000.0));
                packages.add(new PackageData("Hóa Giải Vận Xui - Đổi Vận",
                    "Tư vấn cách hóa giải vận xui, hạn chế rủi ro và cải thiện vận mệnh.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 50, 280000.0));
                packages.add(new PackageData("Phong Thủy Văn Phòng - Thu Hút Thành Công",
                    "Bố trí văn phòng làm việc theo phong thủy để tăng hiệu suất và thành công.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 70, 350000.0));
                packages.add(new PackageData("Xem Tuổi Hợp - Tư Vấn Hôn Nhân",
                    "Xem xét độ hợp tuổi cho việc kết hôn và xây dựng gia đình.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 55, 290000.0));
                packages.add(new PackageData("Phong Thủy Phòng Ngủ - Giấc Ngủ Ngon",
                    "Tư vấn bố trí phòng ngủ để có giấc ngủ ngon và sức khỏe tốt.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 40, 220000.0));
                break;

            case "colinh@iseeyou.com": // Chuyên gia Chỉ Tay, Nhân Tướng Học
                packages.add(new PackageData("Xem Chỉ Tay - Đọc Vận Mệnh Tương Lai",
                    "Đọc các đường chỉ tay để dự đoán về sức khỏe, tình yêu, sự nghiệp và tuổi thọ.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 60, 300000.0));
                packages.add(new PackageData("Xem Tướng Mặt - Giải Mã Tính Cách",
                    "Phân tích tính cách, vận mệnh qua khuôn mặt và các đặc điểm cơ thể.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 50, 270000.0));
                packages.add(new PackageData("Xem Đường Tình Duyên Trên Tay",
                    "Phân tích đường tình duyên để biết về hôn nhân và các mối quan hệ.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 45, 250000.0));
                packages.add(new PackageData("Xem Đường Sự Nghiệp - Tài Lộc",
                    "Đọc đường sự nghiệp và tài lộc trên bàn tay để dự đoán thành công.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 55, 280000.0));
                packages.add(new PackageData("Xem Tướng Trẻ Em - Phát Triển Tương Lai",
                    "Xem tướng và tư vấn cho trẻ em về tính cách, khả năng và hướng phát triển.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 50, 260000.0));
                packages.add(new PackageData("Xem Đường Sinh Mệnh - Sức Khỏe",
                    "Phân tích đường sinh mệnh để biết về sức khỏe và tuổi thọ.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 40, 230000.0));
                packages.add(new PackageData("Nhân Tướng Học Tổng Quát",
                    "Xem tướng toàn diện từ mặt, tay đến cơ thể để hiểu rõ vận mệnh.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 90, 450000.0));
                packages.add(new PackageData("Xem Tướng Phúc Đức - Phước Lành",
                    "Phân tích tướng phúc đức để biết về phước lành và tài lộc.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 50, 270000.0));
                packages.add(new PackageData("Xem Dáng Người - Vận Mệnh Cuộc Đời",
                    "Phân tích dáng người, tư thế đi đứng để đoán tính cách và số phận.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 45, 240000.0));
                packages.add(new PackageData("Xem Chỉ Tay Cho Doanh Nhân",
                    "Tư vấn đặc biệt cho doanh nhân về kinh doanh và cơ hội đầu tư.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 75, 400000.0));
                break;

            case "thayduc@iseeyou.com": // Chuyên gia Ngũ Hành, Phong Thủy
                packages.add(new PackageData("Phong Thủy Nhà Ở - Cân Bằng Ngũ Hành",
                    "Hướng dẫn bố trí nhà cửa theo ngũ hành để tạo không gian sống hài hòa.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 90, 450000.0));
                packages.add(new PackageData("Phong Thủy Văn Phòng - Thịnh Vượng",
                    "Tư vấn bố trí văn phòng để thu hút tài lộc và sự nghiệp thăng tiến.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 80, 400000.0));
                packages.add(new PackageData("Chọn Màu Sắc Theo Mệnh",
                    "Tư vấn màu sắc phù hợp với mệnh ngũ hành để tăng vận may.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 30, 180000.0));
                packages.add(new PackageData("Hóa Giải Sát Khí Phong Thủy",
                    "Tìm và hóa giải các sát khí xấu trong nhà ở và văn phòng.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 60, 320000.0));
                packages.add(new PackageData("Phong Thủy Phòng Khách - Đón Tài Lộc",
                    "Bố trí phòng khách để đón tài lộc và may mắn vào nhà.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 50, 270000.0));
                packages.add(new PackageData("Chọn Hướng Nhà Theo Mệnh",
                    "Tư vấn chọn hướng nhà phù hợp với mệnh chủ nhà để hưng thịnh.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 70, 350000.0));
                packages.add(new PackageData("Phong Thủy Phòng Bếp - Gia Đạo Êm Ấm",
                    "Bố trí bếp ăn hợp phong thủy để gia đình hạnh phúc, êm ấm.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 45, 240000.0));
                packages.add(new PackageData("Trồng Cây Theo Phong Thủy",
                    "Tư vấn chọn và trồng cây phù hợp để thu hút tài lộc và sức khỏe.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 40, 220000.0));
                packages.add(new PackageData("Phong Thủy Cửa Chính - Đường Vào Tài Lộc",
                    "Tư vấn về cửa chính để thu hút khí tốt và tài lộc vào nhà.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 55, 290000.0));
                packages.add(new PackageData("Bố Trí Bàn Làm Việc Hợp Phong Thủy",
                    "Hướng dẫn bố trí bàn làm việc để tăng hiệu suất và thành công.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 35, 200000.0));
                break;

            case "cothuy@iseeyou.com": // Chuyên gia Numerology
                packages.add(new PackageData("Numerology - Khám Phá Bản Thân Qua Số",
                    "Phân tích con số chủ đạo để hiểu rõ bản thân, tính cách và vận mệnh.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 60, 300000.0));
                packages.add(new PackageData("Số Đường Đời - Life Path Number",
                    "Tính toán và giải thích ý nghĩa số đường đời của bạn.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 50, 270000.0));
                packages.add(new PackageData("Năm Cá Nhân - Personal Year",
                    "Dự đoán vận mệnh năm nay qua con số năm cá nhân.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 40, 230000.0));
                packages.add(new PackageData("Số Tên - Expression Number",
                    "Phân tích ý nghĩa của tên tuổi qua con số biểu đạt.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 45, 250000.0));
                packages.add(new PackageData("Độ Tương Thích Theo Số Học",
                    "Xem mức độ hợp nhau giữa hai người qua con số.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 55, 280000.0));
                packages.add(new PackageData("Chọn Số Điện Thoại May Mắn",
                    "Tư vấn chọn số điện thoại phù hợp để thu hút may mắn.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 30, 180000.0));
                packages.add(new PackageData("Chọn Ngày Tốt Theo Số Học",
                    "Chọn ngày tốt cho sự kiện quan trọng dựa trên numerology.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 35, 200000.0));
                packages.add(new PackageData("Số Linh Hồn - Soul Number",
                    "Khám phá số linh hồn để hiểu về nội tâm sâu thẳm.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 50, 260000.0));
                packages.add(new PackageData("Numerology Cho Kinh Doanh",
                    "Tư vấn chọn tên công ty, logo và chiến lược theo số học.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 90, 450000.0));
                packages.add(new PackageData("Chu Kỳ Cuộc Đời - Life Cycles",
                    "Phân tích các giai đoạn quan trọng trong cuộc đời qua số học.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 75, 380000.0));
                break;

            case "thaylong@iseeyou.com": // Chuyên gia Dịch học, Kinh Dịch
                packages.add(new PackageData("Dịch Học - Giải Mã Quẻ Dịch",
                    "Giải quẻ dịch để tìm lời khuyên cho các quyết định quan trọng.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 90, 450000.0));
                packages.add(new PackageData("Tư Vấn Kinh Doanh Theo Dịch Học",
                    "Tư vấn chiến lược kinh doanh, đầu tư dựa trên Kinh Dịch.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 120, 600000.0));
                packages.add(new PackageData("Xem Quẻ Tình Duyên",
                    "Giải quẻ về tình yêu, hôn nhân và các mối quan hệ.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 60, 320000.0));
                packages.add(new PackageData("Xem Quẻ Sự Nghiệp",
                    "Giải quẻ về công việc, thăng tiến và cơ hội nghề nghiệp.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 75, 380000.0));
                packages.add(new PackageData("Bát Quái - Âm Dương Cân Bằng",
                    "Tư vấn về cân bằng âm dương trong cuộc sống và nhà ở.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 80, 400000.0));
                packages.add(new PackageData("Xem Quẻ Tài Lộc",
                    "Giải quẻ về tài chính, đầu tư và cơ hội kiếm tiền.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 70, 350000.0));
                packages.add(new PackageData("Dịch Học Ứng Dụng Hằng Ngày",
                    "Áp dụng nguyên lý dịch học vào cuộc sống hàng ngày.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 50, 270000.0));
                packages.add(new PackageData("Chọn Ngày Giờ Theo Dịch Học",
                    "Chọn thời điểm tốt nhất cho các sự kiện quan trọng.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 55, 290000.0));
                packages.add(new PackageData("Giải Quẻ Sức Khỏe",
                    "Tư vấn về sức khỏe và cách chăm sóc bản thân qua dịch học.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 60, 310000.0));
                packages.add(new PackageData("Dịch Học Cho Lãnh Đạo",
                    "Tư vấn chiến lược lãnh đạo và quản lý dựa trên Kinh Dịch.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 100, 550000.0));
                break;

            case "cohuong@iseeyou.com": // Chuyên gia Cung Hoàng Đạo
                packages.add(new PackageData("Xem Horoscope Hằng Tuần",
                    "Dự báo vận mệnh hằng tuần cho 12 cung hoàng đạo.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 30, 150000.0));
                packages.add(new PackageData("Tư Vấn Hẹn Hò Theo Cung",
                    "Tư vấn về hẹn hò và tình yêu dựa trên cung hoàng đạo.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 45, 240000.0));
                packages.add(new PackageData("Phân Tích Rising Sign - Cung Thăng",
                    "Giải thích ý nghĩa cung thăng và ảnh hưởng đến cuộc sống.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 50, 270000.0));
                packages.add(new PackageData("Moon Sign - Cung Mặt Trăng",
                    "Phân tích cung mặt trăng và ảnh hưởng đến cảm xúc.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 45, 250000.0));
                packages.add(new PackageData("Venus Sign - Tình Yêu Và Sắc Đẹp",
                    "Khám phá phong cách yêu qua vị trí sao Kim.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 40, 230000.0));
                packages.add(new PackageData("Mars Sign - Năng Lượng Và Hành Động",
                    "Phân tích cách bạn hành động qua vị trí sao Hỏa.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 40, 230000.0));
                packages.add(new PackageData("Tư Vấn Nghề Nghiệp Theo Midheaven",
                    "Định hướng sự nghiệp dựa trên điểm Midheaven.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 60, 310000.0));
                packages.add(new PackageData("Synastry - Tương Thích Cặp Đôi Chuyên Sâu",
                    "Phân tích chi tiết độ tương thích giữa hai biểu đồ sao.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 90, 450000.0));
                packages.add(new PackageData("Composite Chart - Biểu Đồ Quan Hệ",
                    "Tạo biểu đồ tổng hợp để xem mối quan hệ hai người.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 75, 380000.0));
                packages.add(new PackageData("Solar Return - Vận Mệnh Sinh Nhật",
                    "Dự đoán vận mệnh năm mới qua biểu đồ sinh nhật mặt trời.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 70, 360000.0));
                break;

            case "thaykhai@iseeyou.com": // Chuyên gia Nhân Tướng Học
                packages.add(new PackageData("Xem Tướng Mặt Tổng Quát",
                    "Phân tích toàn diện khuôn mặt để biết tính cách và vận mệnh.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 60, 300000.0));
                packages.add(new PackageData("Quán Khí Sắc - Đoán Vận May",
                    "Nhìn khí sắc trên mặt để dự đoán vận may gần đây.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 45, 250000.0));
                packages.add(new PackageData("Xem Tướng Mắt - Cửa Sổ Tâm Hồn",
                    "Phân tích đôi mắt để hiểu về tính cách và tình cảm.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 40, 220000.0));
                packages.add(new PackageData("Xem Tướng Mũi - Tài Lộc Giàu Sang",
                    "Phân tích mũi để biết về tài lộc và vận mệnh tài chính.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 35, 200000.0));
                packages.add(new PackageData("Xem Tướng Miệng - Giao Tiếp Xã Hội",
                    "Phân tích miệng và môi để biết về khả năng giao tiếp.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 35, 200000.0));
                packages.add(new PackageData("Xem Tướng Trán - Trí Tuệ Học Vấn",
                    "Phân tích trán để biết về trí tuệ và khả năng học tập.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 40, 220000.0));
                packages.add(new PackageData("Xem Tướng Tai - Phúc Lộc Thọ",
                    "Phân tích tai để biết về phúc lộc và tuổi thọ.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 35, 200000.0));
                packages.add(new PackageData("Xem Tướng Cằm - Vận May Tuổi Già",
                    "Phân tích cằm để biết về vận may tuổi già và con cái.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 35, 200000.0));
                packages.add(new PackageData("Xem Tướng Gò Má - Quyền Lực Địa Vị",
                    "Phân tích gò má để biết về quyền lực và địa vị xã hội.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 40, 220000.0));
                packages.add(new PackageData("Nhân Tướng Học Cho Tuyển Dụng",
                    "Tư vấn tuyển dụng nhân sự qua nhân tướng học.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 90, 480000.0));
                break;

            case "cohang@iseeyou.com": // Chuyên gia Oracle Card, Giải mộng
                packages.add(new PackageData("Oracle Card - Lời Khuyên Thiên Thần",
                    "Rút bài Oracle để nhận lời khuyên từ các thiên thần hộ mệnh.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 45, 250000.0));
                packages.add(new PackageData("Giải Mộng - Thông Điệp Tiềm Thức",
                    "Giải thích ý nghĩa giấc mơ và thông điệp từ tiềm thức.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 40, 220000.0));
                packages.add(new PackageData("Angel Card Reading - Kết Nối Thiên Thần",
                    "Kết nối với thiên thần để nhận hướng dẫn và bảo vệ.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 50, 270000.0));
                packages.add(new PackageData("Oracle Tình Yêu - Lời Khuyên Mối Quan Hệ",
                    "Sử dụng Oracle card để tư vấn về tình yêu và mối quan hệ.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 45, 240000.0));
                packages.add(new PackageData("Oracle Sự Nghiệp - Định Hướng Con Đường",
                    "Nhận lời khuyên về sự nghiệp và con đường phát triển.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 50, 260000.0));
                packages.add(new PackageData("Giải Mộng Chuyên Sâu - Phân Tích Tâm Lý",
                    "Phân tích giấc mơ từ góc độ tâm lý học và tâm linh.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 60, 300000.0));
                packages.add(new PackageData("Oracle Năng Lượng - Cân Bằng Chakra",
                    "Sử dụng Oracle để cân bằng năng lượng và chakra.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_2.jpg", 55, 280000.0));
                packages.add(new PackageData("Giải Mộng Điềm Báo",
                    "Giải thích các giấc mơ có tính chất điềm báo tương lai.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_3.jpg", 45, 250000.0));
                packages.add(new PackageData("Oracle Chữa Lành - Healing Reading",
                    "Sử dụng bài Oracle để chữa lành tâm hồn và cảm xúc.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_4.jpg", 60, 310000.0));
                packages.add(new PackageData("Oracle Phát Triển Bản Thân",
                    "Nhận hướng dẫn để phát triển bản thân và nâng cao năng lượng.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_5.jpg", 50, 270000.0));
                break;

            default:
                // Gói dịch vụ mặc định nếu không khớp email
                packages.add(new PackageData("Xem Tướng Tổng Quát",
                    "Phân tích toàn diện về tính cách và vận mệnh.",
                    "https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/service_package_1.jpg", 60, 300000.0));
                break;
        }

        return packages;
    }

    // Inner class để lưu thông tin package
    private static class PackageData {
        String title;
        String content;
        String imageUrl;
        int duration;
        double price;

        PackageData(String title, String content, String imageUrl, int duration, double price) {
            this.title = title;
            this.content = content;
            this.imageUrl = imageUrl;
            this.duration = duration;
            this.price = price;
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
                            .interactionType(random.nextBoolean() ? 
                                Constants.InteractionTypeEnum.LIKE : 
                                Constants.InteractionTypeEnum.DISLIKE)
                            .build();

                    packageInteractionRepository.save(interaction);
                }
            }
        }

        log.info("Đã tạo các package interactions (likes/dislikes)");
    }

    private void updatePackageCounts() {
        List<ServicePackage> packages = servicePackageRepository.findAll();
        
        for (ServicePackage servicePackage : packages) {
            // Count likes
            long likeCount = packageInteractionRepository.countByServicePackage_IdAndInteractionType(
                    servicePackage.getId(), Constants.InteractionTypeEnum.LIKE);
            
            // Count dislikes
            long dislikeCount = packageInteractionRepository.countByServicePackage_IdAndInteractionType(
                    servicePackage.getId(), Constants.InteractionTypeEnum.DISLIKE);
            
            // Update package
            servicePackage.setLikeCount(likeCount);
            servicePackage.setDislikeCount(dislikeCount);
            servicePackageRepository.save(servicePackage);
            
            log.debug("Updated package {} - Likes: {}, Dislikes: {}", 
                    servicePackage.getPackageTitle(), likeCount, dislikeCount);
        }
        
        log.info("Đã cập nhật like/dislike counts cho tất cả packages dựa trên interactions thực tế");
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
