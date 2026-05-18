package com.company.ragknowledgebase.controller;

import com.company.ragknowledgebase.constant.AppConstants;
import com.company.ragknowledgebase.model.dto.request.ChatRequest;
import com.company.ragknowledgebase.model.dto.request.SearchRequest;
import com.company.ragknowledgebase.model.dto.response.ApiResponse;
import com.company.ragknowledgebase.model.dto.response.ResponseDtos;
import com.company.ragknowledgebase.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for chat and semantic search operations.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Chat & Search", description = "AI chat and semantic search APIs")
public class ChatController {

    private final RagService ragService;

    @PostMapping(AppConstants.CHAT_PATH)
    @Operation(summary = "Ask a question", description = "Ask the AI assistant a question using RAG")
    public ResponseEntity<ApiResponse<ResponseDtos.ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request) {

        log.info("Chat request from user={}: {}", request.getUserIdentifier(), request.getQuestion());
        return ResponseEntity.ok(ApiResponse.success(ragService.chat(request)));
    }

    @PostMapping(AppConstants.SEARCH_PATH)
    @Operation(summary = "Semantic search", description = "Search the knowledge base using vector similarity")
    public ResponseEntity<ApiResponse<ResponseDtos.SearchResponse>> search(
            @Valid @RequestBody SearchRequest request) {

        log.info("Semantic search: {}", request.getQuery());
        return ResponseEntity.ok(ApiResponse.success(ragService.semanticSearch(request)));
    }

    @GetMapping(AppConstants.CHAT_PATH + "/sessions")
    @Operation(summary = "Get sessions for a user")
    public ResponseEntity<ApiResponse<List<ResponseDtos.ChatSessionResponse>>> getSessions(
            @RequestParam(defaultValue = "anonymous") String userIdentifier) {

        return ResponseEntity.ok(ApiResponse.success(ragService.getSessionsByUser(userIdentifier)));
    }

    @GetMapping(AppConstants.CHAT_PATH + "/sessions/{sessionId}/history")
    @Operation(summary = "Get conversation history for a session")
    public ResponseEntity<ApiResponse<List<ResponseDtos.ChatResponse>>> getHistory(
            @PathVariable UUID sessionId) {

        return ResponseEntity.ok(ApiResponse.success(ragService.getSessionHistory(sessionId)));
    }

    @DeleteMapping(AppConstants.CHAT_PATH + "/sessions/{sessionId}")
    @Operation(summary = "Delete / close a session")
    public ResponseEntity<ApiResponse<Void>> deleteSession(@PathVariable UUID sessionId) {
        ragService.deleteSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Session closed"));
    }
}
