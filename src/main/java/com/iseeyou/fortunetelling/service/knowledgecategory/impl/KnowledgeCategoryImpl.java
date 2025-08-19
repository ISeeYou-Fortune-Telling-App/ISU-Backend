package com.iseeyou.fortunetelling.service.knowledgecategory.impl;

import com.iseeyou.fortunetelling.entity.KnowledgeCategory;
import com.iseeyou.fortunetelling.exception.NotFoundException;
import com.iseeyou.fortunetelling.repository.KnowledgeCategoryRepository;
import com.iseeyou.fortunetelling.service.knowledgecategory.KnowledgeCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeCategoryImpl implements KnowledgeCategoryService {

    private final KnowledgeCategoryRepository knowledgeCategoryRepository;

    @Override
    @Transactional
    public Page<KnowledgeCategory> findAll(Pageable pageable) {
        return knowledgeCategoryRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeCategory findById(UUID id) {
        return knowledgeCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("KnowledgeCategory not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeCategory findByName(String name) {
        return knowledgeCategoryRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("KnowledgeCategory not found with id: " + name));
    }

    @Override
    @Transactional
    public KnowledgeCategory create(KnowledgeCategory knowledgeCategory) {
        if (knowledgeCategoryRepository.findByName(knowledgeCategory.getName()).isPresent()) {
            throw new IllegalArgumentException("KnowledgeCategory with name '" + knowledgeCategory.getName() + "' already exists.");
        }
        return knowledgeCategoryRepository.save(knowledgeCategory);
    }

    @Override
    @Transactional
    public KnowledgeCategory update(UUID id, KnowledgeCategory knowledgeCategory) {
        KnowledgeCategory existingCategory = knowledgeCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("KnowledgeCategory not found with id: " + id));
        try {
            existingCategory.setName(knowledgeCategory.getName());
            existingCategory.setDescription(knowledgeCategory.getDescription());
            return knowledgeCategoryRepository.save(existingCategory);
        } catch (Exception e) {
            throw new IllegalArgumentException("The name '" + knowledgeCategory.getName() + "' is already in use.");
        }
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (!knowledgeCategoryRepository.existsById(UUID.fromString(id))) {
            throw new NotFoundException("KnowledgeCategory not found with id: " + id);
        }
        knowledgeCategoryRepository.deleteById(UUID.fromString(id));
    }
}
