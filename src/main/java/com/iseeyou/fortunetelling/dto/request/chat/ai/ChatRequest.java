package com.iseeyou.fortunetelling.dto.request.chat.ai;

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

    @Schema(description = "Option process (1: Fastest, 2: Average, 3: Most Accurate", example = "2", defaultValue = "2")
    @Builder.Default
    private Integer selectedOption = 2;

    @Schema(hidden = true)
    @Builder.Default
    private Boolean forceReindex = false;
}
