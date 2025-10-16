package com.iseeyou.fortunetelling.entity.knowledge;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import com.iseeyou.fortunetelling.entity.certificate.CertificateCategory;
import com.iseeyou.fortunetelling.entity.servicepackage.PackageCategory;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="knowledge_category")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "category_id", nullable = false)),
})
public class KnowledgeCategory extends AbstractBaseEntity {
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @OneToMany(mappedBy = "knowledgeCategory", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CertificateCategory> certificateCategories;

    @OneToMany(mappedBy = "knowledgeCategory", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ItemCategory> itemCategories = new HashSet<>();

    @OneToMany(mappedBy = "knowledgeCategory", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PackageCategory> packageCategories = new HashSet<>();
}
