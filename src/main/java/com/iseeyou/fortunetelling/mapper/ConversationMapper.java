package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.chat.ConversationResponse;
import com.iseeyou.fortunetelling.entity.chat.Conversation;
import com.iseeyou.fortunetelling.entity.chat.Message;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.repository.chat.MessageRepository;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ConversationMapper extends BaseMapper{

    private final MessageRepository messageRepository;

    @Autowired
    public ConversationMapper(ModelMapper modelMapper, MessageRepository messageRepository) {
        super(modelMapper);
        this.messageRepository = messageRepository;
    }

    @Override
    protected void configureCustomMappings() {
        // Map Conversation entity to ConversationResponse DTO
        modelMapper.typeMap(Conversation.class, ConversationResponse.class)
                .setPostConverter(context -> {
                    Conversation source = context.getSource();
                    ConversationResponse destination = context.getDestination();

                    // Map conversation ID
                    destination.setConversationId(source.getId());

                    // Map seer information
                    if (source.getBooking() != null &&
                            source.getBooking().getServicePackage() != null &&
                            source.getBooking().getServicePackage().getSeer() != null) {
                        User seer = source.getBooking().getServicePackage().getSeer();
                        destination.setSeerId(seer.getId());
                        destination.setSeerName(seer.getFullName());
                        destination.setSeerAvatarUrl(seer.getAvatarUrl());
                    }

                    // Map customer information
                    if (source.getBooking() != null && source.getBooking().getCustomer() != null) {
                        User customer = source.getBooking().getCustomer();
                        destination.setCustomerId(customer.getId());
                        destination.setCustomerName(customer.getFullName());
                        destination.setCustomerAvatarUrl(customer.getAvatarUrl());
                    }

                    // Map session times
                    destination.setSessionStartTime(source.getSessionStartTime());
                    destination.setSessionEndTime(source.getSessionEndTime());
                    destination.setSessionDurationMinutes(source.getSessionDurationMinutes());

                    // Map status
                    if (source.getStatus() != null) {
                        destination.setStatus(Constants.ConversationStatusEnum.valueOf(source.getStatus().getValue()));
                    }

                    // Map canceled info
                    destination.setSessionCanceledBy(source.getCanceledBy());

                    // Map created at
                    destination.setCreatedAt(source.getCreatedAt());

                    // Calculate unread counts
                    try {
                        List<Message> unreadMessages = messageRepository.findByConversationIdAndStatus(
                                source.getId(),
                                Constants.MessageStatusEnum.UNREAD
                        );

                        User seer = source.getBooking().getServicePackage().getSeer();
                        User customer = source.getBooking().getCustomer();

                        // Count unread for seer (messages sent by customer that seer hasn't read)
                        long seerUnreadCount = unreadMessages.stream()
                                .filter(msg -> msg.getSender().getId().equals(customer.getId()))
                                .count();
                        destination.setSeerUnreadCount((int) seerUnreadCount);

                        // Count unread for customer (messages sent by seer that customer hasn't read)
                        long customerUnreadCount = unreadMessages.stream()
                                .filter(msg -> msg.getSender().getId().equals(seer.getId()))
                                .count();
                        destination.setCustomerUnreadCount((int) customerUnreadCount);

                    } catch (Exception e) {
                        log.warn("Error calculating unread counts for conversation {}: {}", source.getId(), e.getMessage());
                        destination.setSeerUnreadCount(0);
                        destination.setCustomerUnreadCount(0);
                    }

                    // Get last message
                    try {
                        List<Message> messages = messageRepository.findByConversation_IdOrderByCreatedAtDesc(
                                source.getId(),
                                PageRequest.of(0, 1)
                        ).getContent();

                        if (!messages.isEmpty()) {
                            Message lastMessage = messages.get(0);
                            destination.setLastMessageContent(lastMessage.getTextContent());
                            destination.setLastMessageTime(lastMessage.getCreatedAt());
                        }
                    } catch (Exception e) {
                        log.warn("Error getting last message for conversation {}: {}", source.getId(), e.getMessage());
                    }

                    return destination;
                });
    }
}
