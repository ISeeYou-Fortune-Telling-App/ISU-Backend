package com.iseeyou.fortunetelling.service.ai.impl;

import com.iseeyou.fortunetelling.dto.request.chat.ai.ChatRequest;
import com.iseeyou.fortunetelling.dto.request.chat.ai.ImageAnalysisRequest;
import com.iseeyou.fortunetelling.dto.response.chat.ai.ChatResponse;
import com.iseeyou.fortunetelling.entity.ai.AiMessage;
import com.iseeyou.fortunetelling.mapper.SimpleMapper;
import com.iseeyou.fortunetelling.repository.ai.AiMessageRepository;
import com.iseeyou.fortunetelling.service.ai.AIService;
import com.iseeyou.fortunetelling.service.fileupload.CloudinaryService;
import com.iseeyou.fortunetelling.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServiceImpl implements AIService {

    private final RestTemplate restTemplate;
    private final UserService userService;
    private final AiMessageRepository aiMessageRepository;
    private final SimpleMapper simpleMapper;
    private final CloudinaryService cloudinaryService;

    @Value("${ai.fastapi.base-url:http://localhost:8000}")
    private String fastApiBaseUrl;

    @Override
    public ChatResponse chat(ChatRequest request) {

        try {
            create(
                    ChatResponse.builder()
                            .sentByUser(true)
                            .textContent(request.getQuestion())
                            .analysisType("question")
                            .build()
            );
        } catch (Exception e) {
            log.error("Asynchronous create failed");
        }

        try {
            log.info("Sending chat request to FastAPI: {}", request.getQuestion());
            log.info("FastAPI Base URL: {}", fastApiBaseUrl);

            // Validate request
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                throw new IllegalArgumentException("Question cannot be null or empty");
            }

            // Request body matching FastAPI QueryRequest DTO
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("question", request.getQuestion());
            requestBody.put("selected_option", request.getSelectedOption() != null ? request.getSelectedOption() : 2);
            requestBody.put("force_reindex", request.getForceReindex() != null ? request.getForceReindex() : false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String fullUrl = fastApiBaseUrl + "/query";
            log.info("Calling FastAPI at: {} with payload: {}", fullUrl, requestBody);

            long startTime = System.currentTimeMillis();
            ResponseEntity<Map> response;

            try {
                response = restTemplate.postForEntity(fullUrl, entity, Map.class);
                log.info("FastAPI response status: {}", response.getStatusCode());
                log.info("FastAPI response body: {}", response.getBody());
            } catch (Exception e) {
                log.error("FastAPI call failed with error: {}", e.getMessage(), e);
                // Check if it's a connection issue
                if (e.getMessage().contains("Connection refused") || e.getMessage().contains("ConnectException")) {
                    log.warn("Cannot connect to FastAPI at: {}", fastApiBaseUrl);
                } else if (e.getMessage().contains("timeout") || e.getMessage().contains("TimeoutException")) {
                    log.warn("FastAPI request timed out");
                } else {
                    log.warn("Unknown error when calling FastAPI: {}", e.getClass().getSimpleName());
                }

                // Fallback response when FastAPI fails
                long endTime = System.currentTimeMillis();

                ChatResponse chatResponse = ChatResponse.builder()
                        .sentByUser(false)
                        .textContent("Xin chào! Tôi là trợ lý AI fortune telling. Hiện tại hệ thống không thể kết nối với AI service. Vui lòng thử lại sau.")
                        .processingTime((double) (endTime - startTime) / 1000.0)
                        .build();
                create(chatResponse);

                return chatResponse;
            }

            long endTime = System.currentTimeMillis();

            if (response.getBody() == null) {
                log.error("FastAPI returned null response body");
                throw new RuntimeException("FastAPI returned null response body");
            }

            Map<String, Object> responseBody = response.getBody();
            log.info("Processing FastAPI response: {}", responseBody);

            // Parse FastAPI QueryResponse format
            String answer = (String) responseBody.get("answer");
            if (answer == null || answer.trim().isEmpty()) {
                // Try different response field names that FastAPI might use
                answer = (String) responseBody.get("response");
                if (answer == null || answer.trim().isEmpty()) {
                    answer = (String) responseBody.get("result");
                    if (answer == null || answer.trim().isEmpty()) {
                        log.error("FastAPI response missing answer content. Available keys: {}", responseBody.keySet());
                        throw new RuntimeException("FastAPI response missing answer content");
                    }
                }
            }

            log.info("Successfully processed AI response with {} characters", answer.length());

            // TODO: Send push noti here

            ChatResponse chatResponse = ChatResponse.builder()
                    .sentByUser(false)
                    .textContent(answer)
                    .processingTime((double) (endTime - startTime) / 1000.0)
                    .build();
            create(chatResponse);

            return chatResponse;

        } catch (Exception e) {
            log.error("Error calling FastAPI chat endpoint: {}", e.getMessage(), e);

            // More specific error messages
            if (e.getMessage().contains("Connection refused")) {
                throw new RuntimeException("Cannot connect to AI service. Please check if FastAPI service is running at: " + fastApiBaseUrl);
            } else if (e.getMessage().contains("404")) {
                throw new RuntimeException("AI service endpoint not found. Please check FastAPI service configuration.");
            } else if (e.getMessage().contains("timeout")) {
                throw new RuntimeException("AI service request timed out. Please try again.");
            }

            throw new RuntimeException("Failed to chat with AI: " + e.getMessage());
        }
    }

    @Override
    public ChatResponse analyzePalm(ImageAnalysisRequest request) {
        try {
            String mediaUrl = cloudinaryService.uploadFile(
                    request.getFile(),
                    request.getAnalysisType()
            );

            create(
                    ChatResponse.builder()
                            .sentByUser(true)
                            .imageUrl(mediaUrl)
                            .analysisType(request.getAnalysisType())
                            .build()
            );
        } catch (Exception e) {
            log.error("Asynchronous create failed");
        }

        return analyzeImage(request, "/analyze-palm", "palm");
    }

    @Override
    public ChatResponse analyzeFace(ImageAnalysisRequest request) {
        try {
            String mediaUrl = cloudinaryService.uploadFile(
                    request.getFile(),
                    request.getAnalysisType()
            );

            create(
                    ChatResponse.builder()
                            .sentByUser(true)
                            .imageUrl(mediaUrl)
                            .analysisType(request.getAnalysisType())
                            .build()
            );
        } catch (Exception e) {
            log.error("Asynchronous create failed");
        }
        return analyzeImage(request, "/analyze-face", "face");
    }

    @Override
    public Page<ChatResponse> myChatResponse(Pageable pageable) {
        return aiMessageRepository.findAllByUser_Id(userService.getUser().getId(), pageable)
                .map(aiMessage -> simpleMapper.mapTo(aiMessage, ChatResponse.class));
    }

    private ChatResponse analyzeImage(ImageAnalysisRequest request, String endpoint, String analysisType) {
        try {
            log.info("Sending image analysis request to FastAPI endpoint: {}", endpoint);
            log.info("FastAPI Base URL for image analysis: {}", fastApiBaseUrl);

            if (request.getFile() == null || request.getFile().isEmpty()) {
                throw new IllegalArgumentException("Image file cannot be null or empty");
            }

            // Prepare multipart form data for FastAPI UploadFile
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

            ByteArrayResource fileResource = new ByteArrayResource(request.getFile().getBytes()) {
                @Override
                public String getFilename() {
                    return request.getFile().getOriginalFilename();
                }
            };
            parts.add("file", fileResource);

            // Add selected_option parameter (default to 1 if not provided)
            Integer selectedOption = request.getSelectedOption() != null ? request.getSelectedOption() : 1;
            parts.add("selected_option", selectedOption);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Accept", "application/json");
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(parts, headers);

            String fullUrl = fastApiBaseUrl + endpoint;
            log.info("Calling FastAPI at: {} with file: {} (size: {} bytes), selected_option: {}",
                fullUrl, request.getFile().getOriginalFilename(), request.getFile().getSize(), selectedOption);

            long startTime = System.currentTimeMillis();
            ResponseEntity<Map> response;

            try {
                response = restTemplate.postForEntity(fullUrl, entity, Map.class);
                log.info("FastAPI image analysis response status: {}", response.getStatusCode());
                log.info("FastAPI image analysis response body: {}", response.getBody());
            } catch (Exception e) {
                log.error("FastAPI image analysis call failed with error: {}", e.getMessage(), e);

                // Fallback response for connection errors
                long endTime = System.currentTimeMillis();

                ChatResponse chatResponse = ChatResponse.builder()
                        .sentByUser(false)
                        .textContent("Xin lỗi, không thể kết nối đến hệ thống phân tích " + analysisType + ". Lỗi: " + e.getMessage())
                        .processingTime((double) (endTime - startTime) / 1000.0)
                        .build();
                create(chatResponse);

                return chatResponse;
            }

            long endTime = System.currentTimeMillis();

            if (response.getBody() == null) {
                log.error("FastAPI returned null response body for image analysis");
                throw new RuntimeException("FastAPI returned null response body for image analysis");
            }

            Map<String, Object> responseBody = response.getBody();
            log.info("Processing FastAPI {} response: {}", endpoint, responseBody);

            // Parse FastAPI response - check for errors first
            String analysisResult = (String) responseBody.get("answer");
            if (analysisResult == null || analysisResult.trim().isEmpty()) {
                // Try different response field names
                analysisResult = (String) responseBody.get("analysis_result");
                if (analysisResult == null || analysisResult.trim().isEmpty()) {
                    analysisResult = (String) responseBody.get("result");
                    if (analysisResult == null || analysisResult.trim().isEmpty()) {
                        log.error("FastAPI image analysis response missing content. Available keys: {}", responseBody.keySet());
                        throw new RuntimeException("FastAPI response missing analysis result");
                    }
                }
            }

            // Check if the response indicates an error (like RAG initialization issues)
            if (analysisResult.toLowerCase().contains("lỗi") ||
                analysisResult.toLowerCase().contains("error") ||
                analysisResult.toLowerCase().contains("rag khởi tạo bị lỗi")) {

                log.warn("FastAPI returned error message for {} analysis: {}", analysisType, analysisResult);

                ChatResponse chatResponse = ChatResponse.builder()
                        .sentByUser(false)
                        .textContent("Xin lỗi, hệ thống phân tích " + analysisType + " hiện đang được cấu hình. Vui lòng thử lại sau hoặc liên hệ admin để kiểm tra cấu hình AI service.")
                        .processingTime((double) (endTime - startTime) / 1000.0)
                        .build();
                create(chatResponse);

                return chatResponse;
            }

            log.info("Successfully processed image analysis response with {} characters", analysisResult.length());

            ChatResponse chatResponse = ChatResponse.builder()
                    .sentByUser(false)
                    .textContent(analysisResult)
                    .processingTime((double) (endTime - startTime) / 1000.0)
                    .build();
            create(chatResponse);

            return chatResponse;

        } catch (Exception e) {
            log.error("Error calling FastAPI image analysis endpoint: {}", endpoint, e);
            throw new RuntimeException("Failed to analyze " + analysisType + " image: " + e.getMessage());
        }
    }

    @Async
    protected CompletableFuture<Void> create(ChatResponse chatResponse) {
        AiMessage aiMessage = simpleMapper.mapTo(chatResponse, AiMessage.class);
        aiMessage.setUser(userService.getUser());
        aiMessageRepository.save(aiMessage);
        return CompletableFuture.completedFuture(null);
    }
}
