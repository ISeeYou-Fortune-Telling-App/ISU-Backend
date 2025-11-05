package com.iseeyou.fortunetelling.entity.servicepackage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iseeyou.fortunetelling.entity.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "package_available_time")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "time_id", nullable = false)),
})
public class PackageAvailableTime extends AbstractBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    @JsonIgnore
    private ServicePackage servicePackage;

    @Column(name = "week_date", nullable = false)
    private Integer weekDate; // 2 = Thứ 2, 3 = Thứ 3, ..., 8 = Chủ nhật

    @Column(name = "available_from", nullable = false)
    private LocalTime availableFrom;

    @Column(name = "available_to", nullable = false)
    private LocalTime availableTo;
}

