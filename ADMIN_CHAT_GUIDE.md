# 🛡️ Admin Chat Guide - Complete Documentation

## 📋 Tổng quan

Hướng dẫn đầy đủ về **Admin Chat Feature** - cho phép Admin tạo và quản lý conversations với bất kỳ user nào.

**Audience:** Backend developers, Admin users, QA testers

**Related Files:**
- `admin-chat.html` - Testing tool
- `CHAT_INTEGRATION_GUIDE.md` - Customer & Seer guide

---

Admin có thể tạo conversation và chat trực tiếp với bất kỳ user nào (Customer hoặc Seer) bất cứ lúc nào, không cần booking.

---


## 🎯 Business Logic

### Conversation Types

| Type | booking_id | admin_id | target_user_id | Description |
|------|-----------|----------|----------------|-------------|
| `BOOKING_SESSION` | ✅ Required | ❌ NULL | ❌ NULL | Normal booking conversation |
| `SUPPORT` | ✅ Required | ❌ NULL | ❌ NULL | Support conversation |
| `ADMIN_CHAT` | ❌ NULL | ✅ Required | ✅ Required | Admin chat with user |

### Rules

1. **Admin tạo conversation:**
   - Type = `ADMIN_CHAT`
   - `admin_id` = current admin user ID
   - `target_user_id` = customer hoặc seer ID
   - `booking_id` = NULL
   - `status` = `ACTIVE` (ngay lập tức)
   - Không có `session_end_time` (chat không giới hạn thời gian)

2. **Kiểm tra duplicate:**
   - Nếu admin đã có conversation với user này → trả về conversation cũ
   - Không tạo duplicate conversation

3. **Participants:**
   - Admin chat với Customer → `admin` + `customer`
   - Admin chat với Seer → `admin` + `seer`

---

## 🔌 REST API Endpoints

### Base URL
```
http://localhost:8080/admin/conversations
```

### Authentication
**Required:** Admin role + JWT token

```
Authorization: Bearer {admin_jwt_token}
```

### Response Format

All responses follow the standard format:

**Success Response:**
```json
{
  "statusCode": 200,
  "message": "Success message",
  "data": { ... }  // or data: [ ... ] for list
}
```

**Error Response:**
```json
{
  "statusCode": 400,
  "message": "Error message",
  "errors": [ ... ]
}
```

---

### 1. Create Admin Conversation

**Endpoint:**
```
POST /admin/conversations
```

**Request Body:**
```json
{
  "targetUserId": "uuid-of-customer-or-seer",
  "initialMessage": "Hello! How can I help you?" // Optional
}
```

**Example:**
```bash
curl -X POST http://localhost:8080/admin/conversations \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "targetUserId": "123e4567-e89b-12d3-a456-426614174000",
    "initialMessage": "Hi, this is admin support. How can I assist you?"
  }'
```

**Response (200 OK):**
```json
{
  "statusCode": 200,
  "message": "Admin conversation created successfully",
  "data": {
    "id": "uuid",
    "conversationId": "uuid",
    "seerId": "admin-uuid",
    "seerName": "Admin Name",
    "seerAvatarUrl": "https://...",
    "customerId": "target-user-uuid",
    "customerName": "User Name",
    "customerAvatarUrl": "https://...",
    "sessionStartTime": "2025-10-28T14:00:00",
    "sessionEndTime": null,
    "sessionDurationMinutes": null,
    "seerUnreadCount": 0,
    "customerUnreadCount": 1,
    "lastMessageContent": "Hi, this is admin support...",
    "lastMessageTime": "2025-10-28T14:00:00",
    "status": "ACTIVE",
    "createdAt": "2025-10-28T14:00:00",
    "updatedAt": "2025-10-28T14:00:00"
  }
}
```

**Notes:**
- ✅ Nếu admin chat với **Customer** → customerId = customer, seerId = admin
- ✅ Nếu admin chat với **Seer** → customerId = seer (trong customer field), seerId = admin
- ✅ Frontend check role để hiển thị đúng

---

### 2. Get All Admin Conversations

**Endpoint:**
```
GET /admin/conversations
```

**Query Parameters:**
- `page` (int, default: 1) - Page number (1-based)
- `limit` (int, default: 20) - Items per page
- `sortType` (string, default: "desc") - Sort direction (asc/desc)
- `sortBy` (string, default: "createdAt") - Sort field

**Example:**
```bash
curl -X GET "http://localhost:8080/admin/conversations?page=1&limit=20" \
  -H "Authorization: Bearer {admin_token}"
```

**Response (200 OK):**
```json
{
  "statusCode": 200,
  "message": "Conversations retrieved successfully",
  "data": {
    "content": [
      {
        "id": "uuid",
        "conversationId": "uuid",
        "seerId": "admin-uuid",
        "seerName": "Admin",
        "customerId": "user-uuid",
        "customerName": "John Doe",
        "lastMessageContent": "Thank you!",
        "lastMessageTime": "2025-10-28T15:30:00",
        "status": "ACTIVE"
      }
    ],
    "totalElements": 25,
    "totalPages": 2,
    "size": 20,
    "number": 0
  }
}
```

---

### 3. Get Admin Conversation by ID

**Endpoint:**
```
GET /admin/conversations/{conversationId}
```

**Example:**
```bash
curl -X GET "http://localhost:8080/admin/conversations/{uuid}" \
  -H "Authorization: Bearer {admin_token}"
```

**Response (200 OK):**
```json
{
  "statusCode": 200,
  "message": "Conversation retrieved successfully",
  "data": {
    "id": "uuid",
    "conversationId": "uuid",
    "seerId": "admin-uuid",
    "seerName": "Admin",
    "customerId": "user-uuid",
    "customerName": "John Doe",
    "sessionStartTime": "2025-10-28T14:00:00",
    "sessionEndTime": null,
    "status": "ACTIVE"
  }
}
```

---

## 💬 Socket.IO Integration

Admin chat sử dụng **CÙNG Socket.IO server** với booking chat.

### Connection

```javascript
const socket = io('http://localhost:8081/chat', {
  query: { userId: adminUserId }  // Admin's user ID
});
```

### Join Admin Conversation

```javascript
socket.emit('join_conversation', conversationId, (response) => {
  if (response === 'success') {
    console.log('Admin joined conversation');
  }
});
```

### Send Message

```javascript
const message = {
  conversationId: conversationId,
  textContent: 'Admin message...'
};

socket.emit('send_message', message, (response) => {
  console.log('Message sent');
});
```

### Receive Message

```javascript
socket.on('receive_message', (message) => {
  console.log('New message:', message);
  // message.senderId === adminUserId ? sentByMe : sentByOther
});
```

**Important:**
- ✅ Admin conversation **KHÔNG có** session time limits
- ✅ Admin conversation **KHÔNG bị** auto-cancel hoặc auto-end
- ✅ Admin có thể chat bất cứ lúc nào

---

## 🎨 Frontend Implementation

### React Example - Admin Create Conversation

```javascript
const AdminChatPanel = () => {
  const [targetUserId, setTargetUserId] = useState('');
  const [initialMessage, setInitialMessage] = useState('');

  const createAdminConversation = async () => {
    try {
      const response = await fetch('/admin/conversations', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${adminToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          targetUserId: targetUserId,
          initialMessage: initialMessage
        })
      });

      const result = await response.json();
      
      if (result.statusCode === 200) {
        console.log('Conversation created:', result.data);
        // Redirect to chat page
        window.location.href = `/admin/chat/${result.data.conversationId}`;
      } else {
        alert(result.message);
      }
    } catch (error) {
      console.error('Error creating conversation:', error);
    }
  };

  return (
    <div>
      <h2>Create Admin Chat</h2>
      <input
        type="text"
        placeholder="Target User ID"
        value={targetUserId}
        onChange={(e) => setTargetUserId(e.target.value)}
      />
      <textarea
        placeholder="Initial message (optional)"
        value={initialMessage}
        onChange={(e) => setInitialMessage(e.target.value)}
      />
      <button onClick={createAdminConversation}>
        Start Chat
      </button>
    </div>
  );
};
```

### Admin Chat Component

```javascript
const AdminChatComponent = ({ conversationId, adminUser }) => {
  const [socket, setSocket] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputText, setInputText] = useState('');

  useEffect(() => {
    // Initialize Socket.IO
    const socketInstance = io('http://localhost:8081/chat', {
      query: { userId: adminUser.id }
    });

    socketInstance.on('connect', () => {
      // Join conversation
      socketInstance.emit('join_conversation', conversationId, (response) => {
        if (response === 'success') {
          loadMessages();
        }
      });
    });

    socketInstance.on('receive_message', (message) => {
      setMessages(prev => [...prev, message]);
    });

    setSocket(socketInstance);

    return () => {
      socketInstance.emit('leave_conversation', conversationId);
      socketInstance.disconnect();
    };
  }, [conversationId, adminUser.id]);

  const loadMessages = async () => {
    const response = await fetch(
      `/api/messages/conversation/${conversationId}`,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
    );
    const data = await response.json();
    setMessages(data.content);
  };

  const sendMessage = () => {
    if (!inputText.trim()) return;

    const message = {
      conversationId: conversationId,
      textContent: inputText
    };

    socket.emit('send_message', message, (response) => {
      if (response === 'success') {
        setInputText('');
      }
    });
  };

  return (
    <div className="admin-chat">
      <div className="messages">
        {messages.map(msg => (
          <div
            key={msg.id}
            className={msg.senderId === adminUser.id ? 'sent' : 'received'}
          >
            <strong>
              {msg.senderId === adminUser.id ? 'You (Admin)' : msg.customerName || msg.seerName}:
            </strong>
            <p>{msg.textContent}</p>
          </div>
        ))}
      </div>

      <div className="input-area">
        <input
          type="text"
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
          placeholder="Type a message..."
        />
        <button onClick={sendMessage}>Send</button>
      </div>
    </div>
  );
};
```

---

## 🔍 Data Mapping

### ConversationResponse Mapping Rules

**For ADMIN_CHAT:**

| Field | Value | Description |
|-------|-------|-------------|
| `seerId` | `admin.id` | Admin user ID |
| `seerName` | `admin.fullName` | Admin name |
| `seerAvatarUrl` | `admin.avatarUrl` | Admin avatar |
| `customerId` | `targetUser.id` | Target user ID (customer or seer) |
| `customerName` | `targetUser.fullName` | Target user name |
| `customerAvatarUrl` | `targetUser.avatarUrl` | Target user avatar |

**Important:**
- Nếu target user là **Customer** → mapping như trên
- Nếu target user là **Seer** → vẫn map vào customer fields
- Frontend cần check role để hiển thị đúng label

---

## 🚨 Error Handling

### 1. User không tồn tại
```json
{
  "status": 404,
  "message": "Target user not found with id: xxx"
}
```

### 2. Không phải admin
```json
{
  "status": 403,
  "message": "Only admin can create admin conversations"
}
```

### 3. Unauthorized
```json
{
  "status": 401,
  "message": "Unauthorized"
}
```

---

## ✅ Testing Checklist

### Backend Testing

- [ ] Admin tạo conversation với Customer thành công
- [ ] Admin tạo conversation với Seer thành công
- [ ] Duplicate conversation không được tạo
- [ ] Non-admin user không thể tạo admin conversation
- [ ] Admin có thể join conversation
- [ ] Target user có thể join conversation
- [ ] Admin có thể send message
- [ ] Target user có thể send message
- [ ] Admin conversation không bị auto-end
- [ ] Database migration chạy thành công

### Frontend Testing

- [ ] Admin panel hiển thị list conversations
- [ ] Admin có thể select user để chat
- [ ] Admin có thể send initial message
- [ ] Chat UI hiển thị đúng messages
- [ ] Real-time messaging hoạt động
- [ ] Unread count cập nhật đúng
- [ ] Avatar và name hiển thị đúng

---

## 📊 Differences: Admin Chat vs Booking Chat

| Feature | Booking Chat | Admin Chat |
|---------|-------------|-----------|
| **Type** | `BOOKING_SESSION` | `ADMIN_CHAT` |
| **Booking** | Required | NULL |
| **Participants** | Customer + Seer | Admin + Any User |
| **Session Time** | Limited (based on booking) | Unlimited |
| **Auto End** | Yes (cronjob) | No |
| **Late Cancel** | Yes (>10 mins) | No |
| **Status** | WAITING → ACTIVE → ENDED | ACTIVE only |
| **Who can create** | System (auto) | Admin (manual) |
| **Session Start** | booking.scheduledTime | Immediately |

---

## 🔐 Security

### Access Control

1. **Create Conversation:**
   - ✅ Chỉ ADMIN role
   - ✅ JWT token required

2. **Join Conversation:**
   - ✅ Admin user
   - ✅ Target user
   - ❌ Other users → "unauthorized"

3. **Send Message:**
   - ✅ Admin user
   - ✅ Target user
   - ❌ Other users → "unauthorized"

### Validation

- `targetUserId` must exist in database
- `targetUserId` cannot be admin (cannot chat with self)
- Conversation type must be `ADMIN_CHAT`

---

## 📝 Summary

### What's New?

1. ✅ **New Enum:** `ConversationTypeEnum.ADMIN_CHAT`
2. ✅ **New Entity Fields:**
   - `admin` (User) - Admin who created chat
   - `targetUser` (User) - Customer or Seer
   - `booking` - Now nullable
3. ✅ **New DTO:** `AdminCreateConversationRequest`
4. ✅ **New Controller:** `AdminConversationController`
5. ✅ **New Service Method:** `createAdminConversation()`
6. ✅ **New Repository Queries:**
   - `findAdminConversationByAdminAndTarget()`
   - `findAdminConversationsByAdmin()`
   - `findAdminConversationsByTargetUser()`
7. ✅ **Updated:** `ChatSocketListener` to handle admin chat
8. ✅ **Updated:** `ConversationMapper` to map admin/target users
9. ✅ **Database Migration:** 3 new columns + indexes

### How to Use (Quick Start)

**Admin:**
```bash
# 1. Create conversation
POST /api/admin/conversations
{
  "targetUserId": "user-uuid",
  "initialMessage": "Hi!"
}

# 2. Connect to Socket.IO
socket = io('/chat', { query: { userId: adminId }})

# 3. Join conversation
socket.emit('join_conversation', conversationId)

# 4. Send message
socket.emit('send_message', { conversationId, textContent: "..." })
```

**Target User:**
```bash
# Same as admin - join and chat normally
```

---

**All features are ready to use! 🎉**

