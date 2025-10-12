package com.iseeyou.fortunetelling.service.ai;

import com.iseeyou.fortunetelling.dto.request.ai.ChatRequest;
import com.iseeyou.fortunetelling.dto.request.ai.ImageAnalysisRequest;
import com.iseeyou.fortunetelling.dto.response.ai.ChatResponse;
import com.iseeyou.fortunetelling.dto.response.ai.ImageAnalysisResponse;
import reactor.core.publisher.Flux;

public interface AIService {

    /**
     * Chat with AI - Normal response
     */
    ChatResponse chat(ChatRequest request);

    /**
     * Chat with AI - Streaming response
     */
    Flux<String> chatStream(ChatRequest request);

    /**
     * Analyze palm image
     */
    ImageAnalysisResponse analyzePalm(ImageAnalysisRequest request);

    /**
     * Analyze face image
     */
    ImageAnalysisResponse analyzeFace(ImageAnalysisRequest request);
}
