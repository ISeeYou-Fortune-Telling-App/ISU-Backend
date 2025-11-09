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

    @Getter
    @AllArgsConstructor
    public enum PackageStatusEnum {
        AVAILABLE("AVAILABLE"),
        REJECTED("REJECTED"),
        HAVE_REPORT("HAVE_REPORT"),
        HIDDEN("HIDDEN");

        private final String value;

        public static PackageStatusEnum get(final String name) {
            return Stream.of(PackageStatusEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid Package status name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum PackageActionEnum {
        APPROVED("APPROVED"),
        REJECTED("REJECTED");

        private final String value;

        public static PackageActionEnum get(final String name) {
            return Stream.of(PackageActionEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid package action name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum KnowledgeItemStatusEnum {
        DRAFT("DRAFT"),
        PUBLISHED("PUBLISHED"),
        HIDDEN("HIDDEN");
        private final String value;
        public static KnowledgeItemStatusEnum get(final String name) {
            return Stream.of(KnowledgeItemStatusEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid certificate status name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum ReportTypeEnum {
        SPAM("Cảnh báo vi phạm spam"),
        INAPPROPRIATE_CONTENT("Nội dung không phù hợp"),
        HARASSMENT("Quấy rối"),
        HATE_SPEECH("Ngôn từ thù ghét"),
        VIOLENCE("Bạo lực"),
        NUDITY("Ảnh khỏa thân / nội dung nhạy cảm"),
        COPYRIGHT("Vi phạm bản quyền"),
        IMPERSONATION("Giả mạo danh tính"),
        FRAUD("Gian lận / lừa đảo"),
        OTHER("Khác");

        private final String value;

        public static ReportTypeEnum get(final String name) {
            return Stream.of(ReportTypeEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid report type name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum ReportStatusEnum {
        PENDING("Đang chờ xử lý"),
        VIEWED("Đã xem"),
        RESOLVED("Đã xử lý"),
        REJECTED("Từ chối xử lý");

        private final String value;

        public static ReportStatusEnum get(final String name) {
            return Stream.of(ReportStatusEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid report status name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum ReportActionEnum {
        NO_ACTION("Không hành động"),
        WARNING_ISSUED("Cảnh báo đã được gửi"),
        CONTENT_REMOVED("Nội dung đã bị xóa"),
        USER_SUSPENDED("Người dùng bị đình chỉ"),
        USER_BANNED("Người dùng bị khóa tài khoản");

        private final String value;

        public static ReportActionEnum get(final String name) {
            return Stream.of(ReportActionEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid report action name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum TargetReportTypeEnum {
        SERVICE_PACKAGE("SERVICE_PACKAGE"),
        CHAT("CHAT"),
        BOOKING("BOOKING"),
        SEER("SEER");

        private final String value;

        public static TargetReportTypeEnum get(final String name) {
            return Stream.of(TargetReportTypeEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid target report type name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum BookingStatusEnum {
        PENDING("PENDING"),
        CONFIRMED("CONFIRMED"),
        COMPLETED("COMPLETED"),
        FAILED("FAILED"),
        CANCELED("CANCELED");

        private final String value;

        public static BookingStatusEnum get(final String name) {
            return Stream.of(BookingStatusEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid booking status name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum PaymentMethodEnum {
        MOMO("MOMO"),
        VNPAY("VNPAY"),
        PAYPAL("PAYPAL");

        private final String value;

        public static PaymentMethodEnum get(final String name) {
            return Stream.of(PaymentMethodEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid payment method name: %s. Currently only PAYPAL is supported", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum PaymentStatusEnum {
        PENDING("PENDING"),
        COMPLETED("COMPLETED"),
        FAILED("FAILED"),
        REFUNDED("REFUNDED");

        private final String value;

        public static PaymentStatusEnum get(final String name) {
            return Stream.of(PaymentStatusEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid payment status name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum PaymentTypeEnum {
        PAID_PACKAGE("PAID_PACKAGE"),           // Khách hàng thanh toán cho gói dịch vụ của Seer
        RECEIVED_PACKAGE("RECEIVED_PACKAGE"),   // Hệ thống thanh toán lại tiền cho SEER
        BONUS("BONUS");                         // Admin thưởng thêm cho SEER

        private final String value;

        public static PaymentTypeEnum get(final String name) {
            return Stream.of(PaymentTypeEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid payment type name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum ServiceCategoryEnum {
        TAROT("TAROT"),
        PALM_READING("PALM_READING"),
        CONSULTATION("CONSULTATION"),
        PHYSIOGNOMY("PHYSIOGNOMY");

        private final String value;

        public static ServiceCategoryEnum get(final String name) {
            return Stream.of(ServiceCategoryEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid service category name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum ConversationTypeEnum {
        BOOKING_SESSION("BOOKING_SESSION"),
        SUPPORT("SUPPORT"),
        ADMIN_CHAT("ADMIN_CHAT");  // Admin chat with customer or seer

        private final String value;

        public static ConversationTypeEnum get(final String name) {
            return Stream.of(ConversationTypeEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid conversation type name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum ConversationStatusEnum {
        WAITING("WAITING"),
        ACTIVE("ACTIVE"),
        ENDED("ENDED"),
        CANCELLED("CANCELLED");

        private final String value;

        public static ConversationStatusEnum get(final String name) {
            return Stream.of(ConversationStatusEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid conversation status name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum MessageTypeEnum  {
        USER("USER"),
        SYSTEM("SYSTEM");

        private final String value;

        public static MessageTypeEnum get(final String name) {
            return Stream.of(MessageTypeEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid message type name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum MessageStatusEnum {
        UNREAD("SENT"),
        READ("READ"),
        DELETED("DELETED"),
        REMOVED("REMOVED");

        private final String value;

        public static MessageStatusEnum get(final String name) {
            return Stream.of(MessageStatusEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid message type name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum InteractionTypeEnum {
        LIKE("LIKE"),
        DISLIKE("DISLIKE");

        private final String value;

        public static InteractionTypeEnum get(final String name) {
            return Stream.of(InteractionTypeEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid interaction type name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum NotificationTypeEnum {
        PAYMENT("PAYMENT"),
        ACCOUNT("ACCOUNT"),
        CERTIFICATE("CERTIFICATE");

        private final String value;

        public static NotificationTypeEnum get(final String name) {
            return Stream.of(NotificationTypeEnum.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid notification type name: %s", name)));
        }
    }

    @Getter
    @AllArgsConstructor
    public enum SeerTier {
        BRONZE("BRONZE"),
        SILVER("SILVER"),
        GOLD("GOLD"),
        PLATINUM("PLATINUM"),
        DIAMOND("DIAMOND");

        private final String value;

        public static SeerTier get(final String name) {
            return Stream.of(SeerTier.values())
                    .filter(p -> p.name().equals(name.toUpperCase()) || p.getValue().equals(name.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(String.format("Invalid seer tier name: %s", name)));
        }
    }
}