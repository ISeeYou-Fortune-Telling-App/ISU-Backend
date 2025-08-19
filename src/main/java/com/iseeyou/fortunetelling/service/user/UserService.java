package com.iseeyou.fortunetelling.service.user;

import com.iseeyou.fortunetelling.dto.request.auth.RegisterRequest;
import com.iseeyou.fortunetelling.dto.request.user.UpdateUserRequest;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.security.JwtUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService {
    User getUser();

    UserDetails loadUserById(String id);

    JwtUserDetails getPrincipal(Authentication authentication);

    Page<User> findAll(Pageable pageable);

    User findById(UUID id);

    User findByEmail(String email);

    UserDetails loadUserByEmail(String email);

    User register(RegisterRequest request) throws BindException;

    User updateMe(UpdateUserRequest request) throws BindException;

    String uploadImage(MultipartFile file, String folderName) throws Exception;
    void uploadCertificates(MultipartFile[] files) throws Exception;

    void delete(String id);

    void activeteUser(String id);
}