package com.iseeyou.fortunetelling.service.ai;

import com.iseeyou.fortunetelling.dto.request.chat.ai.ChatRequest;
import com.iseeyou.fortunetelling.dto.request.chat.ai.ImageAnalysisRequest;
import com.iseeyou.fortunetelling.dto.response.chat.ai.ChatResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AIService {

    /**
     * Chat with AI - Normal response
     */
    ChatResponse chat(ChatRequest request);

    /**
     * Analyze palm image
     */
    ChatResponse analyzePalm(ImageAnalysisRequest request);

    /**
     * Analyze face image
     */
    ChatResponse analyzeFace(ImageAnalysisRequest request);

    /// USER
    Page<ChatResponse> myChatResponse(Pageable pageable);
}
