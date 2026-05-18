//package com.company.ragknowledgebase.repository;
//
//import com.company.ragknowledgebase.model.entity.Document;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@Testcontainers
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@DisplayName("DocumentRepository Integration Tests")
//class DocumentRepositoryTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgres =
//            new PostgreSQLContainer<>("pgvector/pgvector:pg16")
//                    .withDatabaseName("ragdb_test")
//                    .withUsername("test")
//                    .withPassword("test");
//
//    @DynamicPropertySource
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url",      postgres::getJdbcUrl);
//        registry.add("spring.datasource.username",  postgres::getUsername);
//        registry.add("spring.datasource.password",  postgres::getPassword);
//        registry.add("spring.flyway.enabled",       () -> "true");
//    }
//
//    @Autowired DocumentRepository documentRepository;
//
//    @Test
//    @DisplayName("save and findByIdAndDeletedFalse - returns document")
//    void saveAndFind_returnsDocument() {
//        Document doc = Document.builder()
//                .name("test.pdf")
//                .originalName("test.pdf")
//                .contentType("application/pdf")
//                .fileSize(1024L)
//                .status("INDEXED")
//                .chunkCount(5)
//                .build();
//
//        Document saved = documentRepository.save(doc);
//        assertThat(saved.getId()).isNotNull();
//
//        var found = documentRepository.findByIdAndDeletedFalse(saved.getId());
//        assertThat(found).isPresent();
//        assertThat(found.get().getOriginalName()).isEqualTo("test.pdf");
//    }
//
//    @Test
//    @DisplayName("soft delete - findByIdAndDeletedFalse returns empty")
//    void softDelete_notFoundAfterDelete() {
//        Document doc = Document.builder()
//                .name("del.pdf").originalName("del.pdf")
//                .contentType("application/pdf").fileSize(512L)
//                .status("INDEXED").chunkCount(0).build();
//
//        Document saved = documentRepository.save(doc);
//        saved.setDeleted(true);
//        documentRepository.save(saved);
//
//        var found = documentRepository.findByIdAndDeletedFalse(saved.getId());
//        assertThat(found).isEmpty();
//    }
//
//    @Test
//    @DisplayName("searchByName - finds matching documents")
//    void searchByName_returnsMatches() {
//        documentRepository.save(Document.builder()
//                .name("annual-report.pdf").originalName("annual-report.pdf")
//                .contentType("application/pdf").fileSize(2048L)
//                .status("INDEXED").chunkCount(20).build());
//
//        Page<Document> results = documentRepository.searchByName("annual", PageRequest.of(0, 10));
//        assertThat(results.getContent()).isNotEmpty();
//        assertThat(results.getContent().get(0).getName()).contains("annual");
//    }
//}
