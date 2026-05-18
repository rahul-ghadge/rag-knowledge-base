package com.company.ragknowledgebase.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents an uploaded document in the knowledge base.
 */
@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "chunks")
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "original_name", nullable = false, length = 500)
    private String originalName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "storage_path", length = 1000)
    private String storagePath;

    @Builder.Default
    @Column(name = "chunk_count")
    private Integer chunkCount = 0;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "document_tags",
            joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DocumentChunk> chunks = new ArrayList<>();
}
