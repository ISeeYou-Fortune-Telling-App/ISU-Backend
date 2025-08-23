package com.iseeyou.fortunetelling.service.knowledgeitem;

import com.iseeyou.fortunetelling.entity.KnowledgeItem;
import com.iseeyou.fortunetelling.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public interface KnowledgeItemService {
    KnowledgeItem findById(UUID id);
    Page<KnowledgeItem> findAll(Pageable pageable);
    Page<KnowledgeItem> findAllByStatus(Constants.KnowledgeItemStatusEnum status, Pageable pageable);
    Page<KnowledgeItem> findAllByKnowledgeCategoryId(UUID knowledgeCategoryId, Pageable pageable);
    KnowledgeItem create(KnowledgeItem knowledgeItem, Set<UUID> categoryIds);
    KnowledgeItem update(UUID id, KnowledgeItem knowledgeItem, Set<UUID> categoryIds) throws IOException;
    void delete(UUID id) throws IOException;
    void view(UUID id);
    // TODO: Full text search by name
}
