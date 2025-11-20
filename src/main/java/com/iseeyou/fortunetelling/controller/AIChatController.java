package com.iseeyou.fortunetelling.controller;

import com.iseeyou.fortunetelling.controller.base.AbstractBaseController;
import com.iseeyou.fortunetelling.dto.request.chat.ai.ChatRequest;
import com.iseeyou.fortunetelling.dto.request.chat.ai.ImageAnalysisRequest;
import com.iseeyou.fortunetelling.dto.response.PageResponse;
import com.iseeyou.fortunetelling.dto.response.SingleResponse;
import com.iseeyou.fortunetelling.dto.response.chat.ai.ChatResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
                    request.getQuestion(), request.getSelectedOption(), request.getForceReindex());

            // Validate input parameters
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                throw new IllegalArgumentException("Question parameter cannot be null or empty");
            }

            Boolean forceReindex = request.getForceReindex() != null ? request.getForceReindex() : false;

            ChatRequest processedRequest = ChatRequest.builder()
                    .question(request.getQuestion())
                    .selectedOption(request.getSelectedOption())
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
                                    schema = @Schema(implementation = ChatResponse.class)
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
    public ResponseEntity<SingleResponse<ChatResponse>> analyzePalm(
            @Parameter(description = "Palm image file (JPEG, PNG)", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Selected RAG option (1, 2, or 3)", required = false)
            @RequestParam(value = "selected_option", required = false, defaultValue = "1") Integer selectedOption
    ) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }

            ImageAnalysisRequest request = ImageAnalysisRequest.builder()
                    .file(file)
                    .analysisType("palm")
                    .selectedOption(selectedOption)
                    .build();

            ChatResponse response = aiService.analyzePalm(request);
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
                                    schema = @Schema(implementation = ChatResponse.class)
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
    public ResponseEntity<SingleResponse<ChatResponse>> analyzeFace(
            @Parameter(description = "Face image file (JPEG, PNG)", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Selected RAG option (1, 2, or 3)", required = false)
            @RequestParam(value = "selected_option", required = false, defaultValue = "1") Integer selectedOption
    ) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }

            ImageAnalysisRequest request = ImageAnalysisRequest.builder()
                    .file(file)
                    .analysisType("face")
                    .selectedOption(selectedOption)
                    .build();


            ChatResponse response = aiService.analyzeFace(request);
            return responseFactory.successSingle(response, "Face analysis completed successfully");

        } catch (Exception e) {
            log.error("Error in face analysis", e);
            throw new RuntimeException("Failed to analyze face: " + e.getMessage());
        }
    }

    @GetMapping("/my-chat-history")
    @Operation(
            summary = "Get My Chat History",
            description = "Get paginated chat history for the current user.",
            security = @SecurityRequirement(name = SECURITY_SCHEME_NAME),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful operation",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PageResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<PageResponse<ChatResponse>> myChatResponse(
            @Parameter(description = "Page number (1-based)")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortType,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<ChatResponse> chatHistory = aiService.myChatResponse(pageable);
        return responseFactory.successPage(chatHistory, "Chat history retrieved successfully");
    }
}
