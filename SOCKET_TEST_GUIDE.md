# Socket.IO Testing Guide - Quick Start

## 🚀 Quick Test Commands

### For Windows (localhost - Spring Boot running directly)
```
http://localhost:8080/socket-test.html
```

### For Docker Environment
```
http://localhost:8080/socket-test.html?server=http://localhost:8081
```

---

## 📋 Step-by-Step Guide

### 1. Start the Application

#### Option A: Docker (Recommended)
```cmd
docker-test.bat
```
Or manually:
```cmd
cd docker
docker-compose down
docker-compose up --build -d
```

#### Option B: Local Spring Boot
```cmd
.\mvnw spring-boot:run
```

### 2. Test Socket.IO Connection

Open in browser:
- **Socket.IO Test**: http://localhost:8080/socket-test.html
- **Conversations Page**: http://localhost:8080/conversations.html
- **Chat Room**: http://localhost:8080/chat-room.html?id=<conversation-id>
- **Admin Chat**: http://localhost:8080/admin-chat.html

### 3. Check Expected Results

✅ **Success indicators:**
- Status shows: "✅ SUCCESS - Socket.IO working!"
- Console logs: "Connected successfully!"
- Socket ID is displayed

❌ **Failure indicators:**
- "⚠️ Library OK, but server not available" → Server not running or port not exposed
- "Connection error" → Wrong URL or firewall blocking

---

## 🐳 Docker-Specific Configuration

### Required Files

**1. `.env` file:**
```env
SOCKET_IO_PORT=8081
SOCKET_IO_HOST=0.0.0.0
```
⚠️ **Important**: `SOCKET_IO_HOST` MUST be `0.0.0.0` in Docker (not `localhost`)

**2. `docker-compose.yaml`:**
```yaml
ports:
  - ${SERVER_PORT}:${SERVER_PORT}
  - ${SOCKET_IO_PORT}:${SOCKET_IO_PORT}  # Must expose Socket.IO port
```

### Verify Docker Setup

```cmd
# Check if containers are running
docker-compose ps

# Check logs
docker-compose logs -f spring-app

# Check if ports are open
netstat -an | findstr ":8081"
```

---

## 🔧 URL Parameters for Testing

All HTML test pages support URL parameters:

### For Socket.IO Server URL:
```
?socketServer=http://localhost:8081
?socketServer=http://192.168.1.100:8081
```

### For API Base URL (admin-chat.html only):
```
?apiBase=http://localhost:8080&socketServer=http://localhost:8081
```

### Examples:

**Test from another computer on same network:**
```
http://192.168.1.100:8080/socket-test.html?server=http://192.168.1.100:8081
```

**Test Docker environment:**
```
http://localhost:8080/conversations.html?socketServer=http://localhost:8081
```

---

## 🐛 Troubleshooting

### Problem 1: "Server not available"
**Symptoms:** Socket.IO library loads but can't connect

**Solutions:**
1. Check if Spring Boot is running:
   ```cmd
   netstat -an | findstr ":8080 :8081"
   ```
2. In Docker, verify port is exposed in `docker-compose.yaml`
3. Check firewall settings
4. Verify `.env` has `SOCKET_IO_HOST=0.0.0.0`

### Problem 2: "Version mismatch"
**Symptoms:** Error about Socket.IO v2.x vs v3.x

**Solution:** Already fixed - all HTML files use Socket.IO v2.5.0

### Problem 3: CORS errors
**Symptoms:** Browser console shows CORS policy errors

**Solution:** Already configured - `SocketIOConfig.java` has `setOrigin("*")`

### Problem 4: Connection timeout
**Symptoms:** Hangs for 10+ seconds then fails

**Solutions:**
1. Check server logs for errors:
   ```cmd
   docker-compose logs -f spring-app
   ```
2. Increase timeout in HTML files (already set to 10s)
3. Check network connectivity

---

## 📝 Log Examples

### Successful Connection
```
Loading Socket.IO library...
✅ Socket.IO library loaded successfully
Attempting to connect to http://localhost:8081/chat...
✅ Connected successfully!
Socket ID: abc123def456
✅ SUCCESS - Socket.IO working!
```

### Server Not Available
```
Loading Socket.IO library...
✅ Socket.IO library loaded successfully
Attempting to connect to http://localhost:8081/chat...
❌ Connection error: Error: timeout
⚠️ Library OK, but server not available
```

---

## 📚 Files Reference

| File | Purpose |
|------|---------|
| `socket-test.html` | Basic Socket.IO connection test |
| `conversations.html` | List all conversations with real-time updates |
| `chat-room.html` | Chat room for seer/customer |
| `admin-chat.html` | Admin chat interface |
| `DOCKER_SOCKET_TEST.md` | Detailed Docker troubleshooting |
| `docker-test.bat` | Windows script to rebuild and test Docker |

---

## 🎯 Quick Checklist

Before testing, ensure:
- [ ] `.env` file exists with correct values
- [ ] `SOCKET_IO_HOST=0.0.0.0` in `.env`
- [ ] `SOCKET_IO_PORT=8081` in `.env`
- [ ] Port 8081 is exposed in `docker-compose.yaml`
- [ ] Docker containers are running: `docker-compose ps`
- [ ] Ports are listening: `netstat -an | findstr ":8081"`

---

## 💡 Tips

1. **Always check logs first:**
   ```cmd
   docker-compose logs -f spring-app
   ```

2. **Test Socket.IO before testing chat:**
   - Use `socket-test.html` first
   - Then try `conversations.html`
   - Finally test actual chat features

3. **Use browser DevTools:**
   - Open Console (F12)
   - Watch for connection logs
   - Check Network tab for WebSocket connections

4. **Test incrementally:**
   - First: Socket.IO connection
   - Second: Authentication
   - Third: Join conversation
   - Fourth: Send/receive messages

---

## 🔗 Related Documentation

- `README_CHAT.md` - Chat system overview
- `CHAT_INTEGRATION_GUIDE.md` - Frontend integration guide
- `ADMIN_CHAT_GUIDE.md` - Admin chat features
- `DOCKER_SOCKET_TEST.md` - Detailed Docker troubleshooting

---

## 📞 Support

If Socket.IO still doesn't work after following this guide:

1. Check all files in this project
2. Verify `.env` configuration
3. Review `SocketIOConfig.java`
4. Check Docker logs
5. Verify network connectivity

