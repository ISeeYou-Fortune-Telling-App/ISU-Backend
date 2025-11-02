package com.iseeyou.fortunetelling.repository.knowledge;

import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeItem;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeItemRepository extends JpaRepository<KnowledgeItem, UUID>, JpaSpecificationExecutor<KnowledgeItem> {
    @EntityGraph(attributePaths = {"itemCategories.knowledgeCategory"})
    Page<KnowledgeItem> findAllByStatus(Constants.KnowledgeItemStatusEnum status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"itemCategories.knowledgeCategory"})
    Page<KnowledgeItem> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"itemCategories.knowledgeCategory"})
    @Query("SELECT DISTINCT ki FROM KnowledgeItem ki JOIN ki.itemCategories ic WHERE ic.knowledgeCategory.id = :categoryId")
    Page<KnowledgeItem> findAllByKnowledgeCategory_Id(@Param("categoryId") UUID categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"itemCategories.knowledgeCategory"})
    @Query("SELECT DISTINCT knowledgeItem FROM KnowledgeItem knowledgeItem " +
            "LEFT JOIN knowledgeItem.itemCategories itemCategories " +
            "WHERE (:title IS NULL OR :title = '' OR LOWER(knowledgeItem.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:categoryIds IS NULL OR itemCategories.knowledgeCategory.id IN :categoryIds) " +
            "AND (:status IS NULL OR knowledgeItem.status = :status)")
    Page<KnowledgeItem> search(
            @Param("title") String title,
            @Param("categoryIds") List<UUID> categoryIds,
            @Param("status") Constants.KnowledgeItemStatusEnum status,
            Pageable pageable
    );

    // Admin statistics methods
    @Query("SELECT COUNT(ki) FROM KnowledgeItem ki WHERE ki.status = :status")
    long countByStatus(Constants.KnowledgeItemStatusEnum status);
    
    @Query("SELECT COALESCE(SUM(ki.viewCount), 0) FROM KnowledgeItem ki")
    Long getTotalViewCount();
}
