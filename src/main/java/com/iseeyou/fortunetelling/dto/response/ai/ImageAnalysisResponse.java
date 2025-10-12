package com.iseeyou.fortunetelling.dto.response.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageAnalysisResponse {

    private String analysisResult;
    private String analysisType; // "palm" or "face"
    private Double processingTime;
    private LocalDateTime timestamp;
}
