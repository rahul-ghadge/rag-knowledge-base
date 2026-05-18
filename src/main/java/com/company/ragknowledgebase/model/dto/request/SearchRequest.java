package com.company.ragknowledgebase.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for semantic search.
 */
@Data
public class SearchRequest {

    @NotBlank(message = "Query must not be blank")
    @Size(max = 1000, message = "Query must be at most 1000 characters")
    private String query;

    private Integer topK = 5;

    private Double similarityThreshold = 0.7;
}
