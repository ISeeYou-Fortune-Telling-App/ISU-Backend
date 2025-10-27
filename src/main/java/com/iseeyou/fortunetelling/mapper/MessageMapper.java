package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.ChatMessageResponse;
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

                    if (source.getSender() != null) {
                        destination.setSenderId(source.getSender().getId());
                        destination.setSenderName(source.getSender().getFullName());
                        destination.setSenderAvatar(source.getSender().getAvatarUrl());
                    }

                    destination.setTextContent(source.getTextContent());
                    destination.setImageUrl(source.getImageUrl());
                    destination.setVideoUrl(source.getVideoUrl());
                    destination.setMessageType(Constants.MessageTypeEnum.valueOf(source.getMessageType()));
                    destination.setIsRead(source.getIsRead());
                    destination.setCreatedAt(source.getCreatedAt());

                    return destination;
                });
    }
}
