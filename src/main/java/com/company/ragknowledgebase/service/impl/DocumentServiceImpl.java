package com.company.ragknowledgebase.service.impl;

import com.company.ragknowledgebase.config.AppProperties;
import com.company.ragknowledgebase.constant.AppConstants;
import com.company.ragknowledgebase.exception.BadRequestException;
import com.company.ragknowledgebase.exception.ResourceNotFoundException;
import com.company.ragknowledgebase.model.dto.request.DocumentUploadRequest;
import com.company.ragknowledgebase.model.dto.response.ResponseDtos;
import com.company.ragknowledgebase.model.entity.Document;
import com.company.ragknowledgebase.repository.DocumentRepository;
import com.company.ragknowledgebase.service.DocumentIngestionService;
import com.company.ragknowledgebase.service.DocumentService;
import com.company.ragknowledgebase.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Implementation of DocumentService: handles file storage + async ingestion trigger.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentIngestionService documentIngestionService;
    private final AppProperties appProperties;

    @Override
    public ResponseDtos.DocumentResponse uploadDocument(MultipartFile file,
                                                        DocumentUploadRequest request) {
        validateFile(file);

        String originalName = file.getOriginalFilename();
        String storedName   = UUID.randomUUID() + "_" + originalName;
        String storagePath  = saveFileToDisk(file, storedName);

        Document document = Document.builder()
                .name(storedName)
                .originalName(originalName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .status(AppConstants.STATUS_PROCESSING)
                .description(request.getDescription())
                .tags(request.getTags())
                .storagePath(storagePath)
                .chunkCount(0)
                .build();

        document = documentRepository.save(document);
        log.info("Document saved: id={}, name={}", document.getId(), originalName);

        // Async ingestion: embed and store vectors
        UUID docId = document.getId();
        String path = document.getStoragePath();
        documentIngestionService.ingestDocument(docId, path);

        return toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDtos.PagedResponse<ResponseDtos.DocumentResponse> getAllDocuments(Pageable pageable) {
        Page<Document> page = documentRepository.findAllByDeletedFalse(pageable);
        return toPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDtos.DocumentResponse getDocumentById(UUID id) {
        Document document = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        return toResponse(document);
    }

    @Override
    public void deleteDocument(UUID id) {
        Document document = documentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        document.setDeleted(true);
        documentRepository.save(document);
        log.info("Document soft-deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDtos.PagedResponse<ResponseDtos.DocumentResponse> searchDocuments(String name,
                                                                                      Pageable pageable) {
        Page<Document> page = documentRepository.searchByName(name, pageable);
        return toPagedResponse(page);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new BadRequestException("File name is missing");
        }
        String ext = FileUtils.getExtension(originalName).toLowerCase();
        if (!appProperties.getUpload().getAllowedExtensions().contains(ext)) {
            throw new BadRequestException("File type not supported: " + ext +
                    ". Allowed: " + appProperties.getUpload().getAllowedExtensions());
        }
    }

    private String saveFileToDisk(MultipartFile file, String storedName) {
        try {
            Path uploadDir = Paths.get(appProperties.getUpload().getStoragePath());
            Files.createDirectories(uploadDir);
            Path destination = uploadDir.resolve(storedName);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return destination.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    private ResponseDtos.DocumentResponse toResponse(Document doc) {
        return ResponseDtos.DocumentResponse.builder()
                .id(doc.getId())
                .name(doc.getName())
                .originalName(doc.getOriginalName())
                .contentType(doc.getContentType())
                .fileSize(doc.getFileSize())
                .status(doc.getStatus())
                .description(doc.getDescription())
                .tags(doc.getTags())
                .chunkCount(doc.getChunkCount())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .createdBy(doc.getCreatedBy())
                .build();
    }

    private ResponseDtos.PagedResponse<ResponseDtos.DocumentResponse> toPagedResponse(Page<Document> page) {
        return ResponseDtos.PagedResponse.<ResponseDtos.DocumentResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
