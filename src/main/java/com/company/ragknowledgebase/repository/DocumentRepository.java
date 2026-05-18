package com.company.ragknowledgebase.repository;

import com.company.ragknowledgebase.model.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for Document entity.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Page<Document> findAllByDeletedFalse(Pageable pageable);

    Page<Document> findAllByDeletedFalseAndStatus(String status, Pageable pageable);

    Optional<Document> findByIdAndDeletedFalse(UUID id);

    @Query("SELECT d FROM Document d WHERE d.deleted = false AND :tag MEMBER OF d.tags")
    List<Document> findByTag(@Param("tag") String tag);

    @Query("SELECT d FROM Document d WHERE d.deleted = false AND d.name LIKE %:name%")
    Page<Document> searchByName(@Param("name") String name, Pageable pageable);

    @Modifying
    @Query("UPDATE Document d SET d.status = :status WHERE d.id = :id")
    void updateStatus(@Param("id") UUID id, @Param("status") String status);

    @Modifying
    @Query("UPDATE Document d SET d.chunkCount = :count WHERE d.id = :id")
    void updateChunkCount(@Param("id") UUID id, @Param("count") Integer count);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.deleted = false AND d.status = :status")
    long countByStatus(@Param("status") String status);
}
