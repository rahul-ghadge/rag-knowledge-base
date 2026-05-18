package com.company.ragknowledgebase.service.impl;

import com.company.ragknowledgebase.config.AppProperties;
import com.company.ragknowledgebase.constant.AppConstants;
import com.company.ragknowledgebase.model.entity.DocumentChunk;
import com.company.ragknowledgebase.repository.DocumentChunkRepository;
import com.company.ragknowledgebase.repository.DocumentRepository;
import com.company.ragknowledgebase.service.DocumentIngestionService;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Async document ingestion: parse → chunk → embed → store in pgvector.
 * Uses Spring AI TikaDocumentReader for broad format support (PDF, DOCX, TXT, etc.)
 * and LangChain4j DocumentSplitter for fine-grained chunking control.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionServiceImpl implements DocumentIngestionService {

    private final VectorStore              vectorStore;
    private final DocumentRepository       documentRepository;
    private final DocumentChunkRepository  documentChunkRepository;
    private final AppProperties            appProperties;

    @Async
    @Override
    @Transactional
    public void ingestDocument(UUID documentId, String storagePath) {
        log.info("Starting ingestion for documentId={}", documentId);

        try {
            // 1. Parse with Apache Tika (handles PDF, DOCX, TXT, MD, etc.)
            TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(storagePath));
            List<Document> rawDocs   = reader.get();

            // 2. Chunk with Spring AI TokenTextSplitter
            int chunkSize    = appProperties.getRag().getChunkSize();
            int chunkOverlap = appProperties.getRag().getChunkOverlap();

            TokenTextSplitter splitter = new TokenTextSplitter(chunkSize, chunkOverlap, 5, 10_000, true);
            List<Document> chunks      = splitter.apply(rawDocs);

            // 3. Enrich metadata with document ID for retrieval traceability
            AtomicInteger index = new AtomicInteger(0);
            List<Document> enrichedChunks = chunks.stream()
                    .map(chunk -> {
                        Map<String, Object> meta = new HashMap<>(chunk.getMetadata());
                        meta.put("documentId",   documentId.toString());
                        meta.put("chunkIndex",   index.getAndIncrement());
                        meta.put("storagePath",  storagePath);
                        return new Document(chunk.getText(), meta);
                    })
                    .toList();

            // 4. Embed + store in pgvector via Spring AI
            vectorStore.add(enrichedChunks);
            log.info("Stored {} chunks in vector store for documentId={}", enrichedChunks.size(), documentId);

            // 5. Persist chunk records for audit/search traceability
            com.company.ragknowledgebase.model.entity.Document docEntity =
                    documentRepository.findById(documentId)
                            .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

            List<DocumentChunk> chunkEntities = enrichedChunks.stream()
                    .map(chunk -> DocumentChunk.builder()
                            .document(docEntity)
                            .chunkIndex((Integer) chunk.getMetadata().get("chunkIndex"))
                            .content(chunk.getText())
                            .tokenCount(chunk.getText().split("\\s+").length)
                            .metadata(chunk.getMetadata())
                            .build())
                    .toList();

            documentChunkRepository.saveAll(chunkEntities);

            // 6. Update document status → INDEXED
            documentRepository.updateStatus(documentId, AppConstants.STATUS_INDEXED);
            documentRepository.updateChunkCount(documentId, enrichedChunks.size());

            log.info("Ingestion complete for documentId={}: {} chunks indexed", documentId, enrichedChunks.size());

        } catch (Exception e) {
            log.error("Ingestion failed for documentId={}: {}", documentId, e.getMessage(), e);
            documentRepository.updateStatus(documentId, AppConstants.STATUS_FAILED);
        }
    }
}
