package com.iseeyou.fortunetelling.repository.servicepackage;

import com.iseeyou.fortunetelling.entity.servicepackage.PackageCategory;
import com.iseeyou.fortunetelling.dto.response.statistics.CategoryPackageStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PackageCategoryRepository extends JpaRepository<PackageCategory, UUID>, JpaSpecificationExecutor<PackageCategory> {

    @Modifying
    @Query("DELETE FROM PackageCategory pc WHERE pc.servicePackage.id = :packageId AND pc.knowledgeCategory.id IN :categoryIds")
    void deleteAllByServicePackage_IdAndKnowledgeCategory_IdIn(@Param("packageId") UUID packageId, @Param("categoryIds") Set<UUID> categoryIds);

    Optional<PackageCategory> findByServicePackage_IdAndKnowledgeCategory_Id(UUID packageId, UUID categoryId);

    // Statistics - Get package distribution by knowledge category
    @Query("SELECT new com.iseeyou.fortunetelling.dto.response.statistics.CategoryPackageStatistics(" +
           "kc.name, COUNT(DISTINCT pc.servicePackage.id)) " +
           "FROM PackageCategory pc " +
           "JOIN pc.knowledgeCategory kc " +
           "JOIN pc.servicePackage sp " +
           "WHERE sp.deletedAt IS NULL " +
           "GROUP BY kc.id, kc.name " +
           "ORDER BY kc.name")
    List<CategoryPackageStatistics> getPackageDistributionByCategory();
}
