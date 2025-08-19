package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.entity.KnowledgeCategory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeCategoryMapper extends BaseMapper<KnowledgeCategory> {
    @Autowired
    public KnowledgeCategoryMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {

    }
}
