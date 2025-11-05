package com.iseeyou.fortunetelling.entity.knowledge;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="item_category")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCategory extends AbstractBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private KnowledgeItem knowledgeItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private KnowledgeCategory knowledgeCategory;
}
