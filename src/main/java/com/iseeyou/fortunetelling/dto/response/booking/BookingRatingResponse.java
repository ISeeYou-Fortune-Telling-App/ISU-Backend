package com.iseeyou.fortunetelling.dto.response.booking;

import com.iseeyou.fortunetelling.dto.response.AbstractBaseDataResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class BookingRatingResponse extends AbstractBaseDataResponse {
    private String comment;
    private Integer rating;
}
