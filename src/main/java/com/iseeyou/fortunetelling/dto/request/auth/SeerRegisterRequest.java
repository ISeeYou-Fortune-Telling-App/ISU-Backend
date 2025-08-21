package com.iseeyou.fortunetelling.dto.request.auth;

import com.iseeyou.fortunetelling.dto.request.certificate.CertificateCreateRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class SeerRegisterRequest extends RegisterRequest {
    private Set<String> specialityIds;
    private String profileDescription;
    private List<CertificateCreateRequest> certificates;
}
