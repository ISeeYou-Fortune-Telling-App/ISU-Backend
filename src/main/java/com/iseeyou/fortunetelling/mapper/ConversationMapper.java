package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.converstation.ChatSessionResponse;
import com.iseeyou.fortunetelling.entity.Conversation;
import com.iseeyou.fortunetelling.entity.Message;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;

import static com.iseeyou.fortunetelling.helper.Message.getMessageContent.getMessage;

@Component
@Slf4j
public class ConversationMapper extends BaseMapper{

    @Autowired
    public ConversationMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        // Map Conversation entity to ChatSessionResponse DTO
        modelMapper.typeMap(Conversation.class, ChatSessionResponse.class)
                .setPostConverter(context -> {
                    Conversation source = context.getSource();
                    ChatSessionResponse destination = context.getDestination();

                    // Map conversation ID
                    destination.setConversationId(source.getId());

                    // Map booking ID
                    if (source.getBooking() != null) {
                        destination.setBookingId(source.getBooking().getId());
                    }

                    // Map seer information
                    if (source.getBooking() != null &&
                            source.getBooking().getServicePackage() != null &&
                            source.getBooking().getServicePackage().getSeer() != null) {
                        destination.setSeerName(source.getBooking().getServicePackage().getSeer().getFullName());
                        destination.setSeerAvatar(source.getBooking().getServicePackage().getSeer().getAvatarUrl());
                    }

                    // Map customer information
                    if (source.getBooking() != null && source.getBooking().getCustomer() != null) {
                        destination.setCustomerName(source.getBooking().getCustomer().getFullName());
                        destination.setCustomerAvatar(source.getBooking().getCustomer().getAvatarUrl());
                    }

                    // Map session times
                    destination.setSessionStartTime(source.getSessionStartTime());
                    destination.setSessionEndTime(source.getSessionEndTime());
                    destination.setSessionDurationMinutes(source.getSessionDurationMinutes());
                    destination.setExtendedMinutes(source.getExtendedMinutes());

                    // Map status (Enum -> String via BaseMapper converter)
                    if (source.getStatus() != null) {
                        destination.setStatus(Constants.ConversationStatusEnum.valueOf(source.getStatus().getValue()));
                    }

                    // Map service package name
                    if (source.getBooking() != null && source.getBooking().getServicePackage() != null) {
                        destination.setPackageTitle(source.getBooking().getServicePackage().getPackageTitle());
                    }

                    // Map last message info
                    if (source.getMessages() != null && !source.getMessages().isEmpty()) {
                        Message lastMessage = source.getMessages().stream()
                                .filter(msg -> !msg.getIsDeleted() && !msg.getIsRemoved())
                                .max(Comparator.comparing(Message::getCreatedAt))
                                .orElse(null);

                        if (lastMessage != null) {
                            destination.setLastMessageContent(getMessage(lastMessage));
                            destination.setLastMessageSenderName(
                                    lastMessage.getSender() != null ? lastMessage.getSender().getFullName() : ""
                            );
                            destination.setLastMessageSenderId(
                                    lastMessage.getSender() != null ? lastMessage.getSender().getId() : null
                            );
                            destination.setLastMessageTime(lastMessage.getCreatedAt());
                        }
                        // Count total non-deleted messages
                        long messageCount = source.getMessages().stream()
                                .filter(msg -> !msg.getIsDeleted() && !msg.getIsRemoved())
                                .count();
                        destination.setTotalMessages((int) messageCount);
                    } else {
                        destination.setTotalMessages(0);
                    }

                    // Map created at
                    destination.setCreatedAt(source.getCreatedAt());

                    return destination;
                });
    }
}
