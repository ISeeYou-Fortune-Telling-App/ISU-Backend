package com.iseeyou.fortunetelling.entity.certificate;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import com.iseeyou.fortunetelling.entity.knowledge.KnowledgeCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="certificate-category")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "certificate_category_id", nullable = false)),
})
public class CertificateCategory extends AbstractBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id", nullable = false)
    private Certificate certificate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private KnowledgeCategory knowledgeCategory;
}
