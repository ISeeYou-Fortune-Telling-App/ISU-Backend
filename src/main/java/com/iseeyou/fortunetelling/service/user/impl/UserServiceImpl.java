package com.iseeyou.fortunetelling.service.user.impl;

import com.iseeyou.fortunetelling.dto.request.auth.RegisterRequest;
import com.iseeyou.fortunetelling.dto.request.auth.SeerRegisterRequest;
import com.iseeyou.fortunetelling.dto.request.certificate.CertificateCreateRequest;
import com.iseeyou.fortunetelling.dto.request.user.UpdateUserRequest;
import com.iseeyou.fortunetelling.dto.request.user.UpdateUserRoleRequest;
import com.iseeyou.fortunetelling.dto.response.account.AccountStatsResponse;
import com.iseeyou.fortunetelling.entity.certificate.Certificate;
import com.iseeyou.fortunetelling.entity.user.CustomerProfile;
import com.iseeyou.fortunetelling.entity.user.SeerProfile;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.booking.BookingRepository;
import com.iseeyou.fortunetelling.repository.user.UserRepository;
import com.iseeyou.fortunetelling.security.JwtUserDetails;
import com.iseeyou.fortunetelling.service.MessageSourceService;
import com.iseeyou.fortunetelling.service.certificate.CertificateService;
import com.iseeyou.fortunetelling.service.email.EmailVerificationService;
import com.iseeyou.fortunetelling.service.fileupload.CloudinaryService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.CalculateZodiac;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSourceService messageSourceService;
    private final CloudinaryService cloudinaryService;
    private final CertificateService certificateService;
    private final EmailVerificationService emailVerificationService;
    private final BookingRepository bookingRepository;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            MessageSourceService messageSourceService,
            CloudinaryService cloudinaryService,
            @Lazy CertificateService certificateService,
            EmailVerificationService emailVerificationService,
            BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSourceService = messageSourceService;
        this.cloudinaryService = cloudinaryService;
        this.certificateService = certificateService;
        this.emailVerificationService = emailVerificationService;
        this.bookingRepository = bookingRepository;
    }

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser() {
        Authentication authentication = getAuthentication();
        if (authentication.isAuthenticated()) {
            try {
                return findById(UUID.fromString(getPrincipal(authentication).getId()));
            } catch (ClassCastException e) {
                log.warn("[JWT] User details not found!");
                throw new BadCredentialsException("Bad credentials");
            }
        } else {
            log.warn("[JWT] User not authenticated!");
            throw new BadCredentialsException("Bad credentials");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional
    public User updateStatus(UUID id, String status) {
        User user = findById(id);
        try {
            user.setStatus(Constants.StatusProfileEnum.valueOf(status.toUpperCase()));
            return userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException(messageSourceService.get("invalid_status"));
        }
    }

    /**
     * Find a user by email.
     *
     * @param email String.
     * @return User
     */
    @Transactional(readOnly = true)
    public User findByEmail(final String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
                        new String[]{messageSourceService.get("user")})));
    }

    /**
     * Load user details by username.
     *
     * @param email String
     * @return UserDetails
     * @throws UsernameNotFoundException email not found exception.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(final String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
                        new String[]{messageSourceService.get("user")})));

        return JwtUserDetails.create(user);
    }

    /**
     * Loads user details by UUID string.
     *
     * @param id String
     * @return UserDetails
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserById(final String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new NotFoundException(messageSourceService.get("not_found_with_param",
                        new String[]{messageSourceService.get("user")})));

        return JwtUserDetails.create(user);
    }

    /**
     * Get UserDetails from security context.
     *
     * @param authentication Wrapper for security context
     * @return the Principal being authenticated or the authenticated principal after authentication.
     */
    @Override
    @Transactional(readOnly = true)
    public JwtUserDetails getPrincipal(final Authentication authentication) {
        return (JwtUserDetails) authentication.getPrincipal();
    }

    @Override
    @Transactional
    public User register(final RegisterRequest request) throws BindException {
        if (userRepository.existsByEmail(request.getEmail())) {
            BindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
            bindingResult.addError(new FieldError(bindingResult.getObjectName(), "email",
                    "Email already exists"));
            throw new BindException(bindingResult);
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setBirthDate(request.getBirthDate());
        user.setGender(request.getGender());
        user.setPhone(request.getPhoneNumber());
        user.setRole(Constants.RoleEnum.CUSTOMER);
        user.setStatus(Constants.StatusProfileEnum.VERIFIED);
        user.setIsActive(false); // Tài khoản chưa được xác thực email

        // Calculate Zodiac sign based on birth date
        CustomerProfile customerProfile = new CustomerProfile();

        int day = request.getBirthDate().getDayOfMonth();
        int month = request.getBirthDate().getMonthValue();
        int year = request.getBirthDate().getYear();

        customerProfile.setZodiacSign(CalculateZodiac.getZodiacSign(month, day));
        customerProfile.setChineseZodiac(CalculateZodiac.getChineseZodiac(year));
        customerProfile.setFiveElements(CalculateZodiac.getFiveElements(year));

        customerProfile.setUser(user);
        user.setCustomerProfile(customerProfile);

        userRepository.save(user);

        // Gửi OTP xác thực email
        emailVerificationService.sendVerificationEmail(request.getEmail());
        log.info("User registered and verification email sent to: {}", request.getEmail());

        return user;
    }

    @Override
    @Transactional
    public User seerRegister(SeerRegisterRequest request) throws BindException {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            BindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
            bindingResult.addError(new FieldError(bindingResult.getObjectName(), "email",
                    "Email already exists"));
            throw new BindException(bindingResult);
        }

        // Create and set up user with basic info
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setBirthDate(request.getBirthDate());
        user.setGender(request.getGender());
        user.setPhone(request.getPhoneNumber());
        user.setRole(Constants.RoleEnum.SEER);
        user.setStatus(Constants.StatusProfileEnum.UNVERIFIED);
        user.setProfileDescription(request.getProfileDescription());
        user.setIsActive(false); // Tài khoản chưa được xác thực email

        SeerProfile seerProfile = new SeerProfile();
        seerProfile.setUser(user);
        seerProfile.setAvgRating(0.0);
        seerProfile.setTotalRates(0);
        user.setSeerProfile(seerProfile);

        userRepository.save(user);

        // Process certificates if provided
        if (request.getCertificates() != null && !request.getCertificates().isEmpty()) {
            for (CertificateCreateRequest certRequest : request.getCertificates()) {
                try {
                    // Create certificate entity from request
                    Certificate certificate = new Certificate();
                    certificate.setCertificateName(certRequest.getCertificateName());
                    certificate.setCertificateDescription(certRequest.getCertificateDescription());
                    certificate.setIssuedBy(certRequest.getIssuedBy());
                    certificate.setIssuedAt(certRequest.getIssuedAt());
                    certificate.setExpirationDate(certRequest.getExpirationDate());
                    certificate.setSeer(user);
                    certificate.setStatus(Constants.CertificateStatusEnum.PENDING);

                    // Upload certificate file if provided
                    if (certRequest.getCertificateFile() != null && !certRequest.getCertificateFile().isEmpty()) {
                        String imageUrl = cloudinaryService.uploadFile(certRequest.getCertificateFile(), "certificates");
                        certificate.setCertificateUrl(imageUrl);
                    }

                    // Create certificate with associated categories
                    certificateService.create(certificate, certRequest.getCategoryIds());
                } catch (IOException e) {
                    log.error("Failed to upload certificate file: {}", e.getMessage());
                }
            }
        }

        // Gửi OTP xác thực email
        emailVerificationService.sendVerificationEmail(request.getEmail());
        log.info("Seer registered and verification email sent to: {}", request.getEmail());

        return user;
    }

    @Override
    @Transactional
    public User updateMe(UpdateUserRequest request) throws BindException {
        User user = getUser();
        
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }
        if (request.getProfileDescription() != null) {
            user.setProfileDescription(request.getProfileDescription());
        }
        
        return userRepository.save(user);
    }

    @Override
    public String uploadImage(MultipartFile file, String folderName) throws Exception {
        User existingUser = getUser();

        // Check if the file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        if (existingUser.getAvatarUrl() == null) {
            String imageUrl = cloudinaryService.uploadFile(file, folderName);
            existingUser.setAvatarUrl(imageUrl);
            userRepository.save(existingUser);

            return imageUrl;
        } else {
            // Delete the old image from Cloudinary
            try {
                cloudinaryService.deleteFile(existingUser.getAvatarUrl());
            } catch (IOException e) {
                log.error("Failed to delete old user avatar: {}", e.getMessage());
            }

            String imageUrl = cloudinaryService.uploadFile(file, folderName);
            existingUser.setAvatarUrl(imageUrl);
            userRepository.save(existingUser);

            return imageUrl;
        }
    }


    @Override
    @Transactional
    public void uploadCertificates(MultipartFile[] files) throws Exception {
    }


    @Transactional
    @Override
    public void delete(String id) {
        userRepository.delete(findById(UUID.fromString(id)));
    }

    @Override
    public void activeteUser(String id) {
        if (!userRepository.existsById(UUID.fromString(id))) {
            throw new NotFoundException(messageSourceService.get("not_found_with_param",
                    new String[]{messageSourceService.get("user")}));
        }
        User user = findById(UUID.fromString(id));
        if (user.getStatus() == Constants.StatusProfileEnum.ACTIVE) {
            user.setStatus(Constants.StatusProfileEnum.ACTIVE);
        } else {
            user.setStatus(Constants.StatusProfileEnum.INACTIVE);
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activateUserByEmail(String email) {
        User user = findByEmail(email);
        user.setIsActive(true);
        userRepository.save(user);
        log.info("User activated by email verification: {}", email);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = findByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset successfully for email: {}", email);
    }

    @Override
    public User updateUserRole(UUID id, UpdateUserRoleRequest request) throws BindException {
        User user = findById(id);

        try {
            Constants.RoleEnum newRole = Constants.RoleEnum.get(request.getRole().getValue());
            Constants.RoleEnum currentRole = user.getRole();

            // Validation: prevent role transitions that don't make sense
            if (!isValidRoleTransition(currentRole, newRole)) {
                BindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
                bindingResult.addError(new FieldError(bindingResult.getObjectName(), "role",
                        "Invalid role transition from " + currentRole + " to " + newRole));
                throw new BindException(bindingResult);
            }

            // Handle role-specific logic
            switch (newRole) {
                case SEER, UNVERIFIED_SEER -> {
                    // When upgrading to Seer, create SeerProfile if it doesn't exist
                    if (user.getSeerProfile() == null) {
                        SeerProfile seerProfile = new SeerProfile();
                        seerProfile.setUser(user);
                        seerProfile.setAvgRating(0.0);
                        seerProfile.setTotalRates(0);
                        user.setSeerProfile(seerProfile);
                    }
                    // New seers start as UNVERIFIED
                    user.setStatus(Constants.StatusProfileEnum.UNVERIFIED);
                }
                case CUSTOMER -> {
                    // When downgrading to Customer, clear Seer profile
                    user.setSeerProfile(null);
                    user.setStatus(Constants.StatusProfileEnum.VERIFIED);
                }
                case ADMIN -> {
                    user.setStatus(Constants.StatusProfileEnum.ACTIVE);
                }
                case GUEST -> {
                    user.setStatus(Constants.StatusProfileEnum.INACTIVE);
                }
                default -> {}
            }

            user.setRole(newRole);
            return userRepository.save(user);
        }
        catch (IllegalArgumentException e) {
            BindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
            bindingResult.addError(new FieldError(bindingResult.getObjectName(), "role",
                    messageSourceService.get("invalid_role")));
            throw new BindException(bindingResult);
        }
    }

    private boolean isValidRoleTransition(Constants.RoleEnum currentRole, Constants.RoleEnum newRole){
        // Allow transition to same role
        if (currentRole == newRole) {
            return true;
        }

        // Define invalid transitions
        return switch (currentRole) {
            case GUEST -> !newRole.equals(Constants.RoleEnum.ADMIN); // Guest can't go directly to Admin
            case CUSTOMER -> !newRole.equals(Constants.RoleEnum.GUEST); // Can't downgrade to Guest
            case SEER -> !newRole.equals(Constants.RoleEnum.GUEST); // Verified Seer can't go to Guest
            case UNVERIFIED_SEER -> true; // Unverified Seer can transition to any role
            case ADMIN -> true; // Admin can change to any role
        };
    }

    @Override
    public AccountStatsResponse getAccountStats() {
        long customerAccounts = userRepository.countByRole(Constants.RoleEnum.CUSTOMER);
        long seerAccounts = userRepository.countByRole(Constants.RoleEnum.SEER);
        long adminAccounts = userRepository.countByRole(Constants.RoleEnum.ADMIN);
        long pendingAccounts = userRepository.countPendingSeers();
        long blockedAccounts = userRepository.countBlockedUsers();

        // Tổng số account không bao gồm guest
        long totalAccounts = customerAccounts + seerAccounts + adminAccounts + pendingAccounts;

        return AccountStatsResponse.builder()
                .totalAccounts(totalAccounts)
                .customerAccounts(customerAccounts)
                .seerAccounts(seerAccounts)
                .adminAccounts(adminAccounts)
                .pendingAccounts(pendingAccounts)
                .blockedAccounts(blockedAccounts)
                .build();
    }

    @Override
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                keyword.trim(), keyword.trim(), pageable);
    }

    @Override
    public Page<User> findAllWithFilters(String role, String status, Pageable pageable) {
        if (role == null && status == null) {
            return userRepository.findAll(pageable);
        }

        if (role != null && status != null) {
            try {
                Constants.RoleEnum roleEnum = Constants.RoleEnum.valueOf(role.toUpperCase());
                Constants.StatusProfileEnum statusEnum = Constants.StatusProfileEnum.valueOf(status.toUpperCase());
                return userRepository.findByRoleAndStatus(roleEnum, statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                return userRepository.findAll(pageable);
            }
        }

        if (role != null) {
            try {
                Constants.RoleEnum roleEnum = Constants.RoleEnum.valueOf(role.toUpperCase());
                return userRepository.findByRole(roleEnum, pageable);
            } catch (IllegalArgumentException e) {
                return userRepository.findAll(pageable);
            }
        }

        if (status != null) {
            try {
                Constants.StatusProfileEnum statusEnum = Constants.StatusProfileEnum.valueOf(status.toUpperCase());
                return userRepository.findByStatus(statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                return userRepository.findAll(pageable);
            }
        }

        return userRepository.findAll(pageable);
    }

    @Override
    public User getUserWithSeerStats(UUID userId) {
        User user = findById(userId);

        // Chỉ thêm thống kê nếu user là seer
        if (user.getRole() == Constants.RoleEnum.SEER && user.getSeerProfile() != null) {
            // Lấy thống kê booking cho seer
            Long totalBookings = bookingRepository.countBySeer(user);
            Long completedBookings = bookingRepository.countBySeerAndStatus(user, Constants.BookingStatusEnum.COMPLETED);
            Double totalRevenue = bookingRepository.getTotalRevenueBySeer(user, Constants.PaymentStatusEnum.COMPLETED);

        }

        return user;
    }
}
