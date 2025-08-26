package com.iseeyou.fortunetelling.repository;

import com.iseeyou.fortunetelling.entity.ServicePackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, UUID>, JpaSpecificationExecutor<ServicePackage> {

    @Override
    @EntityGraph(attributePaths = {"packageCategories.knowledgeCategory", "seer"})
    Page<ServicePackage> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"packageCategories.knowledgeCategory", "seer"})
    Optional<ServicePackage> findById(UUID id);

    @EntityGraph(attributePaths = {"packageCategories.knowledgeCategory", "seer"})
    Page<ServicePackage> findAllBySeer_Id(UUID seerId, Pageable pageable);

    @EntityGraph(attributePaths = {"packageCategories.knowledgeCategory", "seer"})
    @Query("SELECT DISTINCT sp FROM ServicePackage sp " +
           "JOIN sp.packageCategories pc " +
           "WHERE sp.seer.id = :seerId AND pc.knowledgeCategory.id = :categoryId")
    Page<ServicePackage> findBySeerIdAndCategoryId(@Param("seerId") UUID seerId,
                                                   @Param("categoryId") UUID categoryId,
                                                   Pageable pageable);

    @EntityGraph(attributePaths = {"packageCategories.knowledgeCategory", "seer"})
    @Query("SELECT DISTINCT sp FROM ServicePackage sp " +
           "JOIN sp.packageCategories pc " +
           "WHERE pc.knowledgeCategory.id = :categoryId")
    Page<ServicePackage> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);
}
