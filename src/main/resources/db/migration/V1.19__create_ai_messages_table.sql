-- =============================================
-- Fortune Telling Application - Database Schema
-- Version: 1.19
-- Description: Create ai_messages table for AI chat functionality
-- =============================================

CREATE TABLE ai_messages
(
    id              UUID                        NOT NULL,
    user_id         UUID                        NOT NULL,
    sent_by_user    BOOLEAN                     NOT NULL,
    text_content    VARCHAR(5000),
    image_url       VARCHAR(1000),
    video_url       VARCHAR(1000),
    processing_time DOUBLE PRECISION,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_ai_messages PRIMARY KEY (id)
);

-- Add foreign key constraint to user table
ALTER TABLE ai_messages
    ADD CONSTRAINT fk_ai_messages_user
    FOREIGN KEY (user_id) REFERENCES "user" (user_id);

-- Create index for better query performance
CREATE INDEX idx_ai_messages_user_id ON ai_messages (user_id);
CREATE INDEX idx_ai_messages_created_at ON ai_messages (created_at);