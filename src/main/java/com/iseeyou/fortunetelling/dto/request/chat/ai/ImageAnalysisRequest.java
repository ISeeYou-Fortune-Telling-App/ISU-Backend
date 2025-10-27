package com.iseeyou.fortunetelling.dto.request.chat.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageAnalysisRequest {

    private MultipartFile file;

    private String analysisType; // "palm" or "face"
}
