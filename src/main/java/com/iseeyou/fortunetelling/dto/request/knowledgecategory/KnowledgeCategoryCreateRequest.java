package com.iseeyou.fortunetelling.dto.request.knowledgecategory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class KnowledgeCategoryCreateRequest {
    private String name;
    private String description;
}
