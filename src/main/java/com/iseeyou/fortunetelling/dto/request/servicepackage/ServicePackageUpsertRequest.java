package com.iseeyou.fortunetelling.dto.request.servicepackage;

import com.iseeyou.fortunetelling.util.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

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
}
