//package com.company.ragknowledgebase.service;
//
//import com.company.ragknowledgebase.config.AppProperties;
//import com.company.ragknowledgebase.model.dto.request.SearchRequest;
//import com.company.ragknowledgebase.model.dto.response.ResponseDtos;
//import com.company.ragknowledgebase.repository.ChatMessageRepository;
//import com.company.ragknowledgebase.repository.ChatSessionRepository;
//import com.company.ragknowledgebase.service.impl.RagServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.document.Document;
//import org.springframework.ai.vectorstore.VectorStore;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("RagService Tests")
//class RagServiceTest {
//
//    @Mock VectorStore           vectorStore;
//    @Mock ChatClient.Builder    chatClientBuilder;
//    @Mock ChatSessionRepository chatSessionRepository;
//    @Mock ChatMessageRepository chatMessageRepository;
//    @Mock AppProperties         appProperties;
//
//    @InjectMocks RagServiceImpl ragService;
//
//    @BeforeEach
//    void setup() {
//        AppProperties.RagProperties ragProps = new AppProperties.RagProperties();
//        given(appProperties.getRag()).willReturn(ragProps);
//    }
//
//    @Test
//    @DisplayName("semanticSearch - returns results from vector store")
//    void semanticSearch_returnsResults() {
//        Document doc = new Document("Test chunk content",
//                Map.of("documentId", "doc-123", "chunkIndex", 0));
//
//        given(vectorStore.similaritySearch(any())).willReturn(List.of(doc));
//
//        SearchRequest req = new SearchRequest();
//        req.setQuery("test query");
//        req.setTopK(5);
//        req.setSimilarityThreshold(0.7);
//
//        ResponseDtos.SearchResponse response = ragService.semanticSearch(req);
//
//        assertThat(response).isNotNull();
//        assertThat(response.getQuery()).isEqualTo("test query");
//        assertThat(response.getResults()).hasSize(1);
//        assertThat(response.getResults().get(0).getContent()).isEqualTo("Test chunk content");
//    }
//
//    @Test
//    @DisplayName("semanticSearch - returns empty when no matches")
//    void semanticSearch_noMatches_returnsEmpty() {
//        given(vectorStore.similaritySearch(any())).willReturn(List.of());
//
//        SearchRequest req = new SearchRequest();
//        req.setQuery("obscure query");
//        req.setTopK(5);
//        req.setSimilarityThreshold(0.95);
//
//        ResponseDtos.SearchResponse response = ragService.semanticSearch(req);
//
//        assertThat(response.getResults()).isEmpty();
//        assertThat(response.getTotalResults()).isZero();
//    }
//}
