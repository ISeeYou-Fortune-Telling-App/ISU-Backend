package com.iseeyou.fortunetelling.service.knowledgeitem.impl;

import com.iseeyou.fortunetelling.entity.knowledge.ItemCategory;
import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeCategory;
import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeItem;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.knowledge.ItemCategoryRepository;
import com.iseeyou.fortunetelling.repository.knowledge.KnowledgeItemRepository;
import com.iseeyou.fortunetelling.service.fileupload.CloudinaryService;
import com.iseeyou.fortunetelling.service.knowledgecategory.KnowledgeCategoryService;
import com.iseeyou.fortunetelling.service.knowledgeitem.KnowledgeItemService;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KnowledgeItemServiceImpl implements KnowledgeItemService {

    private final ItemCategoryRepository itemCategoryRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final KnowledgeCategoryService knowledgeCategoryService;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public KnowledgeItem findById(UUID id) {
        KnowledgeItem item = knowledgeItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("knowledgeItem not found"));
        // Force load itemCategories and their knowledgeCategory to avoid LazyInitializationException
        Hibernate.initialize(item.getItemCategories());
        item.getItemCategories().forEach(ic -> {
            Hibernate.initialize(ic.getKnowledgeCategory());
        });
        return item;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KnowledgeItem> findAll(Pageable pageable) {
        return knowledgeItemRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KnowledgeItem> findAllByStatus(Constants.KnowledgeItemStatusEnum status, Pageable pageable) {
        return knowledgeItemRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KnowledgeItem> findAllByKnowledgeCategoryId(UUID knowledgeCategoryId, Pageable pageable) {
        return knowledgeItemRepository.findAllByKnowledgeCategory_Id(knowledgeCategoryId, pageable);
    }

    @Override
    @Transactional
    public KnowledgeItem create(KnowledgeItem knowledgeItem, Set<UUID> categoryIds) {

        knowledgeItem.setViewCount(0L);

        KnowledgeItem newKnowledgeItem = knowledgeItemRepository.save(knowledgeItem);

        Set<ItemCategory> itemCategories = new HashSet<>();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (UUID categoryId : categoryIds) {
                KnowledgeCategory knowledgeCategory = knowledgeCategoryService.findById(categoryId);
                ItemCategory itemCategory = ItemCategory.builder()
                        .knowledgeCategory(knowledgeCategory)
                        .knowledgeItem(newKnowledgeItem)
                        .build();
                itemCategories.add(itemCategory);
            }
        }

        itemCategoryRepository.saveAll(itemCategories);

        newKnowledgeItem.setItemCategories(itemCategories);
        return newKnowledgeItem;
    }

    @Override
    @Transactional
    public KnowledgeItem update(UUID itemId, KnowledgeItem knowledgeItem, Set<UUID> categoryIds) throws IOException {
        KnowledgeItem existingKnowledgeItem = knowledgeItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("knowledgeItem not found"));

        existingKnowledgeItem.setTitle(knowledgeItem.getTitle());
        existingKnowledgeItem.setContent(knowledgeItem.getContent());
        existingKnowledgeItem.setStatus(knowledgeItem.getStatus());
        if (existingKnowledgeItem.getImageUrl() != null) {
            cloudinaryService.deleteFile(existingKnowledgeItem.getImageUrl());
            existingKnowledgeItem.setImageUrl(knowledgeItem.getImageUrl());
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            Set<UUID> existingCategoryIds = existingKnowledgeItem.getItemCategories().stream()
                    .map(ic -> ic.getKnowledgeCategory().getId())
                    .collect(Collectors.toSet());

            // Categories to remove
            Set<UUID> categoriesToRemove = existingCategoryIds.stream()
                    .filter(id -> !categoryIds.contains(id))
                    .collect(Collectors.toSet());

            // Categories to add
            Set<UUID> categoriesToAdd = categoryIds.stream()
                    .filter(id -> !existingCategoryIds.contains(id))
                    .collect(Collectors.toSet());

            // Remove old relationships
            if (!categoriesToRemove.isEmpty()) {
                itemCategoryRepository.deleteAllByKnowledgeItem_IdAndKnowledgeCategory_IdIn(
                        existingKnowledgeItem.getId(), categoriesToRemove);
                existingKnowledgeItem.getItemCategories().removeIf(
                        ic -> categoriesToRemove.contains(ic.getKnowledgeCategory().getId()));
            }

            // Add new relationships
            if (!categoriesToAdd.isEmpty()) {
                List<KnowledgeCategory> newCategories = knowledgeCategoryService.findAllByIds(categoriesToAdd);

                Set<ItemCategory> newRelationships = new HashSet<>();
                for (KnowledgeCategory category : newCategories) {
                    ItemCategory itemCategory = ItemCategory.builder()
                            .knowledgeItem(existingKnowledgeItem)
                            .knowledgeCategory(category)
                            .build();
                    newRelationships.add(itemCategory);
                }

                itemCategoryRepository.saveAll(newRelationships);
                existingKnowledgeItem.getItemCategories().addAll(newRelationships);
            }
        }

        return existingKnowledgeItem;
    }

    @Override
    @Transactional
    public void delete(UUID id) throws IOException {
        KnowledgeItem knowledgeItem = knowledgeItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("knowledgeItem not found"));
        if (knowledgeItem.getImageUrl() != null) {
            cloudinaryService.deleteFile(knowledgeItem.getImageUrl());
            knowledgeItem.setImageUrl(knowledgeItem.getImageUrl());
        }
        knowledgeItemRepository.delete(knowledgeItem);
    }

    @Override
    @Transactional
    public void view(UUID id) {
        KnowledgeItem knowledgeItem = knowledgeItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("knowledgeItem not found"));
        knowledgeItem.setViewCount(knowledgeItem.getViewCount() + 1);
        knowledgeItemRepository.save(knowledgeItem);
    }

    @Override
    @Transactional
    public Page<KnowledgeItem> search(String title, List<UUID> categoryIds, Constants.KnowledgeItemStatusEnum status, Pageable pageable) {
        // Convert empty list to null to avoid empty IN clause
        List<UUID> effectiveCategoryIds = (categoryIds != null && categoryIds.isEmpty()) ? null : categoryIds;
        return knowledgeItemRepository.search(title, effectiveCategoryIds, status, pageable);
    }

    @Transactional(readOnly = true)
    public KnowledgeItemStats getAllItemsStats() {
        long published = knowledgeItemRepository.countByStatus(Constants.KnowledgeItemStatusEnum.PUBLISHED);
        long draft = knowledgeItemRepository.countByStatus(Constants.KnowledgeItemStatusEnum.DRAFT);
        long hidden = knowledgeItemRepository.countByStatus(Constants.KnowledgeItemStatusEnum.HIDDEN);
        Long totalViewCount = knowledgeItemRepository.getTotalViewCount();
        
        return KnowledgeItemStats.builder()
                .publishedItems(published)
                .draftItems(draft)
                .hiddenItems(hidden)
                .totalViewCount(totalViewCount != null ? totalViewCount : 0L)
                .build();
    }

    @lombok.Getter
    @lombok.Builder
    @lombok.AllArgsConstructor
    public static class KnowledgeItemStats {
        private Long publishedItems;
        private Long draftItems;
        private Long hiddenItems;
        private Long totalViewCount;
    }
}
