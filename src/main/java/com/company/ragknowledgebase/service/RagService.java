package com.company.ragknowledgebase.service;

import com.company.ragknowledgebase.model.dto.request.ChatRequest;
import com.company.ragknowledgebase.model.dto.request.SearchRequest;
import com.company.ragknowledgebase.model.dto.response.ResponseDtos;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for RAG (Retrieval-Augmented Generation) operations.
 */
public interface RagService {

    ResponseDtos.ChatResponse chat(ChatRequest request);

    ResponseDtos.SearchResponse semanticSearch(SearchRequest request);

    List<ResponseDtos.ChatSessionResponse> getSessionsByUser(String userIdentifier);

    List<ResponseDtos.ChatResponse> getSessionHistory(UUID sessionId);

    void deleteSession(UUID sessionId);
}
