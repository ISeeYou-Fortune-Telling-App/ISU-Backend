package com.iseeyou.fortunetelling.entity.knowledge;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="knowledge_item")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "item_id", nullable = false)),
})
public class KnowledgeItem extends AbstractBaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "status")
    private Constants.KnowledgeItemStatusEnum status;

    @OneToMany(mappedBy = "knowledgeItem", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ItemCategory> itemCategories = new HashSet<>();
}
