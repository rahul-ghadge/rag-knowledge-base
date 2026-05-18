package com.company.ragknowledgebase.service.impl;

import com.company.ragknowledgebase.config.AppProperties;
import com.company.ragknowledgebase.constant.AppConstants;
import com.company.ragknowledgebase.exception.ResourceNotFoundException;
import com.company.ragknowledgebase.model.dto.request.ChatRequest;
import com.company.ragknowledgebase.model.dto.request.SearchRequest;
import com.company.ragknowledgebase.model.dto.response.ResponseDtos;
import com.company.ragknowledgebase.model.entity.ChatMessage;
import com.company.ragknowledgebase.model.entity.ChatSession;
import com.company.ragknowledgebase.repository.ChatMessageRepository;
import com.company.ragknowledgebase.repository.ChatSessionRepository;
import com.company.ragknowledgebase.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * RAG service implementation using Spring AI ChatClient and pgvector similarity search.
 * Flow: question → semantic retrieval → prompt augmentation → LLM response → persist.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RagServiceImpl implements RagService {

    private static final String SYSTEM_PROMPT = """
            You are an expert internal knowledge assistant. Your role is to answer questions
            accurately using ONLY the provided context from the company knowledge base.
            
            Guidelines:
            - Base answers strictly on the provided context
            - If context is insufficient, say so clearly
            - Cite document sources when possible
            - Be concise but comprehensive
            - Use professional, clear language
            - If asked about topics not in the context, acknowledge the limitation
            """;

    private final ChatClient.Builder    chatClientBuilder;
    private final VectorStore           vectorStore;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AppProperties         appProperties;

    @Override
    public ResponseDtos.ChatResponse chat(ChatRequest request) {
        log.debug("Processing chat request: question={}", request.getQuestion());

        // 1. Get or create session
        ChatSession session = resolveSession(request);

        // 2. Semantic retrieval
        int    topK      = request.getTopK() != null ? request.getTopK() : appProperties.getRag().getTopKResults();
        double threshold = request.getSimilarityThreshold() != null
                ? request.getSimilarityThreshold()
                : appProperties.getRag().getSimilarityThreshold();

        List<Document> relevantDocs = vectorStore.similaritySearch(
                org.springframework.ai.vectorstore.SearchRequest.builder().query(request.getQuestion())
                        .topK(topK)
                        .similarityThreshold(threshold).build());

        log.debug("Retrieved {} relevant chunks for question", relevantDocs.size());

        // 3. Build augmented prompt
        String context = buildContext(relevantDocs);
        String augmentedPrompt = String.format("""
                Context from knowledge base:
                %s
                
                Question: %s
                
                Please provide a helpful, accurate answer based on the context above.
                """, context, request.getQuestion());

        // 4. Load conversation history for multi-turn
        List<Message> history = buildConversationHistory(session.getId());

        // 5. Call LLM via Spring AI ChatClient
        ChatClient chatClient = chatClientBuilder.build();
        String answer = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .messages(history)
                .user(augmentedPrompt)
                .call()
                .content();

        // 6. Build source references
        List<ResponseDtos.SourceInfo>       sources    = buildSourceInfos(relevantDocs);
        List<ChatMessage.SourceReference>   sourceRefs = buildSourceRefs(relevantDocs);

        // 7. Persist user message
        ChatMessage userMsg = ChatMessage.builder()
                .session(session)
                .role(AppConstants.ROLE_USER)
                .content(request.getQuestion())
                .build();
        chatMessageRepository.save(userMsg);

        // 8. Persist assistant message
        ChatMessage assistantMsg = ChatMessage.builder()
                .session(session)
                .role(AppConstants.ROLE_ASSISTANT)
                .content(answer)
                .modelUsed("gpt-4o")
                .sources(sourceRefs)
                .build();
        chatMessageRepository.save(assistantMsg);

        return ResponseDtos.ChatResponse.builder()
                .messageId(assistantMsg.getId())
                .sessionId(session.getId())
                .question(request.getQuestion())
                .answer(answer)
                .sources(sources)
                .modelUsed("gpt-4o")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDtos.SearchResponse semanticSearch(SearchRequest request) {
        List<Document> docs = vectorStore.similaritySearch(
                org.springframework.ai.vectorstore.SearchRequest.builder().query(request.getQuery())
                        .topK(request.getTopK())
                        .similarityThreshold(request.getSimilarityThreshold()).build());

        List<ResponseDtos.SearchResult> results = docs.stream()
                .map(doc -> ResponseDtos.SearchResult.builder()
                        .documentId(String.valueOf(doc.getMetadata().get("documentId")))
                        .content(doc.getText())
                        .chunkIndex((Integer) doc.getMetadata().getOrDefault("chunkIndex", 0))
                        .build())
                .toList();

        return ResponseDtos.SearchResponse.builder()
                .query(request.getQuery())
                .results(results)
                .totalResults(results.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseDtos.ChatSessionResponse> getSessionsByUser(String userIdentifier) {
        return chatSessionRepository.findByUserIdentifierAndActiveTrue(userIdentifier).stream()
                .map(s -> ResponseDtos.ChatSessionResponse.builder()
                        .id(s.getId())
                        .sessionName(s.getSessionName())
                        .userIdentifier(s.getUserIdentifier())
                        .createdAt(s.getCreatedAt())
                        .updatedAt(s.getUpdatedAt())
                        .active(s.getActive())
                        .messageCount((int) chatSessionRepository.countMessagesBySessionId(s.getId()))
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseDtos.ChatResponse> getSessionHistory(UUID sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(msg -> ResponseDtos.ChatResponse.builder()
                        .messageId(msg.getId())
                        .sessionId(sessionId)
                        .answer(msg.getContent())
                        .build())
                .toList();
    }

    @Override
    public void deleteSession(UUID sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatSession", "id", sessionId));
        session.setActive(false);
        chatSessionRepository.save(session);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private ChatSession resolveSession(ChatRequest request) {
        if (request.getSessionId() != null) {
            return chatSessionRepository.findById(request.getSessionId())
                    .orElseGet(() -> createNewSession(request.getUserIdentifier()));
        }
        return createNewSession(request.getUserIdentifier());
    }

    private ChatSession createNewSession(String userIdentifier) {
        ChatSession session = ChatSession.builder()
                .userIdentifier(userIdentifier)
                .sessionName("Session " + System.currentTimeMillis())
                .active(true)
                .build();
        return chatSessionRepository.save(session);
    }

    private String buildContext(List<Document> docs) {
        return docs.stream()
                .map(doc -> "---\n" + doc.getText())
                .collect(Collectors.joining("\n\n"));
    }

    private List<Message> buildConversationHistory(UUID sessionId) {
        List<ChatMessage> msgs = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return msgs.stream()
                .map(msg -> switch (msg.getRole()) {
                    case AppConstants.ROLE_USER      -> (Message) new UserMessage(msg.getContent());
                    case AppConstants.ROLE_ASSISTANT -> (Message) new AssistantMessage(msg.getContent());
                    default                          -> (Message) new SystemMessage(msg.getContent());
                })
                .collect(Collectors.toList());
    }

    private List<ResponseDtos.SourceInfo> buildSourceInfos(List<Document> docs) {
        return docs.stream()
                .map(doc -> ResponseDtos.SourceInfo.builder()
                        .documentId(String.valueOf(doc.getMetadata().get("documentId")))
                        .excerpt(truncate(doc.getText(), 200))
                        .build())
                .toList();
    }

    private List<ChatMessage.SourceReference> buildSourceRefs(List<Document> docs) {
        return docs.stream()
                .map(doc -> ChatMessage.SourceReference.builder()
                        .documentId(String.valueOf(doc.getMetadata().get("documentId")))
                        .chunkContent(truncate(doc.getText(), 200))
                        .build())
                .toList();
    }

    private String truncate(String text, int maxLen) {
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }
}
