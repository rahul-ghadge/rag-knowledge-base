package com.company.ragknowledgebase.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * Request DTO for asking questions to the AI assistant.
 */
@Data
public class ChatRequest {

    @NotBlank(message = "Question must not be blank")
    @Size(max = 2000, message = "Question must be at most 2000 characters")
    private String question;

    private UUID sessionId;

    private Integer topK;

    private Double similarityThreshold;

    private String userIdentifier = "anonymous";
}
