package com.iseeyou.fortunetelling.repository;

import com.iseeyou.fortunetelling.entity.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, UUID>, JpaSpecificationExecutor<ItemCategory> {
    @Modifying
    @Transactional
    @Query("DELETE FROM ItemCategory ic WHERE ic.knowledgeItem.id = :itemId AND ic.knowledgeCategory.id IN :categoryIds")
    void deleteAllByKnowledgeItem_IdAndKnowledgeCategory_IdIn(@Param("itemId") UUID itemId, @Param("categoryIds") Set<UUID> categoryIds);
}