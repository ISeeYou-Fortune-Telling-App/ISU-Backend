package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.ai.ChatRequest;
import com.iseeyou.fortunetelling.dto.request.ai.ImageAnalysisRequest;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.ai.ChatResponse;
import com.iseeyou.fortunetelling.dto.response.ai.ImageAnalysisResponse;
import com.iseeyou.fortunetelling.dto.response.error.ErrorResponse;
import com.iseeyou.fortunetelling.service.ai.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import static com.iseeyou.fortunetelling.util.Constants.SECURITY_SCHEME_NAME;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai-chat")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Tag(name = "007. AI Chat", description = "Simple AI Chat and Analysis API")
@Slf4j
public class AIChatController extends AbstractBaseController {

    private final AIService aiService;

    @PostMapping("/query")
    @Operation(
            summary = "Chat with AI - Simple Version",
            description = "Send question to AI and get response with basic parameters only.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ChatResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<ChatResponse>> chatWithAI(
            @RequestBody ChatRequest request
    ) {
        try {
            log.info("Received AI chat request - Question: {}, TopK: {}, ForceReindex: {}",
                    request.getQuestion(), request.getTopK(), request.getForceReindex());

            // Validate input parameters
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                throw new IllegalArgumentException("Question parameter cannot be null or empty");
            }

            Integer topK = request.getTopK() != null ? request.getTopK() : 5;
            if (topK < 1 || topK > 50) {
                throw new IllegalArgumentException("TopK must be between 1 and 50");
            }

            Boolean forceReindex = request.getForceReindex() != null ? request.getForceReindex() : false;

            ChatRequest processedRequest = ChatRequest.builder()
                    .question(request.getQuestion().trim())
                    .mode("mix")  // Always use "mix" mode as specified
                    .topK(topK)
                    .forceReindex(forceReindex)
                    .build();

            log.info("Built ChatRequest: {}", processedRequest);

            ChatResponse response = aiService.chat(processedRequest);
            return responseFactory.successSingle(response, "AI chat completed successfully");

        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in AI chat controller", e);
            throw new RuntimeException("Failed to process AI chat request: " + e.getMessage());
        }
    }

    @PostMapping(value = "/query-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @CrossOrigin(origins = "*", methods = {RequestMethod.POST, RequestMethod.OPTIONS})
    @Operation(
            summary = "Chat with AI - Streaming Response",
            description = "Send question to AI and get streaming response.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME)
    )
    public Flux<String> chatWithAIStream(
            @RequestBody ChatRequest request
    ) {
        try {
            log.info("Received AI streaming chat request - Question: {}, TopK: {}, ForceReindex: {}",
                    request.getQuestion(), request.getTopK(), request.getForceReindex());

            // Validate input parameters
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                log.error("Question parameter is null or empty for streaming request");
                return Flux.error(new IllegalArgumentException("Question parameter cannot be null or empty"));
            }

            Integer topK = request.getTopK() != null ? request.getTopK() : 5;
            if (topK < 1 || topK > 50) {
                log.error("Invalid topK parameter: {} for streaming request", topK);
                return Flux.error(new IllegalArgumentException("TopK must be between 1 and 50"));
            }

            Boolean forceReindex = request.getForceReindex() != null ? request.getForceReindex() : false;

            ChatRequest processedRequest = ChatRequest.builder()
                    .question(request.getQuestion().trim())
                    .mode("mix")  // Explicitly set mode
                    .topK(topK)
                    .forceReindex(forceReindex)
                    .build();

            log.info("Built ChatRequest for streaming: {}", processedRequest);

            return aiService.chatStream(processedRequest)
                    .doOnSubscribe(subscription -> log.info("Started streaming response for question: {}", request.getQuestion()))
                    .doOnNext(chunk -> log.debug("Streaming chunk: {}", chunk))
                    .doOnComplete(() -> log.info("Completed streaming response for question: {}", request.getQuestion()))
                    .doOnError(error -> log.error("Error in streaming response: {}", error.getMessage()));

        } catch (Exception e) {
            log.error("Error in AI streaming chat controller", e);
            return Flux.error(new RuntimeException("Failed to stream chat with AI: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/analyze-palm", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Analyze Palm Image",
            description = "Upload palm image for AI analysis.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ImageAnalysisResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request - Invalid file",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<ImageAnalysisResponse>> analyzePalm(
            @Parameter(description = "Palm image file (JPEG, PNG)", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }

            ImageAnalysisRequest request = ImageAnalysisRequest.builder()
                    .file(file)
                    .analysisType("palm")
                    .build();

            ImageAnalysisResponse response = aiService.analyzePalm(request);
            return responseFactory.successSingle(response, "Palm analysis completed successfully");

        } catch (Exception e) {
            log.error("Error in palm analysis", e);
            throw new RuntimeException("Failed to analyze palm: " + e.getMessage());
        }
    }

    @PostMapping(value = "/analyze-face", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Analyze Face Image",
            description = "Upload face image for AI analysis.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ImageAnalysisResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request - Invalid file",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<SingleResponse<ImageAnalysisResponse>> analyzeFace(
            @Parameter(description = "Face image file (JPEG, PNG)", required = true)
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }

            ImageAnalysisRequest request = ImageAnalysisRequest.builder()
                    .file(file)
                    .analysisType("face")
                    .build();


            ImageAnalysisResponse response = aiService.analyzeFace(request);
            return responseFactory.successSingle(response, "Face analysis completed successfully");

        } catch (Exception e) {
            log.error("Error in face analysis", e);
            throw new RuntimeException("Failed to analyze face: " + e.getMessage());
        }
    }
}
