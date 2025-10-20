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

        // 2. Tài khoản SEER đã xác thực
        User seer = User.builder()
                .role(Constants.RoleEnum.SEER)
                .email("thaytu@iseeyou.com")
                .phone("0912345678")
                .gender("Nữ")
                .password(passwordEncoder.encode("P@sswd123."))
                .fullName("Trần Thị Minh Thầy")
                .avatarUrl("https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/dummy_avatar_4_x9iatb.jpg")
                .coverUrl("https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755572340/OIP_jra5av.jpg")
                .profileDescription("Thầy cúng có 20 năm kinh nghiệm xem tướng, bói bài")
                .birthDate(LocalDateTime.of(1975, 8, 20, 0, 0))
                .status(Constants.StatusProfileEnum.VERIFIED)
                .isActive(true)
                .build();
        // Tạo SeerProfile cho tài khoản SEER
        SeerProfile seerProfile = SeerProfile.builder()
                .user(seer)
                .avgRating(4.8)
                .totalRates(156)
                .paymentInfo("Chuyển khoản: MB Bank - 123456789 - Trần Thị Minh Thầy")
                .build();
        seer.setSeerProfile(seerProfile);
        seerProfile.setUser(seer);

        userRepository.save(seer);



        // 3. Tài khoản UNVERIFIED_SEER
        User unverifiedSeer = User.builder()
                .role(Constants.RoleEnum.UNVERIFIED_SEER)
                .email("thaymoi@iseeyou.com")
                .phone("0923456789")
                .gender("Nam")
                .password(passwordEncoder.encode("P@sswd123."))
                .fullName("Lê Văn Thầy Mới")
                .avatarUrl("https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/dummy_avatar_4_x9iatb.jpg")
                .coverUrl("https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755572340/OIP_jra5av.jpg")
                .profileDescription("Thầy tử vi mới đăng ký, đang chờ xác thực")
                .birthDate(LocalDateTime.of(1990, 12, 5, 0, 0))
                .status(Constants.StatusProfileEnum.UNVERIFIED)
                .isActive(true)
                .build();

        // Tạo SeerProfile cho tài khoản UNVERIFIED_SEER
        SeerProfile unverifiedSeerProfile = SeerProfile.builder()
                .user(unverifiedSeer)
                .avgRating(0.0)
                .totalRates(0)
                .paymentInfo("Chưa cập nhật thông tin thanh toán")
                .build();
        unverifiedSeer.setSeerProfile(unverifiedSeerProfile);
        unverifiedSeerProfile.setUser(unverifiedSeer);

        userRepository.save(unverifiedSeer);


        // 4. Tài khoản GUEST
        User guest = User.builder()
                .role(Constants.RoleEnum.GUEST)
                .email("khach@iseeyou.com")
                .phone("0934567890")
                .gender("Nữ")
                .password(passwordEncoder.encode("P@sswd123."))
                .fullName("Khách")
                .profileDescription("Tài khoản khách tham quan")
                .birthDate(LocalDateTime.of(1995, 6, 10, 0, 0))
                .status(Constants.StatusProfileEnum.ACTIVE)
                .build();
        userRepository.save(guest);

        // 5. Tài khoản CUSTOMER
        User customer = User.builder()
                .role(Constants.RoleEnum.CUSTOMER)
                .email("khachhang@iseeyou.com")
                .phone("0945678901")
                .gender("Nam")
                .password(passwordEncoder.encode("P@sswd123."))
                .fullName("Phạm Văn Khách Hàng")
                .avatarUrl("https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755570460/dummy_avatar_4_x9iatb.jpg")
                .coverUrl("https://res.cloudinary.com/dzpv3mfjt/image/upload/v1755572340/OIP_jra5av.jpg")
                .profileDescription("Khách hàng thường xuyên sử dụng dịch vụ xem tướng")
                .birthDate(LocalDateTime.of(1988, 11, 25, 0, 0))
                .status(Constants.StatusProfileEnum.ACTIVE)
                .isActive(true)
                .build();
        // Tạo CustomerProfile cho tài khoản CUSTOMER
        CustomerProfile customerProfile = CustomerProfile.builder()
                .user(customer)
                .zodiacSign("Nhân Mã")
                .chineseZodiac("Thìn")
                .fiveElements("Mộc")
                .build();
        customer.setCustomerProfile(customerProfile);
        customerProfile.setUser(customer);

        userRepository.save(customer);

        log.info("Đã tạo thành công 5 tài khoản dummy data");
    }
}
