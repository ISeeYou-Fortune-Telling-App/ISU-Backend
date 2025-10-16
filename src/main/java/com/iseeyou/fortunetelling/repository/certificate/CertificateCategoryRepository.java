package com.iseeyou.fortunetelling.repository.certificate;

import com.iseeyou.fortunetelling.entity.certificate.CertificateCategory;
import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CertificateCategoryRepository extends JpaRepository<CertificateCategory, UUID>, JpaSpecificationExecutor<CertificateCategory> {
    @Modifying
    @Transactional
    @Query("DELETE FROM CertificateCategory cc WHERE cc.certificate.id = :certificateId AND cc.knowledgeCategory.id IN :categoryIds")
    void deleteAllByCertificate_IdAndKnowledgeCategory_IdIn(@Param("certificateId") UUID certificateId, @Param("categoryIds") Set<UUID> categoryIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM CertificateCategory cc WHERE cc.certificate.id = :certificateId AND cc.knowledgeCategory IN :categories")
    void deleteAllByCertificate_IdAndKnowledgeCategoryIn(@Param("certificateId") UUID certificateId, @Param("categories") Set<KnowledgeCategory> categories);

    Set<CertificateCategory> findAllByKnowledgeCategory_Id(UUID knowledgeCategoryId);
}
