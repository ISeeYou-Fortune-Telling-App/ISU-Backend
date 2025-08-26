package com.iseeyou.fortunetelling.dto.response.knowledgeitem;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class KnowledgeItemResponse extends AbstractBaseDataResponse {
    private String title;
    private String content;
    private List<String> categories;
    private Constants.KnowledgeItemStatusEnum status;
    private String imageUrl;
    private Long viewCount;
}
