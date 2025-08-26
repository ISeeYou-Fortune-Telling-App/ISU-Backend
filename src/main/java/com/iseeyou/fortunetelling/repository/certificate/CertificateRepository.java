package com.iseeyou.fortunetelling.repository.certificate;

import com.iseeyou.fortunetelling.entity.certificate.Certificate;
import com.iseeyou.fortunetelling.entity.certificate.CertificateCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, UUID>, JpaSpecificationExecutor<Certificate> {
    @EntityGraph(attributePaths = {"certificateCategories.knowledgeCategory", "seer"})
    Page<Certificate> findAllBySeer_Id(UUID seerId, Pageable pageable);

    Set<Certificate> findAllByCertificateCategories(Set<CertificateCategory> certificateCategories);

    @Override
    @EntityGraph(attributePaths = {"certificateCategories.knowledgeCategory", "seer"})
    Page<Certificate> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"certificateCategories.knowledgeCategory", "seer"})
    @Query("SELECT c FROM Certificate c JOIN c.certificateCategories cc WHERE c.seer.id = :seerId AND cc.knowledgeCategory.id = :categoryId")
    Page<Certificate> findBySeerIdAndCategoryId(@Param("seerId") UUID seerId, @Param("categoryId") UUID categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"certificateCategories.knowledgeCategory", "seer"})
    @Query("SELECT c FROM Certificate c JOIN c.certificateCategories cc WHERE cc.knowledgeCategory.id = :categoryId")
    Page<Certificate> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);
}
