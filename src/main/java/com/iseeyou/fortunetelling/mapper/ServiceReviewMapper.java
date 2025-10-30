package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.servicepackage.ServiceReviewResponse;
import com.iseeyou.fortunetelling.entity.servicepackage.ServiceReview;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ServiceReviewMapper extends BaseMapper {

    @Autowired
    public ServiceReviewMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        modelMapper.typeMap(ServiceReview.class, ServiceReviewResponse.class)
                .setPostConverter(context -> {
                    ServiceReview src = context.getSource();
                    ServiceReviewResponse dest = context.getDestination();

                    // Map id to reviewId
                    if (src.getId() != null) {
                        dest.setReviewId(src.getId());
                    }

                    if (src.getUser() != null) {
                        dest.setUser(ServiceReviewResponse.UserInfo.builder()
                                .userId(src.getUser().getId())
                                .fullName(src.getUser().getFullName())
                                .avatarUrl(src.getUser().getAvatarUrl())
                                .build());
                    }

                    if (src.getParentReview() != null) {
                        dest.setParentReviewId(src.getParentReview().getId());
                    }

                    return dest;
                });
    }
}
