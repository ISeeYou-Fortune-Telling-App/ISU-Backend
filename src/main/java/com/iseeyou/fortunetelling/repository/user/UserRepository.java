package com.iseeyou.fortunetelling.repository.user;

import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Statistics methods
    long countByRole(Constants.RoleEnum role);

    long countByStatus(Constants.StatusProfileEnum status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRoleWithParam(@Param("role") Constants.RoleEnum role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    long countByStatusWithParam(@Param("status") Constants.StatusProfileEnum status);

    default long countPendingSeers() {
        return countByRoleWithParam(Constants.RoleEnum.UNVERIFIED_SEER);
    }

    default long countBlockedUsers() {
        return countByStatusWithParam(Constants.StatusProfileEnum.BLOCKED);
    }

    // Search method for admin
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            @Param("keyword") String fullName,
            @Param("keyword") String email,
            Pageable pageable);

    // Filter methods
    Page<User> findByRole(Constants.RoleEnum role, Pageable pageable);

    Page<User> findByStatus(Constants.StatusProfileEnum status, Pageable pageable);

    Page<User> findByRoleAndStatus(Constants.RoleEnum role, Constants.StatusProfileEnum status, Pageable pageable);

    // Combined filter and search method
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> findAllWithFilters(@Param("keyword") String keyword,
                                   @Param("role") Constants.RoleEnum role,
                                   @Param("status") Constants.StatusProfileEnum status,
                                   Pageable pageable);

    @Query("""
        SELECT DISTINCT u FROM User u
        LEFT JOIN u.seerProfile sp
        LEFT JOIN u.seerSpecialities ss
        WHERE u.role = :role
        AND (:searchText IS NULL OR :searchText = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchText, '%')))
        AND (:seerSpecialityIds IS NULL OR ss.knowledgeCategory.id IN :seerSpecialityIds)
        """)
    Page<User> findSeersWithFilters(@Param("role") Constants.RoleEnum role,
                                     @Param("searchText") String searchText,
                                     @Param("seerSpecialityIds") List<UUID> seerSpecialityIds,
                                     Pageable pageable);

    long countAllByStatus(Constants.StatusProfileEnum status);
}
