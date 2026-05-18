package com.company.ragknowledgebase.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Strongly-typed application properties bound from application.yml.
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NotNull
    private RagProperties rag = new RagProperties();

    @NotNull
    private UploadProperties upload = new UploadProperties();

    @NotNull
    private CorsProperties cors = new CorsProperties();

    @Data
    public static class RagProperties {
        @Min(100)
        private int chunkSize = 1000;
        @Min(0)
        private int chunkOverlap = 200;
        @Min(1)
        private int topKResults = 5;
        private double similarityThreshold = 0.7;
    }

    @Data
    public static class UploadProperties {
        private List<String> allowedExtensions = List.of("pdf", "docx", "doc", "txt", "md");
        @NotBlank
        private String storagePath = "./uploads";
    }

    @Data
    public static class CorsProperties {
        private List<String> allowedOrigins = List.of("http://localhost:8080");
    }
}
