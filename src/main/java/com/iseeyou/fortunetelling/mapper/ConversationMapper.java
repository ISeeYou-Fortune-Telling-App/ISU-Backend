package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.converstation.ChatSessionResponse;
import com.iseeyou.fortunetelling.entity.chat.Conversation;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
                    }

                    // Map customer information
                    if (source.getBooking() != null && source.getBooking().getCustomer() != null) {
                        destination.setCustomerName(source.getBooking().getCustomer().getFullName());
                    }

                    // Map session times
                    destination.setSessionStartTime(source.getSessionStartTime());
                    destination.setSessionEndTime(source.getSessionEndTime());
                    destination.setSessionDurationMinutes(source.getSessionDurationMinutes());

                    // Map status (Enum -> String via BaseMapper converter)
                    if (source.getStatus() != null) {
                        destination.setStatus(Constants.ConversationStatusEnum.valueOf(source.getStatus().getValue()));
                    }

                    // Map service package name
                    if (source.getBooking() != null && source.getBooking().getServicePackage() != null) {
                        destination.setServicePackageName(source.getBooking().getServicePackage().getPackageTitle());
                    }

                    // Map created at
                    destination.setCreatedAt(source.getCreatedAt());

                    return destination;
                });
    }
}
