package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.servicepackage.ServicePackageResponse;
import com.iseeyou.fortunetelling.dto.response.servicepackage.ServiceReviewResponse;
import com.iseeyou.fortunetelling.entity.ServicePackage;
import com.iseeyou.fortunetelling.entity.ServiceReview;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ServicePackageMapper extends BaseMapper {
    @Autowired
    public ServicePackageMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        // Map ServicePackage entity to ServicePackageResponse DTO
        modelMapper.typeMap(ServicePackage.class, ServicePackageResponse.class)
                .setPostConverter(context -> {
                    ServicePackage source = context.getSource();
                    ServicePackageResponse destination = context.getDestination();

                    // Map categories from PackageCategory junction table
                    if (source.getPackageCategories() != null) {
                        Set<String> categoryNames = source.getPackageCategories()
                                .stream()
                                .map(pc -> pc.getKnowledgeCategory().getName())
                                .collect(Collectors.toSet());
                        destination.setCategories(categoryNames);
                    } else {
                        destination.setCategories(Set.of());
                    }

                    return destination;
                });

        // Map ServiceReview entity to ServiceReviewResponse DTO
        modelMapper.typeMap(ServiceReview.class, ServiceReviewResponse.class)
                .setPostConverter(context -> {
                    ServiceReview source = context.getSource();
                    ServiceReviewResponse destination = context.getDestination();

                    // Map comment content
                    destination.setContent(source.getComment());

                    // Map user information
                    if (source.getUser() != null) {
                        destination.setUserId(source.getUser().getId());
                        destination.setUserName(source.getUser().getFullName());
                        destination.setUserAvatarUrl(source.getUser().getAvatarUrl());
                    }

                    return destination;
                });
    }
}