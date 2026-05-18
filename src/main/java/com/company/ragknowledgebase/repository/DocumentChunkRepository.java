package com.company.ragknowledgebase.repository;

import com.company.ragknowledgebase.model.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository for DocumentChunk entity.
 */
@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(UUID documentId);

    @Modifying
    @Query("DELETE FROM DocumentChunk c WHERE c.document.id = :documentId")
    void deleteByDocumentId(@Param("documentId") UUID documentId);

    long countByDocumentId(UUID documentId);
}
