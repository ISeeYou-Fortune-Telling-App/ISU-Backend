package com.iseeyou.fortunetelling.dto.response.knowledgecategory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class KnowledgeCategoryResponse {
    private String id;
    private String name;
    private String description;
    private String createdAt;
    private String updatedAt;
}
