-- Check all conversations in database
-- Run this query to see what conversations exist

-- 1. Count conversations by type
SELECT
    type,
    COUNT(*) as count
FROM conversations
GROUP BY type;

-- 2. Count conversations by type and role
SELECT
    c.type,
    CASE
        WHEN c.type = 'BOOKING_SESSION' THEN 'Booking Session'
        WHEN u_admin.role = 'ADMIN' THEN 'Admin → User'
        WHEN u_admin.role = 'CUSTOMER' THEN 'Customer → Seer'
        ELSE 'Unknown'
    END as conversation_category,
    COUNT(*) as count
FROM conversations c
LEFT JOIN users u_admin ON c.admin_id = u_admin.id
GROUP BY c.type, u_admin.role
ORDER BY count DESC;

-- 3. Show all ADMIN_CHAT conversations with details
SELECT
    c.id as conversation_id,
    c.type,
    c.status,
    u_admin.full_name as initiator_name,
    u_admin.role as initiator_role,
    u_target.full_name as target_name,
    u_target.role as target_role,
    c.session_start_time,
    c.session_end_time,
    c.created_at,
    (SELECT COUNT(*) FROM messages WHERE conversation_id = c.id) as message_count
FROM conversations c
LEFT JOIN users u_admin ON c.admin_id = u_admin.id
LEFT JOIN users u_target ON c.target_user_id = u_target.id
WHERE c.type = 'ADMIN_CHAT'
ORDER BY c.created_at DESC;

-- 4. Show Customer → Seer conversations specifically
SELECT
    c.id as conversation_id,
    u_customer.full_name as customer_name,
    u_seer.full_name as seer_name,
    c.session_start_time,
    (SELECT COUNT(*) FROM messages WHERE conversation_id = c.id) as message_count,
    c.created_at
FROM conversations c
JOIN users u_customer ON c.admin_id = u_customer.id
JOIN users u_seer ON c.target_user_id = u_seer.id
WHERE c.type = 'ADMIN_CHAT'
  AND u_customer.role = 'CUSTOMER'
  AND u_seer.role = 'SEER'
ORDER BY c.created_at DESC;

-- 5. Count Customer → Seer conversations per customer
SELECT
    u_customer.full_name as customer_name,
    COUNT(*) as conversation_count
FROM conversations c
JOIN users u_customer ON c.admin_id = u_customer.id
JOIN users u_seer ON c.target_user_id = u_seer.id
WHERE c.type = 'ADMIN_CHAT'
  AND u_customer.role = 'CUSTOMER'
  AND u_seer.role = 'SEER'
GROUP BY u_customer.id, u_customer.full_name
ORDER BY conversation_count DESC;

-- 6. Check for conversations WITHOUT messages
SELECT
    c.id as conversation_id,
    c.type,
    u_admin.full_name as admin_name,
    u_target.full_name as target_name,
    (SELECT COUNT(*) FROM messages WHERE conversation_id = c.id) as message_count
FROM conversations c
LEFT JOIN users u_admin ON c.admin_id = u_admin.id
LEFT JOIN users u_target ON c.target_user_id = u_target.id
WHERE c.type = 'ADMIN_CHAT'
  AND (SELECT COUNT(*) FROM messages WHERE conversation_id = c.id) = 0;

-- 7. Show all conversations for a specific customer (replace UUID)
-- SELECT
--     c.id,
--     c.type,
--     CASE
--         WHEN c.type = 'BOOKING_SESSION' THEN 'Booking with ' || u_seer.full_name
--         WHEN c.admin_id = 'YOUR_CUSTOMER_UUID' THEN 'Chat with ' || u_target.full_name
--         WHEN c.target_user_id = 'YOUR_CUSTOMER_UUID' THEN 'Chat from ' || u_admin.full_name
--     END as description,
--     c.status,
--     c.created_at
-- FROM conversations c
-- LEFT JOIN users u_admin ON c.admin_id = u_admin.id
-- LEFT JOIN users u_target ON c.target_user_id = u_target.id
-- LEFT JOIN bookings b ON c.booking_id = b.id
-- LEFT JOIN service_packages sp ON b.service_package_id = sp.id
-- LEFT JOIN users u_seer ON sp.seer_id = u_seer.id
-- WHERE c.admin_id = 'YOUR_CUSTOMER_UUID'
--    OR c.target_user_id = 'YOUR_CUSTOMER_UUID'
--    OR b.customer_id = 'YOUR_CUSTOMER_UUID'
-- ORDER BY c.created_at DESC;

-- 8. Summary stats
SELECT
    'Total Conversations' as metric,
    COUNT(*) as value
FROM conversations
UNION ALL
SELECT
    'Booking Conversations',
    COUNT(*)
FROM conversations
WHERE type = 'BOOKING_SESSION'
UNION ALL
SELECT
    'Admin Chat Conversations',
    COUNT(*)
FROM conversations
WHERE type = 'ADMIN_CHAT'
UNION ALL
SELECT
    'Customer → Seer Chats',
    COUNT(*)
FROM conversations c
JOIN users u_admin ON c.admin_id = u_admin.id
WHERE c.type = 'ADMIN_CHAT'
  AND u_admin.role = 'CUSTOMER'
UNION ALL
SELECT
    'Admin → User Chats',
    COUNT(*)
FROM conversations c
JOIN users u_admin ON c.admin_id = u_admin.id
WHERE c.type = 'ADMIN_CHAT'
  AND u_admin.role = 'ADMIN'
UNION ALL
SELECT
    'Total Messages',
    COUNT(*)
FROM messages
UNION ALL
SELECT
    'Total Users',
    COUNT(*)
FROM users;

