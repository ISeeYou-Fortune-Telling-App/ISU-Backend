# 🚀 Frontend Chat Integration Guide

## 📋 Mục lục
1. [Tổng quan](#tổng-quan)
2. [Socket.IO Setup](#socketio-setup)
3. [REST API Endpoints](#rest-api-endpoints)
4. [Socket.IO Events](#socketio-events)
5. [Flow diagram](#flow-diagram)
6. [Code Examples](#code-examples)
7. [Error Handling](#error-handling)
8. [Testing](#testing)

---

## 🎯 Tổng quan

Hệ thống chat sử dụng **Socket.IO** cho realtime messaging và **REST API** cho data fetching.

### Architecture
```
Frontend (React/Vue/Angular)
    ↓
Socket.IO Client (port 8081)  +  REST API (port 8080)
    ↓
Backend Server
```

### Key Concepts
- **Conversation**: Phiên chat giữa Customer và Seer
- **Message**: Tin nhắn trong conversation
- **Status**: WAITING → ACTIVE → ENDED/CANCELLED
- **Room**: Mỗi conversation là 1 Socket.IO room

---

## 🔌 Socket.IO Setup

### 1. Installation

**npm:**
```bash
npm install socket.io-client
```

**yarn:**
```bash
yarn add socket.io-client
```

### 2. Configuration

```javascript
import { io } from 'socket.io-client';

// Socket.IO Server Configuration
const SOCKET_URL = 'http://localhost:8081';  // hoặc production URL
const NAMESPACE = '/chat';

// Connect to Socket.IO server
const socket = io(`${SOCKET_URL}${NAMESPACE}`, {
  query: {
    userId: currentUser.id  // REQUIRED: User ID của user hiện tại
  },
  transports: ['websocket', 'polling'],
  reconnection: true,
  reconnectionDelay: 1000,
  reconnectionAttempts: 5
});
```

### 3. Connection Events

```javascript
// Connection successful
socket.on('connect', () => {
  console.log('Connected to Socket.IO server');
  console.log('Socket ID:', socket.id);
});

// Connection error
socket.on('connect_error', (error) => {
  console.error('Connection error:', error);
});

// Disconnected
socket.on('disconnect', (reason) => {
  console.log('Disconnected:', reason);
});

// Server confirmed connection
socket.on('connect_success', (data) => {
  console.log('Server message:', data.message);
  // "Connected to chat server successfully"
});
```

---

## 🌐 REST API Endpoints

### Base URL
```
http://localhost:8080/api
```

### Authentication
Tất cả requests phải có JWT token trong header:
```
Authorization: Bearer {your_jwt_token}
```

### 1. Get My Conversations (List)

**Endpoint:**
```
GET /api/conversations/my-chat-sessions
```

**Query Parameters:**
- `page` (int, default: 0) - Page number
- `size` (int, default: 10) - Items per page
- `sort` (string, default: "createdAt,desc") - Sort field and direction

**Example Request:**
```javascript
const response = await fetch('/api/conversations/my-chat-sessions?page=0&size=10', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const data = await response.json();
```

**Response:**
```json
{
  "content": [
    {
      "id": "uuid",
      "conversationId": "uuid",
      "seerId": "uuid",
      "seerName": "John Doe",
      "seerAvatarUrl": "https://...",
      "customerId": "uuid",
      "customerName": "Jane Smith",
      "customerAvatarUrl": "https://...",
      "sessionStartTime": "2025-10-28T14:00:00",
      "sessionEndTime": "2025-10-28T15:00:00",
      "sessionDurationMinutes": 60,
      "seerUnreadCount": 3,
      "customerUnreadCount": 0,
      "lastMessageContent": "Thank you for the session!",
      "lastMessageTime": "2025-10-28T14:45:00",
      "status": "ACTIVE",
      "createdAt": "2025-10-28T13:55:00",
      "updatedAt": "2025-10-28T14:45:00"
    }
  ],
  "totalElements": 15,
  "totalPages": 2,
  "size": 10,
  "number": 0
}
```

### 2. Get Conversation by ID

**Endpoint:**
```
GET /api/conversations/{conversationId}
```

**Example:**
```javascript
const response = await fetch(`/api/conversations/${conversationId}`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const conversation = await response.json();
```

### 3. Get Conversation by Booking ID

**Endpoint:**
```
GET /api/conversations/booking/{bookingId}
```

**Example:**
```javascript
const response = await fetch(`/api/conversations/booking/${bookingId}`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const conversation = await response.json();
```

### 4. Get Messages in Conversation

**Endpoint:**
```
GET /api/messages/conversation/{conversationId}
```

**Query Parameters:**
- `page` (int, default: 0)
- `size` (int, default: 50)
- `sort` (string, default: "createdAt,asc")

**Example:**
```javascript
const response = await fetch(`/api/messages/conversation/${conversationId}?page=0&size=50`, {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const messages = await response.json();
```

**Response:**
```json
{
  "content": [
    {
      "id": "uuid",
      "conversationId": "uuid",
      "customerId": "uuid",
      "customerName": "Jane Smith",
      "customerAvatar": "https://...",
      "seerId": "uuid",
      "seerName": "John Doe",
      "seerAvatar": "https://...",
      "senderId": "uuid",
      "textContent": "Hello, I need help with...",
      "imageUrl": null,
      "videoUrl": null,
      "messageType": "TEXT",
      "status": "READ",
      "deletedBy": null,
      "createdAt": "2025-10-28T14:01:00",
      "updatedAt": "2025-10-28T14:02:00"
    }
  ],
  "totalElements": 25,
  "totalPages": 1,
  "size": 50,
  "number": 0
}
```

---

## 📡 Socket.IO Events

### Events Client → Server

#### 1. Join Conversation
Join vào một conversation để bắt đầu nhận/gửi messages.

**Event:** `join_conversation`

**Payload:** `conversationId` (string)

**Example:**
```javascript
socket.emit('join_conversation', conversationId, (response) => {
  if (response === 'success') {
    console.log('Joined conversation successfully');
  } else {
    console.error('Failed to join:', response);
  }
});
```

**Important Notes:**
- ✅ Phải join conversation trước khi send/receive messages
- ✅ Backend sẽ tự động track `customerJoinedAt` hoặc `seerJoinedAt`
- ✅ Nếu vào trễ >10 phút, session sẽ bị cancel bởi cronjob
- ❌ Nếu user không phải participant, sẽ nhận error "You are not authorized"

---

#### 2. Send Message
Gửi tin nhắn vào conversation.

**Event:** `send_message`

**Payload:**
```javascript
{
  conversationId: "uuid",     // REQUIRED
  textContent: "Hello!",      // Optional (if no image/video)
  imageUrl: "https://...",    // Optional
  videoUrl: "https://..."     // Optional
}
```

**Example:**
```javascript
const message = {
  conversationId: conversationId,
  textContent: 'Hello, how can I help you?'
};

socket.emit('send_message', message, (response) => {
  if (response === 'success') {
    console.log('Message sent successfully');
  } else {
    console.error('Failed to send:', response);
  }
});
```

**Important Notes:**
- ✅ Conversation status phải là `ACTIVE` mới gửi được message
- ✅ Nếu status là `WAITING`, user có thể join nhưng không gửi được message
- ✅ Message sẽ được broadcast tới TẤT CẢ participants (bao gồm cả sender)
- ✅ Backend tự động set sender dựa trên `userId` trong query params

---

#### 3. Mark Messages as Read
Đánh dấu tất cả messages chưa đọc trong conversation là đã đọc.

**Event:** `mark_read`

**Payload:** `conversationId` (string)

**Example:**
```javascript
socket.emit('mark_read', conversationId, (response) => {
  if (response === 'success') {
    console.log('Messages marked as read');
  }
});
```

**Important Notes:**
- ✅ Chỉ mark read các messages KHÔNG PHẢI do mình gửi
- ✅ Nên call khi user mở conversation hoặc scroll to bottom

---

#### 4. Leave Conversation
Rời khỏi conversation room.

**Event:** `leave_conversation`

**Payload:** `conversationId` (string)

**Example:**
```javascript
socket.emit('leave_conversation', conversationId, (response) => {
  console.log('Left conversation');
});
```

**Important Notes:**
- ✅ Call khi user navigate away from chat screen
- ✅ Không bắt buộc, Socket.IO sẽ tự cleanup khi disconnect

---

### Events Server → Client

#### 1. Receive Message
Nhận tin nhắn mới (từ chính mình hoặc người khác).

**Event:** `receive_message`

**Payload:**
```javascript
{
  id: "uuid",
  conversationId: "uuid",
  customerId: "uuid",
  customerName: "Jane Smith",
  customerAvatar: "https://...",
  seerId: "uuid",
  seerName: "John Doe",
  seerAvatar: "https://...",
  senderId: "uuid",           // Important: So sánh với currentUser.id
  textContent: "Hello!",
  imageUrl: null,
  videoUrl: null,
  messageType: "TEXT",
  status: "UNREAD",
  createdAt: "2025-10-28T14:01:00",
  updatedAt: "2025-10-28T14:01:00"
}
```

**Example:**
```javascript
socket.on('receive_message', (message) => {
  console.log('New message:', message);
  
  // Check if sent by me
  const sentByMe = message.senderId === currentUser.id;
  
  // Add to messages list
  addMessageToUI(message, sentByMe);
  
  // Mark as read if not sent by me
  if (!sentByMe) {
    socket.emit('mark_read', message.conversationId);
  }
});
```

---

#### 2. User Joined
Có user vừa join vào conversation.

**Event:** `user_joined`

**Payload:**
```javascript
{
  userId: "uuid",
  message: "User joined the room",
  timestamp: "2025-10-28T14:00:00"
}
```

**Example:**
```javascript
socket.on('user_joined', (data) => {
  console.log(`User ${data.userId} joined`);
  // Show notification: "Seer has joined the session"
  showNotification(data.message);
});
```

---

#### 3. User Left
User rời khỏi conversation.

**Event:** `user_left`

**Payload:**
```javascript
{
  userId: "uuid"
}
```

**Example:**
```javascript
socket.on('user_left', (data) => {
  console.log(`User ${data.userId} left`);
});
```

---

#### 4. Session Activated
Conversation chuyển từ WAITING → ACTIVE (đã đến giờ hẹn).

**Event:** `session_activated`

**Payload:**
```javascript
{
  conversationId: "uuid",
  sessionStartTime: "2025-10-28T14:00:00",
  sessionEndTime: "2025-10-28T15:00:00",
  message: "Your session is now active! You can start chatting.",
  timestamp: "2025-10-28T14:00:00"
}
```

**Example:**
```javascript
socket.on('session_activated', (data) => {
  console.log('Session is now active!');
  
  // Update conversation status in UI
  updateConversationStatus(data.conversationId, 'ACTIVE');
  
  // Show notification
  showNotification(data.message);
  
  // Enable message input
  enableChatInput();
});
```

---

#### 5. Session Canceled
Session bị hủy do vào trễ >10 phút.

**Event:** `session_canceled`

**Payload:**
```javascript
{
  conversationId: "uuid",
  reason: "Customer late >10 minutes",
  canceledBy: "CUSTOMER",  // "CUSTOMER" | "SEER" | "BOTH"
  message: "Session canceled due to late join (>10 minutes)",
  timestamp: "2025-10-28T14:10:00"
}
```

**Example:**
```javascript
socket.on('session_canceled', (data) => {
  console.log('Session canceled:', data.reason);
  
  // Update conversation status
  updateConversationStatus(data.conversationId, 'CANCELLED');
  
  // Show alert
  showAlert({
    title: 'Session Canceled',
    message: data.message,
    type: 'error'
  });
  
  // Redirect to conversations list
  redirectToConversations();
});
```

---

#### 6. Session Ending Soon
Session sắp kết thúc (còn 10 phút).

**Event:** `session_ending_soon`

**Payload:**
```javascript
{
  conversationId: "uuid",
  remainingMinutes: 10,
  canExtend: true,
  message: "Session will end in 10 minutes",
  timestamp: "2025-10-28T14:50:00"
}
```

**Example:**
```javascript
socket.on('session_ending_soon', (data) => {
  console.log(`Session ending in ${data.remainingMinutes} minutes`);
  
  // Show warning banner
  showWarningBanner({
    message: data.message,
    action: data.canExtend ? 'Extend Session' : null
  });
  
  // Start countdown timer
  startCountdown(data.remainingMinutes);
});
```

---

#### 7. Session Ended
Session đã kết thúc.

**Event:** `session_ended`

**Payload:**
```javascript
{
  conversationId: "uuid",
  message: "Session has ended",
  timestamp: "2025-10-28T15:00:00"
}
```

**Example:**
```javascript
socket.on('session_ended', (data) => {
  console.log('Session ended');
  
  // Update conversation status
  updateConversationStatus(data.conversationId, 'ENDED');
  
  // Disable chat input
  disableChatInput();
  
  // Show feedback dialog
  showFeedbackDialog(data.conversationId);
});
```

---

## 🔄 Flow Diagram

### Complete Chat Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    1. USER CREATES BOOKING                      │
│                                                                 │
│  Customer → Create Booking → Payment Success → Status: CONFIRMED│
└─────────────────────┬───────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────────────────┐
│              2. CONVERSATION AUTO-CREATED                       │
│                                                                 │
│  Backend checks booking.scheduledTime:                          │
│  • If future → status: WAITING                                  │
│  • If now/past → status: ACTIVE                                 │
└─────────────────────┬───────────────────────────────────────────┘
                      ↓
           ┌──────────┴──────────┐
           ↓                     ↓
    ┌────────────┐        ┌────────────┐
    │  WAITING   │        │   ACTIVE   │
    └─────┬──────┘        └─────┬──────┘
          │                     │
          ↓                     ↓
   Cronjob checks         Can chat now
   session_start_time
          │
          ↓
   Time reached? → ACTIVE
                      │
                      ↓
┌─────────────────────────────────────────────────────────────────┐
│                  3. USERS JOIN CONVERSATION                     │
│                                                                 │
│  Frontend:                                                      │
│  • Connect to Socket.IO with userId                             │
│  • Emit 'join_conversation' with conversationId                 │
│                                                                 │
│  Backend tracks:                                                │
│  • customerJoinedAt = now                                       │
│  • seerJoinedAt = now                                           │
└─────────────────────┬───────────────────────────────────────────┘
                      ↓
         ┌────────────┴────────────┐
         ↓                         ↓
   Joined on time?           Late >10 mins?
         ↓                         ↓
   Continue chat            Cronjob → CANCELLED
         │
         ↓
┌─────────────────────────────────────────────────────────────────┐
│                    4. CHAT SESSION ACTIVE                       │
│                                                                 │
│  Users can:                                                     │
│  • Send messages (emit 'send_message')                          │
│  • Receive messages (on 'receive_message')                      │
│  • Mark as read (emit 'mark_read')                              │
└─────────────────────┬───────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────────────────┐
│                 5. SESSION ENDING WARNING                       │
│                                                                 │
│  Cronjob checks:                                                │
│  • sessionEndTime - 10 mins                                     │
│  • Emit 'session_ending_soon' to room                           │
│                                                                 │
│  Frontend shows:                                                │
│  • Warning banner                                               │
│  • Countdown timer                                              │
└─────────────────────┬───────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────────────────┐
│                    6. SESSION AUTO-END                          │
│                                                                 │
│  Cronjob checks:                                                │
│  • sessionEndTime reached                                       │
│  • Update: status = ENDED, booking = COMPLETED                  │
│  • Emit 'session_ended' to room                                 │
│                                                                 │
│  Frontend:                                                      │
│  • Disable chat input                                           │
│  • Show feedback dialog                                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 💻 Code Examples

### Complete React Integration

```javascript
import React, { useState, useEffect, useRef } from 'react';
import { io } from 'socket.io-client';

const ChatComponent = ({ conversationId, currentUser }) => {
  const [messages, setMessages] = useState([]);
  const [inputText, setInputText] = useState('');
  const [conversationStatus, setConversationStatus] = useState('ACTIVE');
  const [isConnected, setIsConnected] = useState(false);
  const socketRef = useRef(null);

  // Initialize Socket.IO
  useEffect(() => {
    const socket = io('http://localhost:8081/chat', {
      query: { userId: currentUser.id },
      transports: ['websocket', 'polling']
    });

    socketRef.current = socket;

    // Connection events
    socket.on('connect', () => {
      console.log('Connected to chat server');
      setIsConnected(true);
      
      // Join conversation
      socket.emit('join_conversation', conversationId, (response) => {
        if (response === 'success') {
          console.log('Joined conversation');
          loadMessages();
        } else {
          console.error('Failed to join:', response);
        }
      });
    });

    socket.on('disconnect', () => {
      setIsConnected(false);
    });

    socket.on('connect_error', (error) => {
      console.error('Connection error:', error);
    });

    // Message events
    socket.on('receive_message', (message) => {
      console.log('New message:', message);
      setMessages(prev => [...prev, message]);
      
      // Auto mark as read if not sent by me
      if (message.senderId !== currentUser.id) {
        socket.emit('mark_read', conversationId);
      }
      
      // Auto scroll to bottom
      scrollToBottom();
    });

    // Session events
    socket.on('session_activated', (data) => {
      setConversationStatus('ACTIVE');
      alert(data.message);
    });

    socket.on('session_canceled', (data) => {
      setConversationStatus('CANCELLED');
      alert(`Session canceled: ${data.reason}`);
      // Redirect to conversations list
      window.location.href = '/conversations';
    });

    socket.on('session_ending_soon', (data) => {
      alert(`Warning: Session will end in ${data.remainingMinutes} minutes`);
    });

    socket.on('session_ended', (data) => {
      setConversationStatus('ENDED');
      alert('Session has ended');
    });

    socket.on('user_joined', (data) => {
      console.log('User joined:', data.userId);
    });

    // Cleanup on unmount
    return () => {
      socket.emit('leave_conversation', conversationId);
      socket.disconnect();
    };
  }, [conversationId, currentUser.id]);

  // Load messages from REST API
  const loadMessages = async () => {
    try {
      const response = await fetch(
        `/api/messages/conversation/${conversationId}?page=0&size=50`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );
      const data = await response.json();
      setMessages(data.content);
      scrollToBottom();
    } catch (error) {
      console.error('Failed to load messages:', error);
    }
  };

  // Send message
  const sendMessage = () => {
    if (!inputText.trim()) return;
    
    const message = {
      conversationId: conversationId,
      textContent: inputText
    };

    socketRef.current.emit('send_message', message, (response) => {
      if (response === 'success') {
        setInputText('');
      } else {
        alert('Failed to send message: ' + response);
      }
    });
  };

  // Scroll to bottom
  const scrollToBottom = () => {
    setTimeout(() => {
      const container = document.getElementById('messages-container');
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    }, 100);
  };

  return (
    <div className="chat-container">
      {/* Connection Status */}
      <div className="status-bar">
        <span className={isConnected ? 'connected' : 'disconnected'}>
          {isConnected ? '🟢 Connected' : '🔴 Disconnected'}
        </span>
        <span>Status: {conversationStatus}</span>
      </div>

      {/* Messages List */}
      <div id="messages-container" className="messages-list">
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={msg.senderId === currentUser.id ? 'message-sent' : 'message-received'}
          >
            <div className="message-sender">
              {msg.senderId === currentUser.id ? 'You' : msg.customerName || msg.seerName}
            </div>
            <div className="message-content">{msg.textContent}</div>
            <div className="message-time">
              {new Date(msg.createdAt).toLocaleTimeString()}
            </div>
          </div>
        ))}
      </div>

      {/* Input Area */}
      <div className="message-input">
        <input
          type="text"
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
          placeholder={
            conversationStatus === 'WAITING' 
              ? 'Waiting for session to start...' 
              : conversationStatus === 'ENDED' 
              ? 'Session has ended' 
              : 'Type a message...'
          }
          disabled={conversationStatus !== 'ACTIVE'}
        />
        <button 
          onClick={sendMessage}
          disabled={conversationStatus !== 'ACTIVE' || !inputText.trim()}
        >
          Send
        </button>
      </div>
    </div>
  );
};

export default ChatComponent;
```

---

### Vue.js Integration

```javascript
<template>
  <div class="chat-container">
    <div class="status-bar">
      <span :class="isConnected ? 'connected' : 'disconnected'">
        {{ isConnected ? '🟢 Connected' : '🔴 Disconnected' }}
      </span>
      <span>Status: {{ conversationStatus }}</span>
    </div>

    <div ref="messagesContainer" class="messages-list">
      <div
        v-for="msg in messages"
        :key="msg.id"
        :class="msg.senderId === currentUser.id ? 'message-sent' : 'message-received'"
      >
        <div class="message-sender">
          {{ msg.senderId === currentUser.id ? 'You' : (msg.customerName || msg.seerName) }}
        </div>
        <div class="message-content">{{ msg.textContent }}</div>
        <div class="message-time">
          {{ formatTime(msg.createdAt) }}
        </div>
      </div>
    </div>

    <div class="message-input">
      <input
        v-model="inputText"
        @keypress.enter="sendMessage"
        :placeholder="getPlaceholder()"
        :disabled="conversationStatus !== 'ACTIVE'"
      />
      <button 
        @click="sendMessage"
        :disabled="conversationStatus !== 'ACTIVE' || !inputText.trim()"
      >
        Send
      </button>
    </div>
  </div>
</template>

<script>
import { io } from 'socket.io-client';

export default {
  props: {
    conversationId: String,
    currentUser: Object
  },
  data() {
    return {
      socket: null,
      messages: [],
      inputText: '',
      conversationStatus: 'ACTIVE',
      isConnected: false
    };
  },
  mounted() {
    this.initSocket();
  },
  beforeUnmount() {
    if (this.socket) {
      this.socket.emit('leave_conversation', this.conversationId);
      this.socket.disconnect();
    }
  },
  methods: {
    initSocket() {
      this.socket = io('http://localhost:8081/chat', {
        query: { userId: this.currentUser.id }
      });

      this.socket.on('connect', () => {
        this.isConnected = true;
        this.joinConversation();
      });

      this.socket.on('disconnect', () => {
        this.isConnected = false;
      });

      this.socket.on('receive_message', (message) => {
        this.messages.push(message);
        if (message.senderId !== this.currentUser.id) {
          this.socket.emit('mark_read', this.conversationId);
        }
        this.$nextTick(() => this.scrollToBottom());
      });

      this.socket.on('session_activated', (data) => {
        this.conversationStatus = 'ACTIVE';
        alert(data.message);
      });

      this.socket.on('session_canceled', (data) => {
        this.conversationStatus = 'CANCELLED';
        alert(`Session canceled: ${data.reason}`);
        this.$router.push('/conversations');
      });

      this.socket.on('session_ending_soon', (data) => {
        alert(`Warning: ${data.message}`);
      });

      this.socket.on('session_ended', (data) => {
        this.conversationStatus = 'ENDED';
        alert('Session has ended');
      });
    },

    joinConversation() {
      this.socket.emit('join_conversation', this.conversationId, (response) => {
        if (response === 'success') {
          this.loadMessages();
        }
      });
    },

    async loadMessages() {
      const response = await fetch(
        `/api/messages/conversation/${this.conversationId}?size=50`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );
      const data = await response.json();
      this.messages = data.content;
      this.$nextTick(() => this.scrollToBottom());
    },

    sendMessage() {
      if (!this.inputText.trim()) return;

      const message = {
        conversationId: this.conversationId,
        textContent: this.inputText
      };

      this.socket.emit('send_message', message, (response) => {
        if (response === 'success') {
          this.inputText = '';
        } else {
          alert('Failed to send: ' + response);
        }
      });
    },

    scrollToBottom() {
      const container = this.$refs.messagesContainer;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    },

    formatTime(dateString) {
      return new Date(dateString).toLocaleTimeString();
    },

    getPlaceholder() {
      if (this.conversationStatus === 'WAITING') {
        return 'Waiting for session to start...';
      } else if (this.conversationStatus === 'ENDED') {
        return 'Session has ended';
      } else {
        return 'Type a message...';
      }
    }
  }
};
</script>
```

---

## ⚠️ Error Handling

### Common Errors

#### 1. "You are not authorized"
**Cause:** User không phải participant của conversation

**Solution:**
```javascript
socket.on('join_conversation_error', (error) => {
  if (error.includes('unauthorized')) {
    alert('You are not authorized to join this conversation');
    window.location.href = '/conversations';
  }
});
```

#### 2. "Conversation not found"
**Cause:** Invalid conversationId

**Solution:**
- Verify conversationId từ REST API trước
- Check response status

#### 3. Connection timeout
**Cause:** Network issues hoặc server down

**Solution:**
```javascript
socket.on('connect_error', (error) => {
  console.error('Connection error:', error);
  showRetryDialog();
});

const showRetryDialog = () => {
  if (confirm('Cannot connect to chat server. Retry?')) {
    socket.connect();
  }
};
```

#### 4. Cannot send message in WAITING status
**Cause:** Conversation chưa ACTIVE

**Solution:**
```javascript
const sendMessage = () => {
  if (conversationStatus !== 'ACTIVE') {
    alert('Cannot send message. Session is not active yet.');
    return;
  }
  // ... send logic
};
```

---

## 🧪 Testing

### Manual Testing Checklist

#### Test Case 1: Join Conversation (ACTIVE)
1. ✅ Open chat page with valid conversationId
2. ✅ Verify Socket.IO connected
3. ✅ Verify joined conversation successfully
4. ✅ Verify messages loaded from REST API
5. ✅ Verify can send message
6. ✅ Verify message appears in UI immediately
7. ✅ Verify other user receives message

#### Test Case 2: Join Conversation (WAITING)
1. ✅ Create booking with future scheduled_time
2. ✅ Open chat page
3. ✅ Verify status = WAITING
4. ✅ Verify message input is disabled
5. ✅ Wait until scheduled_time
6. ✅ Verify receives 'session_activated' event
7. ✅ Verify status changes to ACTIVE
8. ✅ Verify can now send messages

#### Test Case 3: Late Join (>10 mins)
1. ✅ Create conversation with sessionStartTime in past
2. ✅ Don't join for 11 minutes
3. ✅ Verify receives 'session_canceled' event
4. ✅ Verify status = CANCELLED
5. ✅ Verify redirected to conversations list

#### Test Case 4: Session Ending
1. ✅ Join active conversation
2. ✅ Wait until 10 mins before end
3. ✅ Verify receives 'session_ending_soon' event
4. ✅ Verify warning banner appears
5. ✅ Wait until session end
6. ✅ Verify receives 'session_ended' event
7. ✅ Verify message input disabled

### Browser Console Testing

```javascript
// Test in browser console
const socket = io('http://localhost:8081/chat', {
  query: { userId: 'your-user-id' }
});

socket.on('connect', () => {
  console.log('✅ Connected');
  
  // Join conversation
  socket.emit('join_conversation', 'conversation-id', (response) => {
    console.log('Join response:', response);
  });
});

socket.on('receive_message', (msg) => {
  console.log('📩 New message:', msg);
});

// Send test message
socket.emit('send_message', {
  conversationId: 'conversation-id',
  textContent: 'Test message'
}, (response) => {
  console.log('Send response:', response);
});
```

---

## 📊 Conversation Status States

| Status | Có thể Join? | Có thể Chat? | Mô tả |
|--------|-------------|-------------|-------|
| `WAITING` | ✅ Yes | ❌ No | Chờ đến giờ hẹn, có thể xem nhưng không chat được |
| `ACTIVE` | ✅ Yes | ✅ Yes | Đang active, có thể chat bình thường |
| `ENDED` | ✅ Yes | ❌ No | Đã kết thúc, chỉ xem lại messages |
| `CANCELLED` | ✅ Yes | ❌ No | Đã bị hủy, chỉ xem lại messages |

---

## 🎨 UI/UX Recommendations

### 1. Connection Status Indicator
```
🟢 Connected  →  Có thể chat
🟡 Connecting →  Đang kết nối...
🔴 Disconnected → Mất kết nối
```

### 2. Message Status Icons
```
✓  Sent (1 checkmark)
✓✓ Delivered (2 checkmarks)
✓✓ Read (2 blue checkmarks)
```

### 3. Typing Indicator
```javascript
// Optional: Implement typing indicator
socket.emit('typing', { conversationId, isTyping: true });

socket.on('user_typing', (data) => {
  showTypingIndicator(data.userId);
});
```

### 4. Session Timer Display
```javascript
// Show remaining time in header
const calculateRemainingTime = (sessionEndTime) => {
  const now = new Date();
  const end = new Date(sessionEndTime);
  const diff = end - now;
  
  const minutes = Math.floor(diff / 60000);
  return minutes > 0 ? `${minutes} mins left` : 'Session ending soon';
};
```

---

## 📝 Summary

### Quick Start Steps
1. **Install Socket.IO client**: `npm install socket.io-client`
2. **Connect to server**: `io('http://localhost:8081/chat', { query: { userId } })`
3. **Join conversation**: `socket.emit('join_conversation', conversationId)`
4. **Load messages**: REST API `/api/messages/conversation/{id}`
5. **Listen for messages**: `socket.on('receive_message', callback)`
6. **Send messages**: `socket.emit('send_message', messageData)`
7. **Handle session events**: Listen to activation, cancellation, ending

### Important Notes
- ⚠️ Always pass `userId` in Socket.IO query params
- ⚠️ Must join conversation before sending messages
- ⚠️ WAITING status = can't chat yet
- ⚠️ Late join >10 mins = auto cancel
- ⚠️ Handle all session events for best UX

---

**Need help?** Contact backend team or check source code at:
- `ChatSocketListener.java` - Socket.IO event handlers
- `ConversationScheduler.java` - Cronjob logic
- `MessageService.java` - Message business logic

