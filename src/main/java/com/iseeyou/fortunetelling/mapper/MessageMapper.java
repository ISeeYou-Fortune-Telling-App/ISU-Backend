package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.chat.session.ChatMessageResponse;
import com.iseeyou.fortunetelling.entity.chat.Message;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageMapper extends BaseMapper {

    @Autowired
    public MessageMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        modelMapper.typeMap(Message.class, ChatMessageResponse.class)
                .setPostConverter(context -> {
                    Message source = context.getSource();
                    ChatMessageResponse destination = context.getDestination();

                    destination.setId(source.getId());
                    destination.setConversationId(source.getConversation().getId());

                    // Check conversation type to determine how to map customer/seer info
                    if (source.getConversation().getType() == Constants.ConversationTypeEnum.ADMIN_CHAT) {
                        // Admin chat: map admin and target user
                        if (source.getConversation().getAdmin() != null) {
                            var admin = source.getConversation().getAdmin();
                            destination.setSeerId(admin.getId());
                            destination.setSeerName(admin.getFullName());
                            destination.setSeerAvatar(admin.getAvatarUrl());
                        }

                        if (source.getConversation().getTargetUser() != null) {
                            var targetUser = source.getConversation().getTargetUser();
                            destination.setCustomerId(targetUser.getId());
                            destination.setCustomerName(targetUser.getFullName());
                            destination.setCustomerAvatar(targetUser.getAvatarUrl());
                        }
                    } else {
                        // Booking session: map customer and seer from booking
                        if (source.getConversation() != null &&
                            source.getConversation().getBooking() != null &&
                            source.getConversation().getBooking().getCustomer() != null) {
                            var customer = source.getConversation().getBooking().getCustomer();
                            destination.setCustomerId(customer.getId());
                            destination.setCustomerName(customer.getFullName());
                            destination.setCustomerAvatar(customer.getAvatarUrl());
                        }

                        if (source.getConversation() != null &&
                            source.getConversation().getBooking() != null &&
                            source.getConversation().getBooking().getServicePackage() != null &&
                            source.getConversation().getBooking().getServicePackage().getSeer() != null) {
                            var seer = source.getConversation().getBooking().getServicePackage().getSeer();
                            destination.setSeerId(seer.getId());
                            destination.setSeerName(seer.getFullName());
                            destination.setSeerAvatar(seer.getAvatarUrl());
                        }
                    }

                    destination.setTextContent(source.getTextContent());
                    destination.setImageUrl(source.getImageUrl());
                    destination.setVideoUrl(source.getVideoUrl());
                    destination.setMessageType(Constants.MessageTypeEnum.valueOf(source.getMessageType()));

                    // Map status fields
                    destination.setStatus(source.getStatus());
                    destination.setDeletedBy(source.getDeletedBy());

                    // Set sender ID - frontend will compare with current user ID to determine sentByMe
                    if (source.getSender() != null) {
                        destination.setSenderId(source.getSender().getId());
                    }

                    destination.setCreatedAt(source.getCreatedAt());

                    return destination;
                });
    }
}
