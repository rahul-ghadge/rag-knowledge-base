package com.company.ragknowledgebase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * RAG Knowledge Base - AI-Powered Enterprise Knowledge Assistant.
 * Supports PDF/doc upload, semantic search, and conversational Q&A
 * using Spring AI, LangChain4j, and pgvector.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableConfigurationProperties
public class RagKnowledgeBaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagKnowledgeBaseApplication.class, args);
    }
}
