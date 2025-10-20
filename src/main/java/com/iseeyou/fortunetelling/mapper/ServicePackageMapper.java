package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.servicepackage.ServicePackageResponse;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ServicePackageMapper extends BaseMapper {
    @Autowired
    public ServicePackageMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        // Configure User to SeerInfo mapping
        modelMapper.createTypeMap(User.class, ServicePackageResponse.SeerInfo.class)
            .addMapping(User::getId, ServicePackageResponse.SeerInfo::setId)
            .addMapping(User::getFullName, ServicePackageResponse.SeerInfo::setFullName)
            .addMapping(User::getAvatarUrl, ServicePackageResponse.SeerInfo::setAvatarUrl);

        // Configure ServicePackage to ServicePackageResponse mapping
        modelMapper.addMappings(new PropertyMap<ServicePackage, ServicePackageResponse>() {
            @Override
            protected void configure() {
                // Custom mapping for seer UserInfo - id, name, avatar, avgRating, totalRates
                using((Converter<User, ServicePackageResponse.SeerInfo>) ctx -> {
                    if (ctx.getSource() == null) return null;
                    User user = ctx.getSource();
                    ServicePackageResponse.SeerInfo seerInfo = new ServicePackageResponse.SeerInfo();
                    seerInfo.setId(user.getId());
                    seerInfo.setFullName(user.getFullName());
                    seerInfo.setAvatarUrl(user.getAvatarUrl());

                    // Map SeerProfile rating data if available
                    if (user.getSeerProfile() != null) {
                        seerInfo.setAvgRating(user.getSeerProfile().getAvgRating());
                        seerInfo.setTotalRates(user.getSeerProfile().getTotalRates());
                    }

                    return seerInfo;
                }).map(source.getSeer(), destination.getSeer());
                
                // Custom mapping for category - get first category from packageCategories
                using((Converter<ServicePackage, Constants.ServiceCategoryEnum>) ctx -> {
                    ServicePackage pkg = ctx.getSource();
                    if (pkg == null || pkg.getPackageCategories() == null || pkg.getPackageCategories().isEmpty()) {
                        return null;
                    }
                    
                    return pkg.getPackageCategories().stream()
                            .findFirst()
                            .map(pc -> {
                                String categoryName = pc.getKnowledgeCategory().getName();
                                try {
                                    return Constants.ServiceCategoryEnum.get(categoryName);
                                } catch (IllegalArgumentException e) {
                                    log.warn("Unknown category name: {}", categoryName);
                                    return null;
                                }
                            })
                            .orElse(null);
                }).map(source, destination.getCategory());
            }
        });
    }
}