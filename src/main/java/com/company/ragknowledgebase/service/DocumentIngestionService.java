package com.company.ragknowledgebase.service;

import java.util.UUID;

/**
 * Service for async document ingestion and vectorization.
 */
public interface DocumentIngestionService {

    void ingestDocument(UUID documentId, String storagePath);
}
