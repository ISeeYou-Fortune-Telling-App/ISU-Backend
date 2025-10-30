# Admin Conversations Testing Guide

## Overview
Admin conversations allow administrators to chat directly with any customer or seer without requiring a booking session.

## Features
- ✅ **No End Time**: Admin conversations have no time limit (`sessionEndTime = null`)
- ✅ **No Duration**: No session duration restriction (`sessionDurationMinutes = null`)
- ✅ **Always Active**: Admin conversations are always in `ACTIVE` status
- ✅ **Auto Creation**: Dummy data creates 5-10 random admin conversations
- ✅ **Smart Dialogues**: Different conversation content for customers vs seers

## Database Structure

### Conversation Fields for Admin Chat
```java
type = ADMIN_CHAT
admin = User (admin role)
targetUser = User (customer or seer)
booking = null
sessionStartTime = LocalDateTime.now()
sessionEndTime = null
sessionDurationMinutes = null
status = ACTIVE
```

## Testing Steps

### 1. Create Dummy Data

Run the application with dummy data enabled:
```cmd
.\mvnw spring-boot:run
```

Or check the dummy data creation logs:
```
[INFO] Bắt đầu tạo admin conversations...
[INFO] Tìm thấy X users (customers & seers) để tạo admin conversations
[INFO] Tạo ADMIN_CHAT conversation giữa admin {...} và CUSTOMER {...}
[INFO] Đã tạo X messages cho admin conversation {...}
[INFO] Đã tạo Y admin conversations
```

### 2. Test Admin Conversations API

#### A. Create Admin Conversation
**Endpoint:** `POST /admin/conversations`

**Request:**
```json
{
  "targetUserId": "uuid-of-customer-or-seer",
  "initialMessage": "Xin chào, tôi là admin của hệ thống"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Admin conversation created successfully",
  "data": {
    "conversationId": "uuid",
    "type": "ADMIN_CHAT",
    "status": "ACTIVE",
    "sessionStartTime": "2025-10-30T10:00:00",
    "sessionEndTime": null,
    "sessionDurationMinutes": null,
    "admin": {
      "id": "admin-uuid",
      "fullName": "Admin User"
    },
    "targetUser": {
      "id": "user-uuid",
      "fullName": "Customer/Seer Name",
      "role": "CUSTOMER" // or "SEER"
    }
  }
}
```

#### B. Get All Admin Conversations
**Endpoint:** `GET /admin/conversations`

**Query Params:**
- `page`: Page number (default: 1)
- `limit`: Items per page (default: 20)
- `sortType`: asc/desc (default: desc)
- `sortBy`: Field to sort (default: createdAt)

**Response:**
```json
{
  "code": 200,
  "message": "Conversations retrieved successfully",
  "data": {
    "content": [
      {
        "conversationId": "uuid",
        "type": "ADMIN_CHAT",
        "targetUser": {
          "fullName": "User Name",
          "role": "CUSTOMER"
        },
        "sessionStartTime": "2025-10-25T14:30:00",
        "sessionEndTime": null,
        "status": "ACTIVE"
      }
    ],
    "totalElements": 10,
    "totalPages": 1
  }
}
```

#### C. Get Specific Admin Conversation
**Endpoint:** `GET /admin/conversations/{conversationId}`

**Response:** Same as create response

### 3. Test Socket.IO Chat

#### A. Connect as Admin
```javascript
const socket = io('http://localhost:8081/chat', {
    transports: ['websocket'],
    query: {
        userId: 'admin-user-uuid'
    }
});
```

#### B. Join Admin Conversation
```javascript
socket.emit('join_conversation', conversationId, (response) => {
    if (response === 'success') {
        console.log('Joined admin conversation');
    }
});
```

#### C. Send Message
```javascript
socket.emit('send_message', {
    conversationId: 'conversation-uuid',
    textContent: 'Hello from admin'
}, (response) => {
    console.log('Message sent:', response);
});
```

#### D. Receive Messages
```javascript
socket.on('receive_message', (message) => {
    console.log('New message:', message);
});
```

### 4. Test as Customer/Seer

Customers and seers can also see their admin conversations:

**Endpoint:** `GET /conversations`

This will return:
- All booking-based conversations
- All admin conversations where they are the target user

## Sample Conversations

### For Customers
Admin conversations with customers typically include:
- Questions about refund policy
- Booking issues
- Account problems
- Payment inquiries

**Example Dialogue:**
```
Admin: "Xin chào, tôi là quản trị viên của hệ thống"
Customer: "Chào admin ạ"
Admin: "Tôi thấy bạn có một số thắc mắc về dịch vụ. Tôi có thể giúp gì cho bạn?"
Customer: "Em muốn hỏi về quy trình hoàn tiền ạ"
...
```

### For Seers
Admin conversations with seers typically include:
- System updates
- New features announcement
- Policy reminders
- Performance feedback

**Example Dialogue:**
```
Admin: "Xin chào, tôi là quản trị viên của hệ thống"
Seer: "Chào admin"
Admin: "Tôi muốn thông báo về một số cập nhật mới trong hệ thống"
Seer: "Vâng, tôi lắng nghe ạ"
...
```

## Database Queries

### Find All Admin Conversations
```sql
SELECT * FROM conversations 
WHERE type = 'ADMIN_CHAT' 
ORDER BY created_at DESC;
```

### Find Admin Conversations by Admin
```sql
SELECT c.*, u.full_name as target_name, u.role as target_role
FROM conversations c
JOIN users u ON c.target_user_id = u.id
WHERE c.type = 'ADMIN_CHAT' 
  AND c.admin_id = 'admin-uuid'
ORDER BY c.created_at DESC;
```

### Find Admin Conversation with Specific User
```sql
SELECT * FROM conversations 
WHERE type = 'ADMIN_CHAT' 
  AND admin_id = 'admin-uuid' 
  AND target_user_id = 'user-uuid';
```

### Count Messages in Admin Conversations
```sql
SELECT c.id, COUNT(m.id) as message_count
FROM conversations c
LEFT JOIN messages m ON c.id = m.conversation_id
WHERE c.type = 'ADMIN_CHAT'
GROUP BY c.id;
```

## Key Differences: Admin Chat vs Booking Session

| Feature | Admin Chat | Booking Session |
|---------|-----------|-----------------|
| **Type** | ADMIN_CHAT | BOOKING_SESSION |
| **Booking** | null | Required |
| **Admin** | Required | null |
| **Target User** | Required | null (uses booking relations) |
| **Session Start** | Now | Booking scheduled time |
| **Session End** | null (no limit) | Start + duration |
| **Duration** | null (no limit) | From booking package |
| **Status** | Always ACTIVE | WAITING → ACTIVE → ENDED |
| **Can Cancel** | ❌ No | ✅ Yes |
| **Time Limit** | ❌ None | ✅ Yes |
| **Join Tracking** | ❌ No | ✅ Yes (customerJoinedAt, seerJoinedAt) |

## Testing Checklist

- [ ] Admin can create conversation with any customer
- [ ] Admin can create conversation with any seer
- [ ] Cannot create duplicate admin conversation with same user
- [ ] Admin conversations have no end time
- [ ] Admin conversations have no duration limit
- [ ] Admin conversations are always ACTIVE
- [ ] Customer can see their admin conversation in conversations list
- [ ] Seer can see their admin conversation in conversations list
- [ ] Messages work correctly in admin conversations
- [ ] Socket.IO join/send/receive work for admin conversations
- [ ] Dummy data creates 5-10 admin conversations
- [ ] Different dialogues for customers vs seers

## Troubleshooting

### Issue 1: No Admin User Found
**Problem:** "Không tìm thấy admin user để tạo admin conversations"

**Solution:** Create an admin user first in the database

### Issue 2: Duplicate Conversation
**Problem:** Admin conversation already exists

**Solution:** This is expected behavior - only one admin conversation per user

### Issue 3: Cannot Join Conversation
**Problem:** Socket.IO returns "Unauthorized"

**Solution:** Verify the user is either:
- The admin who created the conversation
- The target user of the conversation

### Issue 4: Messages Not Appearing
**Problem:** Messages sent but not showing

**Solution:** 
- Check Socket.IO connection
- Verify user joined the conversation room
- Check browser console for errors

## Related Files

- `ConversationService.java` - Service layer
- `ConversationServiceImpl.java` - Implementation
- `AdminConversationController.java` - REST API endpoints
- `ConversationRepository.java` - Database queries
- `ChatSocketListener.java` - Socket.IO event handlers
- `Conversations.java` - Dummy data generation

## API Documentation

Full API documentation available at:
```
http://localhost:8080/swagger-ui.html
```

Look for tag: **011. Admin Conversation**

