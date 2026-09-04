# Secure LAN Chat & Collaborative Whiteboard Application

A Core Java server-client application for real-time LAN communication and interactive whiteboard collaboration. Built from scratch with a custom IoC framework, annotation-driven routing, multi-threaded TCP/WebSocket communication, RSA payload security, and MySQL database authentication.

---

## 🌟 Features

- **Custom Core Java Framework**:
  - **IoC Container**: Lightweight dependency injection framework (`ApplicationContext`).
  - **Annotation-Driven Routing**: Spring-like annotations (`@RestController`, `@PostMapping`, `@GetMapping`, `@RequestParam`, `@SecurePayload`).
  - **Custom Protocol Engine**: HTTP parsing, custom TCP routing, and WebSocket frame encoding/decoding.
- **User Authentication & Persistence**:
  - Registration and login backed by a MySQL database (`chat_app_db`).
- **Real-Time LAN Chat**:
  - Broadcast and user-to-user messaging across clients over TCP socket threads.
- **Secure RSA Messaging**:
  - Cryptographic key generation (`IdentityManager`) for digital signature signing and verification.
- **Collaborative Whiteboard / Canvas**:
  - Real-time shared canvas rooms for interactive drawing across connected LAN clients.
  - Controls for stroke width, color selection, room joining, and clearing canvas.

---

## 📁 Project Structure

```text
Chat_Application/
├── schema.sql                 # Database setup script for MySQL
├── src/
│   ├── Client/                # TCP Client connection & message handler
│   ├── Controller/            # Controller endpoints handling requests
│   ├── Db/                    # Database configurations & user DAO
│   ├── Framework/             # Custom IoC container, router, annotations, WebSocket parser
│   ├── Security/              # RSA identity & digital signature management
│   ├── Server/                # Core TCP server entry point & socket listener
│   ├── Services/              # Business logic services
│   ├── Thread/                # Socket reader/writer thread handler
│   └── gui/                   # Swing GUI (Login, Register, Chat, Whiteboard)
└── lib/                       # Dependencies (MySQL Connector JDBC, etc.)
```

---

## 🛠️ Prerequisites & Setup

### 1. Database Setup
Ensure MySQL is running on your machine, then execute `schema.sql` to create the database and required tables:

```bash
mysql -u root -p < schema.sql
```

### 2. Configure Database Credentials
Update your MySQL database credentials in `src/Db/DatabaseConfig.java` if necessary.

---

## 🚀 How to Run

### Step 1: Launch Server
Compile and run the server entry point:
```bash
# Run Server.java main method
src/Server/Server.java
```
*The server will start listening on port `5000`.*

### Step 2: Launch Client GUI
Run the Swing Client GUI on one or more LAN client machines:
```bash
# Run MainFrame.java main method
src/gui/MainFrame.java
```

1. **Register / Login**: Create an account or log in.
2. **Chat Tab**: Communicate in real-time with connected LAN users.
3. **Canvas Tab**: Join a whiteboard room to draw interactively with other users.
