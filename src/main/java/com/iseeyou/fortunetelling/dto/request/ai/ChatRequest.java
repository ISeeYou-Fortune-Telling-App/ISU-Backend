package com.iseeyou.fortunetelling.dto.request.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @Schema(description = "Question to ask AI", example = "Tôi yêu bạn Lý, bạn Lý sinh ngày 1/1/2005, tôi sinh ngày 21/02/2005. Thử độ hợp nhau")
    private String question;

    @Schema(hidden = true)
    @Builder.Default
    private String mode = "mix"; // Just keep "mix", don't change it

    @Schema(description = "Top K (5=factual/fast, 20=balanced, 40=creative/slow)", example = "5", defaultValue = "5")
    @Builder.Default
    private Integer topK = 5; // 5=factual/fast, 20=balanced, 40=creative/slow

    @Schema(hidden = true)
    @Builder.Default
    private Boolean forceReindex = false;
}
