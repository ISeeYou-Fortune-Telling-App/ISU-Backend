package com.iseeyou.fortunetelling.dto.request.servicepackage;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ServicePackageUpsertRequest {
    private String packageId; // null khi tạo mới, có giá trị khi cập nhật
    private String packageTitle;
    private String packageContent;
    private Integer durationMinutes;
    private Double price;
    private MultipartFile image; // file ảnh minh họa
}

