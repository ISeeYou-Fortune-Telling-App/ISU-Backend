CREATE TABLE booking
(
    booking_id         UUID     NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    scheduled_time     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status             SMALLINT NOT NULL,
    additional_note    VARCHAR(1000),
    rating             DECIMAL(2, 1),
    comment            VARCHAR(1000),
    reviewed_at        TIMESTAMP WITHOUT TIME ZONE,
    service_package_id UUID     NOT NULL,
    customer_id        UUID     NOT NULL,
    CONSTRAINT pk_booking PRIMARY KEY (booking_id)
);

CREATE TABLE booking_payment
(
    booking_payment_id UUID             NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payment_method     SMALLINT         NOT NULL,
    amount             DOUBLE PRECISION NOT NULL,
    status             SMALLINT         NOT NULL,
    payment_type       SMALLINT         NOT NULL,
    transaction_id     VARCHAR(1000),
    approval_url       VARCHAR(1000),
    failure_reason     VARCHAR(1000),
    extra_info         VARCHAR(255),
    booking_id         UUID             NOT NULL,
    CONSTRAINT pk_booking_payment PRIMARY KEY (booking_payment_id)
);

CREATE TABLE certificate
(
    certificate_id          UUID         NOT NULL,
    created_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at              TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    certificate_name        VARCHAR(100) NOT NULL,
    certificate_description VARCHAR(1000),
    issued_by               VARCHAR(100) NOT NULL,
    issued_at               TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expiration_date         TIMESTAMP WITHOUT TIME ZONE,
    certificate_url         VARCHAR(500),
    status                  SMALLINT,
    decision_date           TIMESTAMP WITHOUT TIME ZONE,
    decision_reason         VARCHAR(500),
    seer_id                 UUID,
    CONSTRAINT pk_certificate PRIMARY KEY (certificate_id)
);

CREATE TABLE "certificate_category"
(
    id             UUID NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    certificate_id UUID NOT NULL,
    category_id    UUID NOT NULL,
    CONSTRAINT "pk_certificate_category" PRIMARY KEY (id)
);

CREATE TABLE conversation
(
    conversation_id           UUID        NOT NULL,
    created_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    booking_id                UUID,
    type                      VARCHAR(50) NOT NULL,
    admin_id                  UUID,
    target_user_id            UUID,
    session_start_time        TIMESTAMP WITHOUT TIME ZONE,
    session_end_time          TIMESTAMP WITHOUT TIME ZONE,
    session_duration_minutes  INTEGER,
    status                    VARCHAR(50) NOT NULL,
    customer_joined_at        TIMESTAMP WITHOUT TIME ZONE,
    seer_joined_at            TIMESTAMP WITHOUT TIME ZONE,
    canceled_by               VARCHAR(500),
    warning_notification_sent BOOLEAN,
    extended_minutes          INTEGER,
    CONSTRAINT pk_conversation PRIMARY KEY (conversation_id)
);

CREATE TABLE customer_profile
(
    customer_id    UUID         NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    zodiac_sign    VARCHAR(255),
    chinese_zodiac VARCHAR(255),
    five_elements  VARCHAR(255),
    CONSTRAINT pk_customer_profile PRIMARY KEY (customer_id)
);

CREATE TABLE email_verification
(
    id         UUID         NOT NULL,
    email      VARCHAR(255) NOT NULL,
    otp_code   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_used    BOOLEAN      NOT NULL,
    CONSTRAINT pk_email_verification PRIMARY KEY (id)
);

CREATE TABLE item_category
(
    id          UUID NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item_id     UUID,
    category_id UUID,
    CONSTRAINT pk_item_category PRIMARY KEY (id)
);

CREATE TABLE knowledge_category
(
    category_id UUID         NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    CONSTRAINT pk_knowledge_category PRIMARY KEY (category_id)
);

CREATE TABLE knowledge_item
(
    item_id    UUID          NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    title      VARCHAR(255)  NOT NULL,
    content    TEXT NOT NULL,
    image_url  VARCHAR(500),
    source     VARCHAR(500),
    view_count BIGINT        NOT NULL,
    status     SMALLINT,
    CONSTRAINT pk_knowledge_item PRIMARY KEY (item_id)
);

CREATE TABLE message
(
    message_id      UUID        NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    text_content    TEXT,
    image_url       VARCHAR(500),
    video_url       VARCHAR(500),
    message_type    VARCHAR(50),
    status          VARCHAR(20) NOT NULL,
    deleted_by      VARCHAR(20),
    read_at         TIMESTAMP WITHOUT TIME ZONE,
    conversation_id UUID        NOT NULL,
    sender_id       UUID,
    is_recalled     BOOLEAN     NOT NULL,
    recalled_at     TIMESTAMP WITHOUT TIME ZONE,
    recalled_by     UUID,
    CONSTRAINT pk_message PRIMARY KEY (message_id)
);

CREATE TABLE message_deleted_by
(
    message_id UUID NOT NULL,
    user_id    UUID
);

CREATE TABLE package_available_time
(
    time_id        UUID    NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    package_id     UUID    NOT NULL,
    week_date      INTEGER NOT NULL,
    available_from time WITHOUT TIME ZONE      NOT NULL,
    available_to   time WITHOUT TIME ZONE      NOT NULL,
    CONSTRAINT pk_package_available_time PRIMARY KEY (time_id)
);

CREATE TABLE package_category
(
    package_category_id UUID NOT NULL,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    package_id          UUID NOT NULL,
    category_id         UUID NOT NULL,
    CONSTRAINT pk_package_category PRIMARY KEY (package_category_id)
);

CREATE TABLE package_interaction
(
    id               UUID         NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    interaction_type VARCHAR(255) NOT NULL,
    user_id          UUID         NOT NULL,
    package_id       UUID         NOT NULL,
    CONSTRAINT pk_package_interaction PRIMARY KEY (id)
);

CREATE TABLE report
(
    report_id          UUID     NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    target_type        SMALLINT NOT NULL,
    target_id          UUID     NOT NULL,
    report_description VARCHAR(1000),
    status             SMALLINT NOT NULL,
    action_type        SMALLINT NOT NULL,
    note               VARCHAR(1000),
    reporter_id        UUID     NOT NULL,
    reported_user_id   UUID     NOT NULL,
    report_type_id     UUID     NOT NULL,
    CONSTRAINT pk_report PRIMARY KEY (report_id)
);

CREATE TABLE report_evidence
(
    evidence_id        UUID NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    report_id          UUID NOT NULL,
    evidence_image_url VARCHAR(500),
    CONSTRAINT pk_report_evidence PRIMARY KEY (evidence_id)
);

CREATE TABLE report_type
(
    type_id     UUID     NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name        SMALLINT NOT NULL,
    description VARCHAR(500),
    CONSTRAINT pk_report_type PRIMARY KEY (type_id)
);

CREATE TABLE seer_profile
(
    seer_id      UUID             NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    payment_info VARCHAR(500),
    paypal_email VARCHAR(255),
    CONSTRAINT pk_seer_profile PRIMARY KEY (seer_id)
);

CREATE TABLE seer_speciality
(
    seer_speciality_id UUID NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    user_id            UUID NOT NULL,
    category_id        UUID NOT NULL,
    CONSTRAINT pk_seer_speciality PRIMARY KEY (seer_speciality_id)
);

CREATE TABLE service_package
(
    package_id         UUID             NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    package_title      VARCHAR(100)     NOT NULL,
    package_content    VARCHAR(1000),
    image_url          VARCHAR(500),
    duration_minutes   INTEGER          NOT NULL,
    price              DOUBLE PRECISION NOT NULL,
    commission_rate    DOUBLE PRECISION,
    service_fee_amount DOUBLE PRECISION,
    status             SMALLINT,
    rejection_reason   VARCHAR(500),
    like_count         BIGINT,
    dislike_count      BIGINT,
    comment_count      BIGINT,
    deleted_at         TIMESTAMP WITHOUT TIME ZONE,
    seer_id            UUID,
    CONSTRAINT pk_service_package PRIMARY KEY (package_id)
);

CREATE TABLE service_review
(
    review_id        UUID          NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    comment          VARCHAR(1000) NOT NULL,
    package_id       UUID          NOT NULL,
    user_id          UUID          NOT NULL,
    review_parent_id UUID,
    CONSTRAINT pk_service_review PRIMARY KEY (review_id)
);

CREATE TABLE "user"
(
    user_id             UUID         NOT NULL,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    role                SMALLINT     NOT NULL,
    email               VARCHAR(255) NOT NULL,
    phone               VARCHAR(255) NOT NULL,
    gender              VARCHAR(255) NOT NULL,
    password_hash       VARCHAR(255) NOT NULL,
    full_name           VARCHAR(255) NOT NULL,
    avatar_url          VARCHAR(255),
    cover_url           VARCHAR(255),
    profile_description VARCHAR(1000),
    birth_date          TIMESTAMP WITHOUT TIME ZONE,
    status              SMALLINT     NOT NULL,
    fcm_token           VARCHAR(255),
    is_active           BOOLEAN,
    CONSTRAINT pk_user PRIMARY KEY (user_id)
);

ALTER TABLE package_interaction
    ADD CONSTRAINT uc_5ab591522f78e7306809f8e99 UNIQUE (user_id, package_id);

ALTER TABLE conversation
    ADD CONSTRAINT uc_conversation_booking UNIQUE (booking_id);

ALTER TABLE knowledge_category
    ADD CONSTRAINT uc_knowledge_category_name UNIQUE (name);

ALTER TABLE report_type
    ADD CONSTRAINT uc_report_type_name UNIQUE (name);

ALTER TABLE "user"
    ADD CONSTRAINT uc_user_email UNIQUE (email);

ALTER TABLE booking
    ADD CONSTRAINT FK_BOOKING_ON_CUSTOMER FOREIGN KEY (customer_id) REFERENCES "user" (user_id);

ALTER TABLE booking
    ADD CONSTRAINT FK_BOOKING_ON_SERVICE_PACKAGE FOREIGN KEY (service_package_id) REFERENCES service_package (package_id);

ALTER TABLE booking_payment
    ADD CONSTRAINT FK_BOOKING_PAYMENT_ON_BOOKING FOREIGN KEY (booking_id) REFERENCES booking (booking_id);

ALTER TABLE "certificate_category"
    ADD CONSTRAINT "FK_CERTIFICATE_CATEGORY_ON_CATEGORY" FOREIGN KEY (category_id) REFERENCES knowledge_category (category_id);

ALTER TABLE "certificate_category"
    ADD CONSTRAINT "FK_CERTIFICATE_CATEGORY_ON_CERTIFICATE" FOREIGN KEY (certificate_id) REFERENCES certificate (certificate_id);

ALTER TABLE certificate
    ADD CONSTRAINT FK_CERTIFICATE_ON_SEER FOREIGN KEY (seer_id) REFERENCES "user" (user_id);

ALTER TABLE conversation
    ADD CONSTRAINT FK_CONVERSATION_ON_ADMIN FOREIGN KEY (admin_id) REFERENCES "user" (user_id);

ALTER TABLE conversation
    ADD CONSTRAINT FK_CONVERSATION_ON_BOOKING FOREIGN KEY (booking_id) REFERENCES booking (booking_id);

ALTER TABLE conversation
    ADD CONSTRAINT FK_CONVERSATION_ON_TARGET_USER FOREIGN KEY (target_user_id) REFERENCES "user" (user_id);

ALTER TABLE customer_profile
    ADD CONSTRAINT FK_CUSTOMER_PROFILE_ON_CUSTOMER FOREIGN KEY (customer_id) REFERENCES "user" (user_id);

ALTER TABLE item_category
    ADD CONSTRAINT FK_ITEM_CATEGORY_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES knowledge_category (category_id);

ALTER TABLE item_category
    ADD CONSTRAINT FK_ITEM_CATEGORY_ON_ITEM FOREIGN KEY (item_id) REFERENCES knowledge_item (item_id);

ALTER TABLE message
    ADD CONSTRAINT FK_MESSAGE_ON_CONVERSATION FOREIGN KEY (conversation_id) REFERENCES conversation (conversation_id);

ALTER TABLE message
    ADD CONSTRAINT FK_MESSAGE_ON_RECALLED_BY FOREIGN KEY (recalled_by) REFERENCES "user" (user_id);

ALTER TABLE message
    ADD CONSTRAINT FK_MESSAGE_ON_SENDER FOREIGN KEY (sender_id) REFERENCES "user" (user_id);

ALTER TABLE package_available_time
    ADD CONSTRAINT FK_PACKAGE_AVAILABLE_TIME_ON_PACKAGE FOREIGN KEY (package_id) REFERENCES service_package (package_id);

ALTER TABLE package_category
    ADD CONSTRAINT FK_PACKAGE_CATEGORY_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES knowledge_category (category_id);

ALTER TABLE package_category
    ADD CONSTRAINT FK_PACKAGE_CATEGORY_ON_PACKAGE FOREIGN KEY (package_id) REFERENCES service_package (package_id);

ALTER TABLE package_interaction
    ADD CONSTRAINT FK_PACKAGE_INTERACTION_ON_PACKAGE FOREIGN KEY (package_id) REFERENCES service_package (package_id);

ALTER TABLE package_interaction
    ADD CONSTRAINT FK_PACKAGE_INTERACTION_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (user_id);

ALTER TABLE report_evidence
    ADD CONSTRAINT FK_REPORT_EVIDENCE_ON_REPORT FOREIGN KEY (report_id) REFERENCES report (report_id);

ALTER TABLE report
    ADD CONSTRAINT FK_REPORT_ON_REPORTED_USER FOREIGN KEY (reported_user_id) REFERENCES "user" (user_id);

ALTER TABLE report
    ADD CONSTRAINT FK_REPORT_ON_REPORTER FOREIGN KEY (reporter_id) REFERENCES "user" (user_id);

ALTER TABLE report
    ADD CONSTRAINT FK_REPORT_ON_REPORT_TYPE FOREIGN KEY (report_type_id) REFERENCES report_type (type_id);

ALTER TABLE seer_profile
    ADD CONSTRAINT FK_SEER_PROFILE_ON_SEER FOREIGN KEY (seer_id) REFERENCES "user" (user_id);

ALTER TABLE seer_speciality
    ADD CONSTRAINT FK_SEER_SPECIALITY_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES knowledge_category (category_id);

ALTER TABLE seer_speciality
    ADD CONSTRAINT FK_SEER_SPECIALITY_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (user_id);

ALTER TABLE service_package
    ADD CONSTRAINT FK_SERVICE_PACKAGE_ON_SEER FOREIGN KEY (seer_id) REFERENCES "user" (user_id);

ALTER TABLE service_review
    ADD CONSTRAINT FK_SERVICE_REVIEW_ON_PACKAGE FOREIGN KEY (package_id) REFERENCES service_package (package_id);

ALTER TABLE service_review
    ADD CONSTRAINT FK_SERVICE_REVIEW_ON_REVIEW_PARENT FOREIGN KEY (review_parent_id) REFERENCES service_review (review_id);

ALTER TABLE service_review
    ADD CONSTRAINT FK_SERVICE_REVIEW_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (user_id);

ALTER TABLE message_deleted_by
    ADD CONSTRAINT fk_message_deleted_by_on_message FOREIGN KEY (message_id) REFERENCES message (message_id);