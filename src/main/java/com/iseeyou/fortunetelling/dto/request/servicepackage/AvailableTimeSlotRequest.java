package com.iseeyou.fortunetelling.dto.request.servicepackage;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableTimeSlotRequest {

    @NotNull(message = "Thứ trong tuần không được để trống")
    @Min(value = 2, message = "Thứ phải từ 2 đến 8 (2=Thứ 2, 8=Chủ nhật)")
    @Max(value = 8, message = "Thứ phải từ 2 đến 8 (2=Thứ 2, 8=Chủ nhật)")
    private Integer weekDate; // 2 = Thứ 2, 3 = Thứ 3, ..., 8 = Chủ nhật

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalTime availableFrom;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    private LocalTime availableTo;
}
