package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.account.SimpleSeerCardResponse;
import com.iseeyou.fortunetelling.dto.response.user.CustomerProfileResponse;
import com.iseeyou.fortunetelling.dto.response.user.UserResponse;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.service.user.SeerStatsService;
import com.iseeyou.fortunetelling.util.Constants;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper extends BaseMapper {

    @Autowired
    private SeerStatsService seerStatsService;

    @Autowired
    public UserMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        modelMapper.typeMap(User.class, UserResponse.class)
                .addMappings(mapper -> {
                    mapper.using(ctx -> {
                        User user = (User) ctx.getSource();

                        if (user.getRole() == Constants.RoleEnum.CUSTOMER && user.getCustomerProfile() != null) {
                            return modelMapper.map(user.getCustomerProfile(), CustomerProfileResponse.class);
                        } else if (user.getRole() == Constants.RoleEnum.SEER && user.getSeerProfile() != null) {
                            // Sử dụng SeerStatsService để lấy profile với thống kê
                            return seerStatsService.enrichSeerProfile(user);
                        }

                        return null;
                    }).map(src -> src, (dest, value) -> dest.setProfile(value));
                });

        // Mapping for User to SimpleSeerCardResponse
        modelMapper.typeMap(User.class, SimpleSeerCardResponse.class)
                .addMappings(mapper -> {
                    mapper.map(User::getId, SimpleSeerCardResponse::setId);
                    mapper.map(User::getFullName, SimpleSeerCardResponse::setName);
                    mapper.map(User::getAvatarUrl, SimpleSeerCardResponse::setAvatarUrl);
                    mapper.map(User::getProfileDescription, SimpleSeerCardResponse::setProfileDescription);

                    // Custom mapping for rating
                    mapper.using(ctx -> {
                        User user = (User) ctx.getSource();
                        return user.getSeerProfile() != null ? user.getSeerProfile().getAvgRating() : 0.0;
                    }).map(src -> src, SimpleSeerCardResponse::setRating);

                    // Custom mapping for totalRates
                    mapper.using(ctx -> {
                        User user = (User) ctx.getSource();
                        return user.getSeerProfile() != null ? user.getSeerProfile().getTotalRates().doubleValue() : 0.0;
                    }).map(src -> src, SimpleSeerCardResponse::setTotalRates);

                    // Custom mapping for specialities
                    mapper.using(ctx -> {
                        User user = (User) ctx.getSource();
                        return user.getSeerSpecialities().stream()
                                .map(ss -> ss.getKnowledgeCategory().getName())
                                .toList();
                    }).map(src -> src, SimpleSeerCardResponse::setSpecialities);
                });
    }
}
