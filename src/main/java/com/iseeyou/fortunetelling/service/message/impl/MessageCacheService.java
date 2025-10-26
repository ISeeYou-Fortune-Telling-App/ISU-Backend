package com.iseeyou.fortunetelling.service.message.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iseeyou.fortunetelling.dto.Internal.DeletedMessageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String DELETED_MESSAGE_PREFIX = "deleted_messages:";
    private static final int UNDO_EXPIRATION_SECONDS = 30;

    public void cacheDeletedMessages(UUID userId, UUID conversationId, List<UUID> messageIds) {
        String key = buildCacheKey(userId, conversationId);

        DeletedMessageInfo info = DeletedMessageInfo.builder()
                .userId(userId)
                .conversationId(conversationId)
                .messageIds(messageIds)
                .deletedAt(LocalDateTime.now())
                .build();

        try {
            String jsonValue = objectMapper.writeValueAsString(info);
            // Set with TTL = 30 seconds
            redisTemplate.opsForValue().set(key, jsonValue, UNDO_EXPIRATION_SECONDS, TimeUnit.SECONDS);
            log.debug("Cached deleted messages for user {} in conversation {}: {} messages",
                    userId, conversationId, messageIds.size());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize DeletedMessageInfo", e);
        }
    }

    public Optional<DeletedMessageInfo> getDeletedMessages(UUID userId, UUID conversationId) {
        String key = buildCacheKey(userId, conversationId);

        try {
            String jsonValue = (String) redisTemplate.opsForValue().get(key);
            if (jsonValue != null) {
                DeletedMessageInfo info = objectMapper.readValue(jsonValue, DeletedMessageInfo.class);
                return Optional.of(info);
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize DeletedMessageInfo", e);
        }

        return Optional.empty();
    }

    public boolean canUndoDelete(UUID userId, UUID conversationId) {
        String key = buildCacheKey(userId, conversationId);
        return redisTemplate.hasKey(key);
    }

    public void removeCachedMessages(UUID userId, UUID conversationId) {
        String key = buildCacheKey(userId, conversationId);
        redisTemplate.delete(key);
        log.debug("Removed cached deleted messages for user {} in conversation {}", userId, conversationId);
    }

    public Long getRemainingUndoTime(UUID userId, UUID conversationId) {
        String key = buildCacheKey(userId, conversationId);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return (ttl != null && ttl > 0) ? ttl : 0L;
    }

    public void appendDeletedMessages(UUID userId, UUID conversationId, List<UUID> additionalMessageIds) {
        Optional<DeletedMessageInfo> existingInfo = getDeletedMessages(userId, conversationId);

        if (existingInfo.isPresent()) {
            DeletedMessageInfo info = existingInfo.get();
            // Merge message IDs
            Set<UUID> mergedIds = new HashSet<>(info.getMessageIds());
            mergedIds.addAll(additionalMessageIds);
            info.setMessageIds(new ArrayList<>(mergedIds));

            // Re-cache with refreshed TTL
            try {
                String key = buildCacheKey(userId, conversationId);
                String jsonValue = objectMapper.writeValueAsString(info);
                redisTemplate.opsForValue().set(key, jsonValue, UNDO_EXPIRATION_SECONDS, TimeUnit.SECONDS);
            } catch (JsonProcessingException e) {
                log.error("Failed to append deleted messages", e);
            }
        } else {
            // No existing cache, create new
            cacheDeletedMessages(userId, conversationId, additionalMessageIds);
        }
    }

    private String buildCacheKey(UUID userId, UUID conversationId) {
        return DELETED_MESSAGE_PREFIX + userId + ":" + conversationId;
    }
}
