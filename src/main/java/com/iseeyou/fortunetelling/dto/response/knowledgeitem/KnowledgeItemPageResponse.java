package com.iseeyou.fortunetelling.dto.response.knowledgeitem;

import com.iseeyou.fortunetelling.dto.response.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class KnowledgeItemPageResponse extends PageResponse<KnowledgeItemResponse> {
    private KnowledgeItemStats stats;

    @Data
    @Builder
    @AllArgsConstructor
    public static class KnowledgeItemStats {
        private Long publishedItems;
        private Long draftItems;
        private Long hiddenItems;
        private Long totalViewCount;
    }

    public KnowledgeItemPageResponse(int statusCode, String message, List<KnowledgeItemResponse> data,
                                     PageResponse.PagingResponse paging, KnowledgeItemStats stats) {
        super(statusCode, message, data, paging);
        this.stats = stats;
    }
}

