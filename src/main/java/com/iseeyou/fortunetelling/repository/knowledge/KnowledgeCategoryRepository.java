package com.iseeyou.fortunetelling.repository.knowledge;

import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface KnowledgeCategoryRepository extends JpaRepository<KnowledgeCategory, UUID>, JpaSpecificationExecutor<KnowledgeCategory> {
    Optional<KnowledgeCategory> findByName(String name);
}
