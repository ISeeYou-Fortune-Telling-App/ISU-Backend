package com.iseeyou.fortunetelling.service.knowledgecategory;

import com.iseeyou.fortunetelling.entity.KnowledgeCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface KnowledgeCategoryService {
    Page<KnowledgeCategory> findAll(Pageable pageable);
    KnowledgeCategory findById(UUID id);
    List<KnowledgeCategory> findAllByIds(Iterable<UUID> ids);
    KnowledgeCategory findByName(String name);
    KnowledgeCategory create(KnowledgeCategory knowledgeCategory);
    KnowledgeCategory update(UUID id, KnowledgeCategory knowledgeCategory);
    KnowledgeCategory save(KnowledgeCategory knowledgeCategory);
    void delete(String id);
}
