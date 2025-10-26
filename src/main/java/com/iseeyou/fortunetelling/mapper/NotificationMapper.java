package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.notification.NotificationResponse;
import com.iseeyou.fortunetelling.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationMapper extends BaseMapper {
    @Autowired
    public NotificationMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        modelMapper.typeMap(Notification.class, NotificationResponse.class)
                .setPostConverter(context -> {
                    Notification source = context.getSource();
                    NotificationResponse destination = context.getDestination();

                    if (source.getRecipient() != null) {
                        NotificationResponse.RecipientInfo recipientInfo = NotificationResponse.RecipientInfo.builder()
                                .fullName(source.getRecipient().getFullName())
                                .avatarUrl(source.getRecipient().getAvatarUrl())
                                .email(source.getRecipient().getEmail())
                                .build();
                        destination.setRecipient(recipientInfo);
                    }

                    return destination;
                });
    }
}
