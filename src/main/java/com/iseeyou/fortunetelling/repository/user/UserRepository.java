package com.iseeyou.fortunetelling.repository.user;

import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    List<User> findAllByRole(Constants.RoleEnum role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.seerProfile WHERE u.id = :userId")
    Optional<User> findByIdWithSeerProfile(@Param("userId") UUID userId);
}
