package com.iseeyou.fortunetelling.dto.request.knowledgeitem;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class KnowledgeItemUpdateRequest {
    private UUID id;
    private String title;
    private String content;
    private String source;
    private Set<UUID> categoryIds;
    private Constants.KnowledgeItemStatusEnum status;
    private MultipartFile imageFile;
}
