package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.user.UserResponse;
import com.iseeyou.fortunetelling.entity.User;
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
        // For additional custom mappings, if needed
        // Example: create new fields, map complex types or skip certain fields
    }

    @Override
    protected Class<UserResponse> getResponseType() {
        return UserResponse.class;
    }
}
