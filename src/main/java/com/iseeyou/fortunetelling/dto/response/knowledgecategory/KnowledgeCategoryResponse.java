package com.iseeyou.fortunetelling.dto.response.knowledgecategory;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class KnowledgeCategoryResponse extends AbstractBaseDataResponse {
    private String name;
    private String description;
}
