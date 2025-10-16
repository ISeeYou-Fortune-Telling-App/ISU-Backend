package com.iseeyou.fortunetelling.dto.response.report;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ReportTypeResponse extends AbstractBaseDataResponse {
    private String name;
    private String description;
}
