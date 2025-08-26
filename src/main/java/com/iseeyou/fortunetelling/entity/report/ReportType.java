package com.iseeyou.fortunetelling.entity.report;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="report_type")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "type_id", nullable = false)),
})
public class ReportType extends AbstractBaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private Constants.ReportTypeEnum name;

    @Column(name = "description", length = 500)
    private String description;
}
