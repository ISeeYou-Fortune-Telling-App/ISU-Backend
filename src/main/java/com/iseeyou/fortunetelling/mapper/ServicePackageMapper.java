package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.report.SeerSimpleRating;
import com.iseeyou.fortunetelling.dto.response.servicepackage.ServicePackageResponse;
import com.iseeyou.fortunetelling.entity.servicepackage.ServicePackage;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.service.report.StatisticReportService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ServicePackageMapper extends BaseMapper {

    @Autowired
    private StatisticReportService reportService;

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

                    // Enrich with rating data from StatisticReportService
                    try {
                        LocalDate now = LocalDate.now();
                        SeerSimpleRating rating = reportService.getSeerSimpleRating(
                                user.getId().toString(),
                                now.getMonthValue(),
                                now.getYear()
                        );

                        if (rating != null) {
                            seerInfo.setAvgRating(rating.getAvgRating() != null ? rating.getAvgRating().doubleValue() : null);
                            seerInfo.setTotalRates(rating.getTotalRates() != null ? rating.getTotalRates() : 0);

                            log.debug("Enriched seer info with rating: seerId={}, avgRating={}, totalRates={}",
                                    user.getId(), seerInfo.getAvgRating(), seerInfo.getTotalRates());
                            } else {
                                seerInfo.setAvgRating(null);
                                seerInfo.setTotalRates(0);
                            }
                    } catch (Exception e) {
                        log.error("Failed to get rating from Report Service for seer: {}", user.getId(), e);
                        seerInfo.setAvgRating(null);
                        seerInfo.setTotalRates(0);
                    }

                    return seerInfo;
                }).map(source.getSeer(), destination.getSeer());
                
                // Custom mapping for categories - map all categories from packageCategories
                using((Converter<ServicePackage, List<ServicePackageResponse.CategoryInfo>>) ctx -> {
                    ServicePackage pkg = ctx.getSource();
                    if (pkg == null || pkg.getPackageCategories() == null || pkg.getPackageCategories().isEmpty()) {
                        return null;
                    }
                    
                    return pkg.getPackageCategories().stream()
                            .map(pc -> ServicePackageResponse.CategoryInfo.builder()
                                    .id(pc.getKnowledgeCategory().getId())
                                    .name(pc.getKnowledgeCategory().getName())
                                    .description(pc.getKnowledgeCategory().getDescription())
                                    .build())
                            .collect(Collectors.toList());
                }).map(source, destination.getCategories());
            }
        });
    }
}