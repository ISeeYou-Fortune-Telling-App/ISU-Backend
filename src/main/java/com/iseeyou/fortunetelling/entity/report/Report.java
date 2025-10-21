package com.iseeyou.fortunetelling.entity.report;

import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.util.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="report")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "report_id", nullable = false)),
})
public class Report extends AbstractBaseEntity {
    @Column(name = "target_type", nullable = false, length = 50)
    private Constants.TargetReportTypeEnum targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "report_description", length = 1000)
    private String reportDescription;

    @Column(name = "status", nullable = false, length = 20)
    private Constants.ReportStatusEnum status;

    @Column(name = "action_type", nullable = false, length = 50)
    private Constants.ReportActionEnum actionType;

    @Column(name = "note", length = 1000)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_type_id", nullable = false)
    private ReportType reportType;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReportEvidence> reportEvidences = new ArrayList<>();
}
