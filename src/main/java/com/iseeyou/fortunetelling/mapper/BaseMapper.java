package com.iseeyou.fortunetelling.mapper;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public abstract class BaseMapper<E, D> {

    protected final ModelMapper modelMapper;

    @Autowired
    protected BaseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.registerCommonConverters();
        this.configureCustomMappings();
    }

    private void registerCommonConverters() {
        // UUID -> String
        modelMapper.addConverter(new AbstractConverter<UUID, String>() {
            @Override
            protected String convert(UUID source) {
                return source != null ? source.toString() : null;
            }
        });

        // Enum -> String
        modelMapper.addConverter(new AbstractConverter<Enum<?>, String>() {
            @Override
            protected String convert(Enum<?> source) {
                return source != null ? source.name() : null;
            }
        });

        // LocalDateTime -> String
        modelMapper.addConverter(new AbstractConverter<LocalDateTime, String>() {
            @Override
            protected String convert(LocalDateTime source) {
                if (source == null) return null;
                return source.format(DateTimeFormatter.ISO_DATE_TIME);
            }
        });

        // EnumSet -> List<String>
        modelMapper.addConverter(new AbstractConverter<EnumSet<?>, List<String>>() {
            @Override
            protected List<String> convert(EnumSet<?> source) {
                if (source == null) return null;
                return source.stream()
                        .map(Enum::name)
                        .collect(Collectors.toList());
            }
        });
    }

    protected abstract void configureCustomMappings();

    public D toResponse(E entity) {
        if (entity == null) return null;
        return modelMapper.map(entity, getResponseType());
    }

    protected abstract Class<D> getResponseType();
}