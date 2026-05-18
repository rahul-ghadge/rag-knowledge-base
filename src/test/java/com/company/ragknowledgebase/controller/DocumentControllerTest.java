//package com.company.ragknowledgebase.controller;
//
//import com.company.ragknowledgebase.model.dto.response.ApiResponse;
//import com.company.ragknowledgebase.model.dto.response.ResponseDtos;
//import com.company.ragknowledgebase.service.DocumentService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.BDDMockito.given;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(DocumentController.class)
//@DisplayName("DocumentController Tests")
//class DocumentControllerTest {
//
//    @Autowired MockMvc mockMvc;
//    @Autowired ObjectMapper objectMapper;
//    @MockBean  DocumentService documentService;
//
//    @Test
//    @DisplayName("GET /api/v1/documents - returns paginated list")
//    void getAllDocuments_returnsPagedResponse() throws Exception {
//        ResponseDtos.PagedResponse<ResponseDtos.DocumentResponse> page =
//                ResponseDtos.PagedResponse.<ResponseDtos.DocumentResponse>builder()
//                        .content(List.of(buildDocResponse()))
//                        .pageNo(0).pageSize(20).totalElements(1).totalPages(1).last(true)
//                        .build();
//
//        given(documentService.getAllDocuments(any(Pageable.class))).willReturn(page);
//
//        mockMvc.perform(get("/api/v1/documents").contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.content").isArray())
//                .andExpect(jsonPath("$.data.totalElements").value(1));
//    }
//
//    @Test
//    @DisplayName("POST /api/v1/documents/upload - uploads PDF successfully")
//    void uploadDocument_returnsCreated() throws Exception {
//        MockMultipartFile file = new MockMultipartFile(
//                "file", "test.pdf", "application/pdf", "PDF content".getBytes());
//
//        given(documentService.uploadDocument(any(), any())).willReturn(buildDocResponse());
//
//        mockMvc.perform(multipart("/api/v1/documents/upload").file(file))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.originalName").value("test.pdf"));
//    }
//
//    @Test
//    @DisplayName("GET /api/v1/documents/{id} - returns document")
//    void getDocumentById_returnsDocument() throws Exception {
//        UUID id = UUID.randomUUID();
//        given(documentService.getDocumentById(id)).willReturn(buildDocResponse(id));
//
//        mockMvc.perform(get("/api/v1/documents/{id}", id))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.id").value(id.toString()));
//    }
//
//    @Test
//    @DisplayName("DELETE /api/v1/documents/{id} - deletes document")
//    void deleteDocument_returns200() throws Exception {
//        UUID id = UUID.randomUUID();
//        mockMvc.perform(delete("/api/v1/documents/{id}", id))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true));
//    }
//
//    // ── helpers ─────────────────────────────────────
//    private ResponseDtos.DocumentResponse buildDocResponse() {
//        return buildDocResponse(UUID.randomUUID());
//    }
//
//    private ResponseDtos.DocumentResponse buildDocResponse(UUID id) {
//        return ResponseDtos.DocumentResponse.builder()
//                .id(id)
//                .name("abc123_test.pdf")
//                .originalName("test.pdf")
//                .contentType("application/pdf")
//                .fileSize(1024L)
//                .status("INDEXED")
//                .chunkCount(10)
//                .createdAt(LocalDateTime.now())
//                .build();
//    }
//}
