package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.chat.ChatMessageResponse;
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

                    // Map customer info from conversation booking
                    if (source.getConversation() != null &&
                        source.getConversation().getBooking() != null &&
                        source.getConversation().getBooking().getCustomer() != null) {
                        var customer = source.getConversation().getBooking().getCustomer();
                        destination.setCustomerId(customer.getId());
                        destination.setCustomerName(customer.getFullName());
                        destination.setCustomerAvatar(customer.getAvatarUrl());
                    }

                    // Map seer info from conversation booking service package
                    if (source.getConversation() != null &&
                        source.getConversation().getBooking() != null &&
                        source.getConversation().getBooking().getServicePackage() != null &&
                        source.getConversation().getBooking().getServicePackage().getSeer() != null) {
                        var seer = source.getConversation().getBooking().getServicePackage().getSeer();
                        destination.setSeerId(seer.getId());
                        destination.setSeerName(seer.getFullName());
                        destination.setSeerAvatar(seer.getAvatarUrl());
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
