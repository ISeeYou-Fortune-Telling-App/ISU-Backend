package com.iseeyou.fortunetelling.dto.request.servicepackage;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ServicePackageUpsertRequest {
    @NotBlank(message = "Package title is required")
    private String packageTitle;
    
    private String packageContent;
    
    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer durationMinutes;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;
    
    @NotEmpty(message = "At least one category is required")
    private List<String> categoryIds; // Danh sách ID của các category

    private MultipartFile image; // file ảnh minh họa - optional

    // Commission rate (optional) - chỉ admin mới có thể cập nhật
    @DecimalMin(value = "0.0", message = "Commission rate must be greater than or equal to 0")
    @DecimalMax(value = "1.0", message = "Commission rate must be less than or equal to 1 (100%)")
    private Double commissionRate;

    // Thời gian rảnh của service package
    @Valid
    private List<AvailableTimeSlotRequest> availableTimeSlots;
}
