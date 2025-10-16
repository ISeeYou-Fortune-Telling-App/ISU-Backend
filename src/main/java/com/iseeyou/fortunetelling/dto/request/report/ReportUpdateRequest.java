package com.iseeyou.fortunetelling.dto.request.report;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ReportUpdateRequest {
    private UUID reportId;
    private Constants.ReportActionEnum actionType;
    private String note;
}
