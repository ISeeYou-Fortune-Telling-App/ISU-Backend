package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.knowledgeitem.KnowledgeItemResponse;
import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeItem;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KnowledgeItemMapper extends BaseMapper {

    @Autowired
    public KnowledgeItemMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        modelMapper.typeMap(KnowledgeItem.class, KnowledgeItemResponse.class)
                .setPostConverter(context -> {
                    KnowledgeItem source = context.getSource();
                    KnowledgeItemResponse destination = context.getDestination();

                    if (source.getItemCategories() != null) {
                        List<String> categoryNames = source.getItemCategories()
                                .stream()
                                .map(ic -> ic.getKnowledgeCategory().getName())
                                .collect(Collectors.toList());
                        destination.setCategories(categoryNames);
                    } else {
                        destination.setCategories(List.of());
                    }

                    return destination;
                });
    }
}
