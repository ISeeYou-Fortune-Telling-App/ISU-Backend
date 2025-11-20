-- V1.21__delete_self_conversations.sql
-- Delete any conversation where admin_id = target_user_id and its related messages
-- Safe order: delete dependent messages first (there is a FK from message.conversation_id -> conversation.conversation_id)

-- NOTE: This migration targets PostgreSQL (see application.yaml hibernate dialect).

-- Delete messages that belong to conversations where admin_id equals target_user_id
DELETE FROM message
WHERE conversation_id IN (
  SELECT conversation_id FROM conversation
  WHERE admin_id IS NOT NULL
    AND target_user_id IS NOT NULL
    AND admin_id = target_user_id
);

-- Delete the conversations themselves
DELETE FROM conversation
WHERE admin_id IS NOT NULL
  AND target_user_id IS NOT NULL
  AND admin_id = target_user_id;

