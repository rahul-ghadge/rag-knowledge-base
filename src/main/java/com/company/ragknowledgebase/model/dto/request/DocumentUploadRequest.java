package com.company.ragknowledgebase.model.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for document upload (multipart form data).
 */
@Data
public class DocumentUploadRequest {

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private List<String> tags;
}
