package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.servicepackage.ServicePackageResponse;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SimpleMapper extends BaseMapper {
    @Autowired
    public SimpleMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        // Basic mapping for ServicePackage -> ServicePackageResponse
        // Keep it simple to avoid null pointer issues during initialization
        modelMapper.createTypeMap(ServicePackage.class, ServicePackageResponse.class)
                .addMappings(mapper -> {
                    // Only map basic fields that are guaranteed to exist
                    mapper.map(ServicePackage::getPackageTitle, ServicePackageResponse::setPackageTitle);
                    mapper.map(ServicePackage::getPackageContent, ServicePackageResponse::setPackageContent);
                    mapper.map(ServicePackage::getImageUrl, ServicePackageResponse::setImageUrl);
                    mapper.map(ServicePackage::getDurationMinutes, ServicePackageResponse::setDurationMinutes);
                    mapper.map(ServicePackage::getPrice, ServicePackageResponse::setPrice);
                    mapper.map(ServicePackage::getCreatedAt, ServicePackageResponse::setCreatedAt);
                    mapper.map(ServicePackage::getUpdatedAt, ServicePackageResponse::setUpdatedAt);
                });
    }
}
