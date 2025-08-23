package com.iseeyou.fortunetelling.dto.response.knowledgeitem;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class KnowledgeItemResponse {
    private UUID id;
    private String title;
    private String content;
    private List<String> categories;
    private Constants.KnowledgeItemStatusEnum status;
    private String imageUrl;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
