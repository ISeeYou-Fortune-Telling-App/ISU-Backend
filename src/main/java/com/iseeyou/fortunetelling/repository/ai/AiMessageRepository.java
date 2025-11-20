package com.iseeyou.fortunetelling.repository.ai;

import com.iseeyou.fortunetelling.entity.ai.AiMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiMessageRepository extends JpaRepository<AiMessage, UUID> {
    Page<AiMessage> findAllByUser_Id(UUID userId, Pageable pageable);
}

