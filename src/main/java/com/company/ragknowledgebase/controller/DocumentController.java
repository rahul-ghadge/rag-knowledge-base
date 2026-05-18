package com.company.ragknowledgebase.controller;

import com.company.ragknowledgebase.constant.AppConstants;
import com.company.ragknowledgebase.model.dto.request.DocumentUploadRequest;
import com.company.ragknowledgebase.model.dto.response.ApiResponse;
import com.company.ragknowledgebase.model.dto.response.ResponseDtos;
import com.company.ragknowledgebase.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * REST controller for document management: upload, list, get, delete, search.
 */
@Slf4j
@RestController
@RequestMapping(AppConstants.DOCUMENTS_PATH)
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document upload and management APIs")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document", description = "Upload PDF, DOCX, TXT, or MD file for indexing")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<ResponseDtos.DocumentResponse>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute DocumentUploadRequest request) {

        log.info("Uploading document: name={}, size={}", file.getOriginalFilename(), file.getSize());
        ResponseDtos.DocumentResponse response = documentService.uploadDocument(file, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Document uploaded successfully and queued for indexing"));
    }

    @GetMapping
    @Operation(summary = "List all documents", description = "Returns paginated list of all indexed documents")
    public ResponseEntity<ApiResponse<ResponseDtos.PagedResponse<ResponseDtos.DocumentResponse>>> getAllDocuments(
            @RequestParam(defaultValue = "0")                              int pageNo,
            @RequestParam(defaultValue = "20")                             int pageSize,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_BY)    String sortBy,
            @RequestParam(defaultValue = AppConstants.DEFAULT_SORT_DIR)    String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(pageNo, Math.min(pageSize, AppConstants.MAX_PAGE_SIZE), sort);
        return ResponseEntity.ok(ApiResponse.success(documentService.getAllDocuments(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<ApiResponse<ResponseDtos.DocumentResponse>> getDocumentById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getDocumentById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Document deleted successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search documents by name")
    public ResponseEntity<ApiResponse<ResponseDtos.PagedResponse<ResponseDtos.DocumentResponse>>> searchDocuments(
            @RequestParam String name,
            @RequestParam(defaultValue = "0")   int pageNo,
            @RequestParam(defaultValue = "20")  int pageSize) {

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success(documentService.searchDocuments(name, pageable)));
    }
}
