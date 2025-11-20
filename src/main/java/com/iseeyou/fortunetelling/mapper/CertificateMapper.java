package com.iseeyou.fortunetelling.mapper;

import com.iseeyou.fortunetelling.dto.response.certificate.CertificateResponse;
import com.iseeyou.fortunetelling.entity.certificate.Certificate;
import org.hibernate.Hibernate;
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
                        // seer id is usually safe to access from a proxy
                        try {
                            if (source.getSeer().getId() != null) {
                                destination.setSeerId(source.getSeer().getId().toString());
                            }
                        } catch (Exception ignored) {
                        }

                        // Try to read name/avatar — if proxy not initialized this may throw, so catch and ignore
                        try {
                            if (Hibernate.isInitialized(source.getSeer())) {
                                destination.setSeerName(source.getSeer().getFullName());
                                destination.setSeerAvatar(source.getSeer().getAvatarUrl());
                            } else {
                                // attempt best-effort access; wrap in try-catch
                                try {
                                    destination.setSeerName(source.getSeer().getFullName());
                                } catch (Exception ignored) {
                                }
                                try {
                                    destination.setSeerAvatar(source.getSeer().getAvatarUrl());
                                } catch (Exception ignored) {
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    return destination;
                });
    }
}
