package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.report.ReportResponse;
import com.iseeyou.fortunetelling.entity.report.Report;
import com.iseeyou.fortunetelling.entity.user.User;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper extends BaseMapper {
    @Autowired
    public ReportMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        // Configure Report to ReportResponse mapping
        modelMapper.addMappings(new PropertyMap<Report, ReportResponse>() {
            @Override
            protected void configure() {
                // Map reporter fields
                map().setReporterId(source.getReporter().getId());

                // Map enum values to strings
                using((Converter<Object, String>) ctx ->
                    ctx.getSource() != null ? ctx.getSource().toString() : null)
                    .map(source.getTargetType(), destination.getTargetReportType());

                using((Converter<Object, String>) ctx ->
                    ctx.getSource() != null ? ctx.getSource().toString() : null)
                    .map(source.getStatus(), destination.getReportStatus());

                using((Converter<Object, String>) ctx ->
                    ctx.getSource() != null ? ctx.getSource().toString() : null)
                    .map(source.getActionType(), destination.getActionType());

                // Map report type name to string
                using((Converter<Object, String>) ctx ->
                    ctx.getSource() != null ? ((com.iseeyou.fortunetelling.entity.report.ReportType) ctx.getSource()).getName().toString() : null)
                    .map(source.getReportType(), destination.getReportType());

                // Map note field
                map(source.getNote(), destination.getNote());
            }
        });

        // Configure User to UserInfo mapping
        modelMapper.createTypeMap(User.class, ReportResponse.UserInfo.class)
            .addMapping(User::getId, ReportResponse.UserInfo::setId)
            .addMapping(User::getFullName, ReportResponse.UserInfo::setUsername)
            .addMapping(User::getAvatarUrl, ReportResponse.UserInfo::setAvatarUrl);

        // Configure nested UserInfo mappings for reporter and reported user
        modelMapper.addMappings(new PropertyMap<Report, ReportResponse>() {
            @Override
            protected void configure() {
                using((Converter<User, ReportResponse.UserInfo>) ctx -> {
                    if (ctx.getSource() == null) return null;
                    User user = ctx.getSource();
                    ReportResponse.UserInfo userInfo = new ReportResponse.UserInfo();
                    userInfo.setId(user.getId());
                    userInfo.setUsername(user.getFullName());
                    userInfo.setAvatarUrl(user.getAvatarUrl());
                    return userInfo;
                }).map(source.getReporter(), destination.getReporter());

                // Custom mapping for reported UserInfo
                using((Converter<User, ReportResponse.UserInfo>) ctx -> {
                    if (ctx.getSource() == null) return null;
                    User user = ctx.getSource();
                    ReportResponse.UserInfo userInfo = new ReportResponse.UserInfo();
                    userInfo.setId(user.getId());
                    userInfo.setUsername(user.getFullName());
                    userInfo.setAvatarUrl(user.getAvatarUrl());
                    return userInfo;
                }).map(source.getReportedUser(), destination.getReported());
            }
        });
    }
}
