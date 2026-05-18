//package com.company.ragknowledgebase.config;
//
//import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
//import org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.jdbc.core.JdbcTemplate;
//
///**
// * Spring AI vector store configuration using PostgreSQL pgvector.
// * Uses Spring AI 1.0.0 PgVectorStore builder API.
// */
//@Configuration
//public class VectorStoreConfig {
//
//    @Primary
//    @Bean
//    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
//        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
//                .dimensions(1536)
//                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
//                .indexType(PgVectorStore.PgIndexType.HNSW)
//                .initializeSchema(false)
//                .build();
//    }
//}
