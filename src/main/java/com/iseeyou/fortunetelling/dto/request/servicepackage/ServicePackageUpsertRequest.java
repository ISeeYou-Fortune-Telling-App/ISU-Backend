package com.iseeyou.fortunetelling.dto.request.servicepackage;

import com.iseeyou.fortunetelling.util.Constants;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ServicePackageUpsertRequest {
    private String packageId; // null khi tạo mới, có giá trị khi cập nhật
    private String packageTitle;
    private String packageContent;
    private Integer durationMinutes;
    private Double price;
    private Constants.ServiceCategoryEnum category; // thêm trường category
    private MultipartFile image; // file ảnh minh họa
}
