package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.servicepackage.ServicePackageResponse;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

                    // ServicePackageResponse không có trường categories nên bỏ qua phần này
                    // Chỉ map các trường cơ bản đã có

                    return destination;
                });
    }
}