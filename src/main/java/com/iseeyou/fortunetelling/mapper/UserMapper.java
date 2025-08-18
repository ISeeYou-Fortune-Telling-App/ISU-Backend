package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.user.CustomerProfileResponse;
import com.iseeyou.fortunetelling.dto.response.user.SeerProfileResponse;
import com.iseeyou.fortunetelling.dto.response.user.UserResponse;
import com.iseeyou.fortunetelling.entity.user.User;
import com.iseeyou.fortunetelling.util.Constants;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserMapper extends BaseMapper<User, UserResponse> {

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
                            return modelMapper.map(user.getSeerProfile(), SeerProfileResponse.class);
                        }
                        return null;
                    }).map(src -> src, UserResponse::setProfile);
                });
    }

    @Override
    protected Class<UserResponse> getResponseType() {
        return UserResponse.class;
    }
}
