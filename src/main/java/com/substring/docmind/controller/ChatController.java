package com.substring.docmind.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.substring.docmind.dto.ApiResponse;
import com.substring.docmind.dto.ChatRequestDto;
import com.substring.docmind.dto.ChatResponseDto;
import com.substring.docmind.dto.SearchRequestDto;
import com.substring.docmind.dto.SearchResultDto;
import com.substring.docmind.service.RagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
@Tag(name = "Chat Controller", description = "All chat related apis")
@RequiredArgsConstructor
public class ChatController {

    private final RagService ragService;

    @PostMapping("/query")
    @Operation(summary = "Ask a question", description = "This api is used to ask a question")
    public ResponseEntity<ApiResponse<ChatResponseDto>> askQuestion(@Valid @RequestBody ChatRequestDto request) {
        ChatResponseDto response = ragService.askQuestion(request);
        return ResponseEntity.ok(ApiResponse.<ChatResponseDto>builder()
                .success(true)
                .message("Query processed successfully")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/stream")
    @Operation(summary = "Stream real time Q&A token via Server-Sent Events (SSE)", description = "This api is used to stream real time Q&A token via Server-Sent Events (SSE) ")
    public Flux<String> streamQuestionAnswer(@Valid @RequestBody ChatRequestDto request) {
        return ragService.streamQuestionAnswer(request);
    }

    @PostMapping("/search/similarity")
    @Operation(summary = "Perform semantic similarity search on stored document vector ")
    public ResponseEntity<ApiResponse<SearchResultDto>> searchSimilar(@Valid @RequestBody SearchRequestDto request) {
        SearchResultDto result = ragService.searchSimilarChunks(request);
        return ResponseEntity.ok(
                ApiResponse.<SearchResultDto>builder()
                        .success(true)
                        .message("Similarity search performed successfully")
                        .data(result)
                        .timestamp(LocalDateTime.now())
                        .build());

    }

}
