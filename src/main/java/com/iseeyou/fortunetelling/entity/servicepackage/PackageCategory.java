package com.iseeyou.fortunetelling.entity.servicepackage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="package_category")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "package_category_id", nullable = false)),
})
public class PackageCategory extends AbstractBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    @JsonIgnore
    private ServicePackage servicePackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private KnowledgeCategory knowledgeCategory;
}
