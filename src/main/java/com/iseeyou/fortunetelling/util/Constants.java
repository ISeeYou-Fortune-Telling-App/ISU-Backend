package com.iseeyou.fortunetelling.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

public final class Constants {
    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    public static final String TOKEN_HEADER = "Authorization";

    public static final String TOKEN_TYPE = "Bearer";

    @Getter
    @AllArgsConstructor
    public enum RoleEnum {
        ADMIN("ADMIN"),
        SEER("SEER"),
        UNVERIFIED_SEER("UNVERIFIED_SEER"),
        GUEST("GUEST"),
        CUSTOMER("CUSTOMER");

        private final String value;

        public static RoleEnum get(final String name) {
            return Stream.of(RoleEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid role name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum StatusProfileEnum {
        ACTIVE("ACTIVE"),
        INACTIVE("INACTIVE"),
        VERIFIED("VERIFIED"),
        UNVERIFIED("UNVERIFIED"),
        BLOCKED("BLOCKED");

        private final String value;

        public static StatusProfileEnum get(final String name) {
            return Stream.of(StatusProfileEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid status profile name: %s", name)));
        }
    }

    public static String getTokenFromPath(final String path) {
        if (path == null || path.isEmpty())
            return null;

        final String[] fields = path.split("/");

        if (fields.length == 0)
            return null;

        try {
            return fields[2];
        } catch (final IndexOutOfBoundsException e) {
            System.out.println("Cannot find user or channel id from the path!. Ex:"+ e.getMessage());
        }
        return null;
    }

    @Getter
    @AllArgsConstructor
    public enum CertificateStatusEnum {
        PENDING("PENDING"),
        APPROVED("APPROVED"),
        REJECTED("REJECTED");

        private final String value;

        public static CertificateStatusEnum get(final String name) {
            return Stream.of(CertificateStatusEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid certificate status name: %s", name)));
        }
    }
}