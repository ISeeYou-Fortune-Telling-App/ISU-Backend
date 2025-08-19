package com.iseeyou.fortunetelling.service.user.impl;

import com.iseeyou.fortunetelling.dto.request.auth.RegisterRequest;
import com.iseeyou.fortunetelling.dto.request.user.UpdateUserRequest;
import com.iseeyou.fortunetelling.entity.user.CustomerProfile;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.UserRepository;
import com.iseeyou.fortunetelling.security.JwtUserDetails;
import com.iseeyou.fortunetelling.service.MessageSourceService;
import com.iseeyou.fortunetelling.service.fileupload.CloudinaryService;
import com.iseeyou.fortunetelling.service.user.UserService;
import com.iseeyou.fortunetelling.util.CalculateZodiac;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final MessageSourceService messageSourceService;

    private final CloudinaryService cloudinaryService;

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

        return user;
    }

    @Override
    @Transactional
    public User updateMe(UpdateUserRequest request) throws BindException {
        User user = getUser();
        user.setBirthDate(request.getBirthDate());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        user.setFullName(request.getFullName());
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
}
