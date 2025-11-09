package com.iseeyou.fortunetelling.service.user;

import com.iseeyou.fortunetelling.dto.request.auth.RegisterRequest;
import com.iseeyou.fortunetelling.dto.request.auth.SeerRegisterRequest;
import com.iseeyou.fortunetelling.dto.request.user.ApproveSeerRequest;
import com.iseeyou.fortunetelling.dto.request.user.UpdatePaypalEmailRequest;
import com.iseeyou.fortunetelling.dto.request.user.UpdateUserRequest;
import com.iseeyou.fortunetelling.dto.request.user.UpdateUserRoleRequest;
import com.iseeyou.fortunetelling.dto.response.account.AccountStatsResponse;
import com.iseeyou.fortunetelling.dto.response.account.SimpleSeerCardResponse;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.security.JwtUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User getUser();

    UserDetails loadUserById(String id);

    JwtUserDetails getPrincipal(Authentication authentication);

    Page<User> findAll(Pageable pageable);

    Page<SimpleSeerCardResponse> getSimpleSeerCardsWithFilter(Pageable pageable,
                                                              String searchText,
                                                              List<UUID> seerSpecialityIds);

    User findById(UUID id);

    User updateStatus(UUID id, String status);

    User findByEmail(String email);

    UserDetails loadUserByEmail(String email);

    User register(RegisterRequest request) throws BindException;

    User seerRegister(SeerRegisterRequest request) throws BindException;

    User updateMe(UpdateUserRequest request) throws BindException;

    String uploadImage(MultipartFile file, String folderName) throws Exception;
    void uploadCertificates(MultipartFile[] files) throws Exception;

    void setFcmTokenByEmail(String email, String fcmToken );

    void delete(String id);

    void activateUserByEmail(String email);

    void resetPassword(String email, String newPassword);

    Page<User> findAllWithFilters(String keyword, String role, String status, Pageable pageable);

    User updateUserRole(UUID id, UpdateUserRoleRequest request) throws BindException;

    AccountStatsResponse getAccountStats();

    // Lấy user với thống kê seer (nếu là seer)
    User getUserWithSeerStats(UUID userId);

    // Seer: Update PayPal email

    // Admin: Approve or reject seer
    User approveSeer(UUID userId, ApproveSeerRequest request);
    User updatePaypalEmail(UpdatePaypalEmailRequest request);
}