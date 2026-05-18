package com.company.ragknowledgebase.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTOs for Document, Chat, and Search operations.
 */
public final class ResponseDtos {

    private ResponseDtos() {}

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DocumentResponse {
        private UUID id;
        private String name;
        private String originalName;
        private String contentType;
        private Long fileSize;
        private String status;
        private String description;
        private List<String> tags;
        private Integer chunkCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChatResponse {
        private UUID messageId;
        private UUID sessionId;
        private String question;
        private String answer;
        private List<SourceInfo> sources;
        private Integer tokensUsed;
        private String modelUsed;
        private LocalDateTime timestamp;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SourceInfo {
        private String documentId;
        private String documentName;
        private String excerpt;
        private Double similarity;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SearchResponse {
        private String query;
        private List<SearchResult> results;
        private Integer totalResults;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SearchResult {
        private String documentId;
        private String documentName;
        private String content;
        private Double similarity;
        private Integer chunkIndex;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PagedResponse<T> {
        private List<T> content;
        private int pageNo;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean last;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ChatSessionResponse {
        private UUID id;
        private String sessionName;
        private String userIdentifier;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Boolean active;
        private Integer messageCount;
    }
}
