package com.iseeyou.fortunetelling.repository.servicepackage;

import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, UUID>, JpaSpecificationExecutor<ServicePackage> {

    @Override
    @EntityGraph(attributePaths = {"packageCategories.knowledgeCategory", "seer", "seer.seerProfile"})
    Page<ServicePackage> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"packageCategories.knowledgeCategory", "seer", "seer.seerProfile"})
    Page<ServicePackage> findAll(Specification<ServicePackage> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"packageCategories.knowledgeCategory", "seer", "seer.seerProfile"})
    Optional<ServicePackage> findById(UUID id);

    @EntityGraph(attributePaths = {"packageCategories.knowledgeCategory", "seer", "seer.seerProfile"})
    Page<ServicePackage> findAllBySeer_Id(UUID seerId, Pageable pageable);

    @EntityGraph(attributePaths = {"packageCategories.knowledgeCategory", "seer", "seer.seerProfile"})
    @Query("SELECT DISTINCT sp FROM ServicePackage sp " +
            "JOIN sp.packageCategories pc " +
            "WHERE sp.seer.id = :seerId AND pc.knowledgeCategory.id = :categoryId")
    Page<ServicePackage> findBySeerIdAndCategoryId(@Param("seerId") UUID seerId,
                                                   @Param("categoryId") UUID categoryId,
                                                   Pageable pageable);

    @EntityGraph(attributePaths = {"packageCategories.knowledgeCategory", "seer", "seer.seerProfile"})
    @Query("SELECT DISTINCT sp FROM ServicePackage sp " +
            "JOIN sp.packageCategories pc " +
            "WHERE pc.knowledgeCategory.id = :categoryId")
    Page<ServicePackage> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

    @Query("""
        SELECT DISTINCT sp FROM ServicePackage sp
        LEFT JOIN sp.packageCategories pc
        LEFT JOIN sp.seer s
        LEFT JOIN s.seerSpecialities ss
        WHERE (:status IS NULL OR sp.status = :status)
        AND (:minPrice IS NULL OR sp.price >= :minPrice)
        AND (:searchText IS NULL OR :searchText = '' OR LOWER(sp.packageTitle) LIKE LOWER(CONCAT('%', :searchText, '%')))
        AND (:maxPrice IS NULL OR sp.price <= :maxPrice)
        AND (:minTime IS NULL OR sp.durationMinutes >= :minTime)
        AND (:maxTime IS NULL OR sp.durationMinutes <= :maxTime)
        AND (:packageCategoryIds IS NULL OR pc.knowledgeCategory.id IN :packageCategoryIds)
        AND (:seerSpecialityIds IS NULL OR ss.knowledgeCategory.id IN :seerSpecialityIds)
        """)
    @EntityGraph(attributePaths = {"packageCategories.knowledgeCategory", "seer", "seer.seerProfile"})
    Page<ServicePackage> findAllWithFilters(@Param("status") com.iseeyou.fortunetelling.util.Constants.PackageStatusEnum status,
                                            @Param("minPrice") Double minPrice,
                                            @Param("maxPrice") Double maxPrice,
                                            @Param("packageCategoryIds") List<UUID> packageCategoryIds,
                                            @Param("seerSpecialityIds") List<UUID> seerSpecialityIds,
                                            @Param("minTime") Integer minTime,
                                            @Param("maxTime") Integer maxTime,
                                            @Param("searchText") String searchText,
                                            Pageable pageable);

    @EntityGraph(attributePaths = {"packageCategories.knowledgeCategory", "seer", "seer.seerProfile"})
    @Query("SELECT DISTINCT sp FROM ServicePackage sp " +
            "JOIN sp.packageCategories pc " +
            "WHERE pc.knowledgeCategory.id IN :categoryIds")
    Page<ServicePackage> findByCategoryIds(@Param("categoryIds") List<UUID> categoryIds, Pageable pageable);
}
