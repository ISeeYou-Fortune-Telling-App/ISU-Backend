package com.iseeyou.fortunetelling.dto.response.servicepackage;

import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableTimeSlotResponse {
    private Integer weekDate; // 2 = Thứ 2, 3 = Thứ 3, ..., 8 = Chủ nhật
    private String weekDayName; // Tên thứ bằng tiếng Việt
    private LocalTime availableFrom;
    private LocalTime availableTo;
}

