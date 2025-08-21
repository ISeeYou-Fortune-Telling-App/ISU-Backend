package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.certificate.CertificateResponse;
import com.iseeyou.fortunetelling.entity.Certificate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CertificateMapper extends BaseMapper {
    @Autowired
    public CertificateMapper(ModelMapper modelMapper) {
        super(modelMapper);
    }

    @Override
    protected void configureCustomMappings() {
        modelMapper.typeMap(Certificate.class, CertificateResponse.class)
                .setPostConverter(context -> {
                    Certificate source = context.getSource();
                    CertificateResponse destination = context.getDestination();

                    if (source.getCertificateCategories() != null) {
                        Set<String> categoryNames = source.getCertificateCategories()
                                .stream()
                                .map(cc -> cc.getKnowledgeCategory().getName())
                                .collect(Collectors.toSet());
                        destination.setCategories(categoryNames);
                    } else {
                        destination.setCategories(Set.of());
                    }

                    if (source.getSeer() != null) {
                        destination.setSeerName(source.getSeer().getFullName());
                    }

                    return destination;
                });
    }
}
