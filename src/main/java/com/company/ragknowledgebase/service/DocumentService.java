package com.company.ragknowledgebase.service;

import com.company.ragknowledgebase.model.dto.request.DocumentUploadRequest;
import com.company.ragknowledgebase.model.dto.response.ResponseDtos;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Service interface for document lifecycle management and ingestion.
 */
public interface DocumentService {

    ResponseDtos.DocumentResponse uploadDocument(MultipartFile file, DocumentUploadRequest request);

    ResponseDtos.PagedResponse<ResponseDtos.DocumentResponse> getAllDocuments(Pageable pageable);

    ResponseDtos.DocumentResponse getDocumentById(UUID id);

    void deleteDocument(UUID id);

    ResponseDtos.PagedResponse<ResponseDtos.DocumentResponse> searchDocuments(String name, Pageable pageable);
}
