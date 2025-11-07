package com.iseeyou.fortunetelling.service.dummydata.domain;

import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.entity.user.CustomerProfile;
import com.iseeyou.fortunetelling.entity.user.SeerProfile;
import java.time.LocalDateTime;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class Accounts {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void createDummyData() {
        createAccount();
    }

    private void createAccount() {
        log.info("Bắt đầu tạo dummy data cho tài khoản...");

        // 1. Tài khoản ADMIN
        User admin = User.builder()
                .role(Constants.RoleEnum.ADMIN)
                .email("admin@iseeyou.com")
                .phone("0901234567")
                .gender("Nam")
                .password(passwordEncoder.encode("P@sswd123."))
                .fullName("Nguyễn Văn Quản Trị")
                .avatarUrl("https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/dummy_avatar_4_x9iatb.jpg")
                .coverUrl("https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755572340/OIP_jra5av.jpg")
                .profileDescription("Quản trị viên hệ thống xem tướng số")
                .birthDate(LocalDateTime.of(1985, 3, 15, 0, 0))
                .status(Constants.StatusProfileEnum.ACTIVE)
                .isActive(true)
                .build();
        userRepository.save(admin);

        // 2-11. 10 Tài khoản SEER chuyên nghiệp
        createSeerAccount(
                "thayminh@iseeyou.com",
                "0912345678",
                "Nữ",
                "Nguyễn Thị Minh Châu",
                "Chuyên gia Tarot với hơn 15 năm kinh nghiệm, từng đào tạo tại Học viện Tarot Quốc tế Paris. Tư vấn tình yêu, sự nghiệp và định hướng cuộc sống qua bộ bài Tarot.",
                LocalDateTime.of(1978, 5, 12, 0, 0),
                "VCB - 1234567890 - Nguyễn Thị Minh Châu"
        );

        createSeerAccount(
                "thayphuong@iseeyou.com",
                "0923456789",
                "Nữ",
                "Lê Phương Anh",
                "Thạc sĩ Tâm lý học kết hợp Chiêm tinh học phương Tây. Chuyên phân tích biểu đồ sao và tư vấn tính cách, khả năng tương thích trong các mối quan hệ.",
                LocalDateTime.of(1985, 9, 23, 0, 0),
                "ACB - 9876543210 - Lê Phương Anh"
        );

        createSeerAccount(
                "thaytuan@iseeyou.com",
                "0934567890",
                "Nam",
                "Trần Anh Tuấn",
                "Chuyên gia Tử vi Đẩu số với 20 năm kinh nghiệm. Được đào tạo bài bản từ các bậc cao thủ, chuyên xem vận hạn, giải mộng và phong thủy nhà ở.",
                LocalDateTime.of(1975, 3, 8, 0, 0),
                "Techcombank - 0123456789 - Trần Anh Tuấn"
        );

        createSeerAccount(
                "colinh@iseeyou.com",
                "0945678901",
                "Nữ",
                "Phạm Thị Linh",
                "Chuyên gia xem chỉ tay và nhân tướng học, kế thừa nghề từ gia đình với truyền thống hơn 3 đời. Tư vấn về tài lộc, hôn nhân và sức khỏe qua đường chỉ tay.",
                LocalDateTime.of(1982, 11, 15, 0, 0),
                "MB Bank - 5555666777 - Phạm Thị Linh"
        );

        createSeerAccount(
                "thayduc@iseeyou.com",
                "0956789012",
                "Nam",
                "Hoàng Đức Minh",
                "Chuyên gia Ngũ hành và phong thủy, có chứng chỉ quốc tế về Feng Shui. Tư vấn hóa giải vận hạn, chọn ngày giờ tốt và bài trí không gian sống hài hòa.",
                LocalDateTime.of(1980, 7, 20, 0, 0),
                "BIDV - 3333444555 - Hoàng Đức Minh"
        );

        createSeerAccount(
                "cothuy@iseeyou.com",
                "0967890123",
                "Nữ",
                "Vũ Thị Thùy Dung",
                "Chuyên gia Numerology (Thần số học), giúp khách hàng khám phá bản thân qua con số và đưa ra lời khuyên về sự nghiệp, tài chính dựa trên ngày tháng năm sinh.",
                LocalDateTime.of(1987, 2, 28, 0, 0),
                "Vietinbank - 7777888999 - Vũ Thị Thùy Dung"
        );

        createSeerAccount(
                "thaylong@iseeyou.com",
                "0978901234",
                "Nam",
                "Đỗ Văn Long",
                "Chuyên gia về Dịch học và Kinh Dịch, nghiên cứu về Bát quái và Âm dương. Tư vấn chiến lược kinh doanh, đầu tư và các quyết định quan trọng trong cuộc sống.",
                LocalDateTime.of(1970, 12, 5, 0, 0),
                "Sacombank - 2222333444 - Đỗ Văn Long"
        );

        createSeerAccount(
                "cohuong@iseeyou.com",
                "0989012345",
                "Nữ",
                "Bùi Thị Hương",
                "Chuyên gia về 12 cung hoàng đạo phương Tây. Tư vấn về tình yêu, tương thích cặp đôi và định hướng nghề nghiệp dựa trên vị trí các vì sao khi sinh.",
                LocalDateTime.of(1990, 6, 18, 0, 0),
                "TPBank - 6666777888 - Bùi Thị Hương"
        );

        createSeerAccount(
                "thaykhai@iseeyou.com",
                "0990123456",
                "Nam",
                "Ngô Minh Khải",
                "Chuyên gia xem tướng mặt và quán khí sắc. Với kinh nghiệm 18 năm, có thể nhận định tính cách, vận mệnh và xu hướng phát triển của mỗi người qua gương mặt.",
                LocalDateTime.of(1976, 4, 9, 0, 0),
                "VPBank - 4444555666 - Ngô Minh Khải"
        );

        createSeerAccount(
                "cohang@iseeyou.com",
                "0901237890",
                "Nữ",
                "Đinh Thị Hằng",
                "Chuyên gia Oracle Card và giải mộng. Sử dụng bộ bài thiên thần để kết nối với năng lượng vũ trụ, đưa ra lời khuyên và hướng dẫn cho những bước đi trong tương lai.",
                LocalDateTime.of(1983, 10, 30, 0, 0),
                "SHB - 8888999000 - Đinh Thị Hằng"
        );

        // 12. Tài khoản GUEST
        User guest = User.builder()
                .role(Constants.RoleEnum.GUEST)
                .email("khach@iseeyou.com")
                .phone("0934560000")
                .gender("Nữ")
                .password(passwordEncoder.encode("P@sswd123."))
                .fullName("Khách")
                .profileDescription("Tài khoản khách tham quan")
                .birthDate(LocalDateTime.of(1995, 6, 10, 0, 0))
                .status(Constants.StatusProfileEnum.ACTIVE)
                .build();
        userRepository.save(guest);

        // 13-32. 20 Tài khoản CUSTOMER
        createCustomerAccount("customer1@iseeyou.com", "0911111111", "Nam", "Nguyễn Văn An",
                "Yêu thích tìm hiểu về vận mệnh và con đường phát triển bản thân",
                LocalDateTime.of(1990, 3, 21, 0, 0), "Bạch Dương", "Ngọ", "Hỏa");

        createCustomerAccount("customer2@iseeyou.com", "0922222222", "Nữ", "Trần Thị Bình",
                "Quan tâm đến phong thủy và sự hài hòa trong cuộc sống",
                LocalDateTime.of(1992, 5, 15, 0, 0), "Kim Ngưu", "Thân", "Kim");

        createCustomerAccount("customer3@iseeyou.com", "0933333333", "Nam", "Lê Văn Cường",
                "Đam mê nghiên cứu về chiêm tinh và tử vi",
                LocalDateTime.of(1988, 7, 8, 0, 0), "Cự Giải", "Thìn", "Thổ");

        createCustomerAccount("customer4@iseeyou.com", "0944444444", "Nữ", "Phạm Thị Dung",
                "Tin vào sức mạnh của tarot trong việc định hướng cuộc sống",
                LocalDateTime.of(1995, 9, 12, 0, 0), "Xử Nữ", "Hợi", "Mộc");

        createCustomerAccount("customer5@iseeyou.com", "0955555555", "Nam", "Hoàng Văn Đức",
                "Thường xuyên tìm lời khuyên về sự nghiệp và đầu tư",
                LocalDateTime.of(1985, 11, 5, 0, 0), "Thiên Yết", "Sửu", "Thủy");

        createCustomerAccount("customer6@iseeyou.com", "0966666666", "Nữ", "Vũ Thị Hoa",
                "Yêu thích xem chỉ tay và tìm hiểu về tương lai",
                LocalDateTime.of(1993, 1, 18, 0, 0), "Ma Kết", "Dậu", "Kim");

        createCustomerAccount("customer7@iseeyou.com", "0977777777", "Nam", "Đỗ Văn Hùng",
                "Quan tâm đến ngũ hành và cách cân bằng năng lượng",
                LocalDateTime.of(1987, 4, 25, 0, 0), "Kim Ngưu", "Mão", "Hỏa");

        createCustomerAccount("customer8@iseeyou.com", "0988888888", "Nữ", "Bùi Thị Lan",
                "Thích tìm hiểu về cung hoàng đạo và tính cách con người",
                LocalDateTime.of(1991, 6, 30, 0, 0), "Cự Giải", "Mùi", "Thổ");

        createCustomerAccount("customer9@iseeyou.com", "0999999999", "Nam", "Ngô Văn Minh",
                "Đang tìm kiếm định hướng cho công việc kinh doanh",
                LocalDateTime.of(1989, 8, 14, 0, 0), "Sư Tử", "Tỵ", "Mộc");

        createCustomerAccount("customer10@iseeyou.com", "0900000000", "Nữ", "Đinh Thị Nga",
                "Quan tâm đến tình duyên và các mối quan hệ",
                LocalDateTime.of(1994, 10, 22, 0, 0), "Thiên Bình", "Tuất", "Thủy");

        createCustomerAccount("customer11@iseeyou.com", "0911122222", "Nam", "Phan Văn Phong",
                "Yêu thích numerology và khám phá ý nghĩa của con số",
                LocalDateTime.of(1986, 12, 3, 0, 0), "Nhân Mã", "Dần", "Hỏa");

        createCustomerAccount("customer12@iseeyou.com", "0922233333", "Nữ", "Mai Thị Quỳnh",
                "Thường xuyên tham khảo ý kiến về sức khỏe và gia đình",
                LocalDateTime.of(1992, 2, 17, 0, 0), "Bảo Bình", "Thân", "Kim");

        createCustomerAccount("customer13@iseeyou.com", "0933344444", "Nam", "Lý Văn Sơn",
                "Quan tâm đến phong thủy nhà ở và văn phòng",
                LocalDateTime.of(1990, 4, 9, 0, 0), "Bạch Dương", "Ngọ", "Thổ");

        createCustomerAccount("customer14@iseeyou.com", "0944455555", "Nữ", "Tô Thị Tâm",
                "Tin vào oracle card và năng lượng vũ trụ",
                LocalDateTime.of(1996, 6, 21, 0, 0), "Song Tử", "Tý", "Mộc");

        createCustomerAccount("customer15@iseeyou.com", "0955566666", "Nam", "Cao Văn Tuấn",
                "Đang tìm hiểu về vận hạn và cách hóa giải",
                LocalDateTime.of(1988, 8, 7, 0, 0), "Sư Tử", "Thìn", "Thủy");

        createCustomerAccount("customer16@iseeyou.com", "0966677777", "Nữ", "Dương Thị Uyên",
                "Yêu thích nhân tướng học và đọc vị con người",
                LocalDateTime.of(1993, 10, 11, 0, 0), "Thiên Bình", "Dậu", "Hỏa");

        createCustomerAccount("customer17@iseeyou.com", "0977788888", "Nam", "Trịnh Văn Vũ",
                "Quan tâm đến chiêm tinh học phương Đông",
                LocalDateTime.of(1985, 12, 28, 0, 0), "Ma Kết", "Sửu", "Kim");

        createCustomerAccount("customer18@iseeyou.com", "0988899999", "Nữ", "Hồ Thị Xuân",
                "Thích tìm hiểu về tử vi đẩu số và lá số tứ trụ",
                LocalDateTime.of(1991, 3, 5, 0, 0), "Song Ngư", "Mùi", "Thổ");

        createCustomerAccount("customer19@iseeyou.com", "0900011111", "Nam", "Võ Văn Yên",
                "Đang tìm kiếm cơ hội và thời điểm tốt cho các quyết định lớn",
                LocalDateTime.of(1987, 5, 19, 0, 0), "Kim Ngưu", "Mão", "Mộc");

        createCustomerAccount("customer20@iseeyou.com", "0911133333", "Nữ", "Đặng Thị Thảo",
                "Yêu thích tarot và tìm kiếm sự chỉ dẫn từ các lá bài",
                LocalDateTime.of(1994, 7, 26, 0, 0), "Sư Tử", "Tuất", "Thủy");

        log.info("Đã tạo thành công 32 tài khoản (1 admin, 10 seer, 1 guest, 20 customer)");
    }

    private void createSeerAccount(String email, String phone, String gender, String fullName,
                                   String profileDescription, LocalDateTime birthDate,
                                   String paymentInfo) {
        User seer = User.builder()
                .role(Constants.RoleEnum.SEER)
                .email(email)
                .phone(phone)
                .gender(gender)
                .password(passwordEncoder.encode("P@sswd123."))
                .fullName(fullName)
                .avatarUrl("https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/dummy_avatar_4_x9iatb.jpg")
                .coverUrl("https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755572340/OIP_jra5av.jpg")
                .profileDescription(profileDescription)
                .birthDate(birthDate)
                .status(Constants.StatusProfileEnum.VERIFIED)
                .isActive(true)
                .build();

        SeerProfile seerProfile = SeerProfile.builder()
                .user(seer)
                .paymentInfo(paymentInfo)
                .build();

        seer.setSeerProfile(seerProfile);
        seerProfile.setUser(seer);

        userRepository.save(seer);
        log.info("Đã tạo tài khoản Seer: {}", fullName);
    }

    private void createCustomerAccount(String email, String phone, String gender, String fullName,
                                       String profileDescription, LocalDateTime birthDate,
                                       String zodiacSign, String chineseZodiac, String fiveElements) {
        User customer = User.builder()
                .role(Constants.RoleEnum.CUSTOMER)
                .email(email)
                .phone(phone)
                .gender(gender)
                .password(passwordEncoder.encode("P@sswd123."))
                .fullName(fullName)
                .avatarUrl("https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/dummy_avatar_4_x9iatb.jpg")
                .coverUrl("https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755572340/OIP_jra5av.jpg")
                .profileDescription(profileDescription)
                .birthDate(birthDate)
                .status(Constants.StatusProfileEnum.ACTIVE)
                .isActive(true)
                .build();

        CustomerProfile customerProfile = CustomerProfile.builder()
                .user(customer)
                .zodiacSign(zodiacSign)
                .chineseZodiac(chineseZodiac)
                .fiveElements(fiveElements)
                .build();

        customer.setCustomerProfile(customerProfile);
        customerProfile.setUser(customer);

        userRepository.save(customer);
        log.info("Đã tạo tài khoản Customer: {}", fullName);
    }
}
