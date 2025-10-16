package com.iseeyou.fortunetelling.dto.request.servicepackage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ServicePackageCreateRequest {
    private String packageTitle;
    private String packageContent;
    private Integer durationMinutes;
    private Double price;
    private MultipartFile imageFile;
    private Set<UUID> categoryIds;
}