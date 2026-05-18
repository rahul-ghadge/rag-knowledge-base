package com.company.ragknowledgebase.repository;

import com.company.ragknowledgebase.model.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository for ChatMessage entity.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
}
