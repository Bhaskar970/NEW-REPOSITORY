# 🏋️ IronPulse — Gym Management System

A full-stack Gym Management System built with **pure Java (JDBC)**, **MySQL**, and **HTML/CSS/JS** — no Servlets, no Spring Boot, no frameworks. Runs entirely from VS Code.

---

## 📁 Project Structure

```
GymManagementSystem/
├── src/
│   └── com/gym/
│       ├── dao/
│       │   ├── UserDAO.java        ← JDBC ops for users
│       │   ├── SlotDAO.java        ← JDBC ops for slots
│       │   └── BookingDAO.java     ← JDBC ops + overcrowding lock
│       ├── model/
│       │   ├── User.java
│       │   ├── Slot.java
│       │   └── Booking.java
│       ├── server/
│       │   └── GymServer.java      ← HTTP server (com.sun.net.httpserver)
│       └── util/
│           ├── DBConnection.java   ← MySQL singleton connection
│           └── JsonUtil.java       ← Lightweight JSON builder
├── frontend/
│   ├── index.html                  ← Single-page application
│   ├── css/style.css
│   └── js/app.js
├── database/
│   └── gym_schema.sql              ← Run this in MySQL first
├── lib/
│   └── mysql-connector-j.jar       ← You provide this (see Step 2)
├── .vscode/
│   ├── tasks.json                  ← Ctrl+Shift+B to build & run
│   ├── launch.json                 ← F5 to debug
│   └── settings.json
├── build.sh                        ← macOS / Linux build script
├── build.bat                       ← Windows build script
└── README.md
```

---

## ⚙️ Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| JDK  | 17+     | https://adoptium.net |
| MySQL | 8.0+  | https://dev.mysql.com/downloads/ |
| VS Code | any | https://code.visualstudio.com |
| MySQL JDBC Driver | latest | https://dev.mysql.com/downloads/connector/j/ |

**Recommended VS Code Extensions:**
- Extension Pack for Java (`vscjava.vscode-java-pack`)

---

## 🚀 Setup — Step by Step

### Step 1 — Clone / Extract the project

Open the `GymManagementSystem` folder in VS Code:
```
File → Open Folder → GymManagementSystem
```

---

### Step 2 — Add MySQL JDBC Driver

1. Download **MySQL Connector/J** from:  
   https://dev.mysql.com/downloads/connector/j/  
   (Choose *Platform Independent* → ZIP)

2. Extract and copy the JAR file into the project's `lib/` folder:
   ```
   GymManagementSystem/lib/mysql-connector-j.jar
   ```
   > The exact JAR filename may include a version number like  
   > `mysql-connector-j-8.3.0.jar` — rename it to `mysql-connector-j.jar`

---

### Step 3 — Create the Database

Open **MySQL Workbench** (or any MySQL client) and run:

```sql
source /path/to/GymManagementSystem/database/gym_schema.sql
```

Or paste the contents of `database/gym_schema.sql` into your client and execute.

This creates:
- `gym_management` database
- `users`, `slots`, `bookings` tables
- An admin account: **admin@gym.com / admin123**
- Sample slots seeded for today + next 7 days

---

### Step 4 — Configure Database Password

Open `src/com/gym/util/DBConnection.java` and update line 12:

```java
private static final String PASSWORD = "your_mysql_password";
```

Change `your_mysql_password` to your actual MySQL root password.

If your MySQL username is not `root`, update the `USER` constant too.

---

### Step 5 — Build & Run

**Option A — VS Code (recommended):**
```
Press Ctrl+Shift+B  (or Cmd+Shift+B on Mac)
→ Select "Build & Run Gym Server"
```

**Option B — Terminal (macOS/Linux):**
```bash
cd GymManagementSystem
chmod +x build.sh
bash build.sh
```

**Option C — Terminal (Windows):**
```cmd
cd GymManagementSystem
build.bat
```

You should see:
```
╔══════════════════════════════════════╗
║  Gym Management System started       ║
║  http://localhost:8080               ║
╚══════════════════════════════════════╝
```

---

### Step 6 — Open in Browser

Navigate to: **http://localhost:8080**

---

## 🔐 Default Accounts

| Role   | Email           | Password  |
|--------|-----------------|-----------|
| Admin  | admin@gym.com   | admin123  |
| Member | Register via UI | your own  |

---

## 📡 REST API Reference

All requests go to `http://localhost:8080`.

| Method | Endpoint               | Description                        |
|--------|------------------------|------------------------------------|
| POST   | `/api/register`        | Register a new member              |
| POST   | `/api/login`           | Login and receive user object      |
| GET    | `/api/slots?date=`     | Get slots for a specific date      |
| GET    | `/api/slots/all`       | Get all slots (admin)              |
| POST   | `/api/slots`           | Add a new slot (admin)             |
| POST   | `/api/book`            | Book a slot for a user             |
| GET    | `/api/bookings?userId=`| Get a user's bookings              |
| GET    | `/api/bookings/all`    | Get all bookings (admin)           |
| POST   | `/api/cancel`          | Cancel a booking                   |
| GET    | `/api/users`           | Get all users (admin)              |
| DELETE | `/api/users?id=`       | Delete a user (admin)              |

**Example — Register:**
```bash
curl -X POST http://localhost:8080/api/register \
  -d "name=John&email=john@example.com&phone=9876543210&password=pass123"
```

**Example — Book a Slot:**
```bash
curl -X POST http://localhost:8080/api/book \
  -d "userId=2&slotId=3"
```

---

## 🛡️ Overcrowding Prevention

The `BookingDAO.bookSlot()` method uses a **MySQL transaction with `SELECT … FOR UPDATE`** to prevent race conditions:

1. The slot row is **locked** before reading capacity
2. Current booking count is checked against `max_capacity`
3. If full → returns code `1` (slot full)
4. If duplicate booking → returns code `2`
5. Only if both checks pass → booking is inserted and committed

This means even if 50 users click "Book" simultaneously, the database lock ensures no slot ever exceeds its capacity.

---

## 🖥️ Features Summary

| Feature | Details |
|---------|---------|
| User Registration | Name, email, phone, password stored in MySQL |
| User Login | Credential validation against DB |
| Slot Booking | Select date → view available slots → book |
| Overcrowding Prevention | Transaction-locked capacity check |
| Booking Cancellation | Member can cancel own bookings |
| Dashboard | Stats: total / confirmed / cancelled |
| Admin — Members | View all users, delete members |
| Admin — Slots | Add new slots, view all, delete |
| Admin — Bookings | View all bookings, cancel any |

---

## 🐛 Troubleshooting

**"MySQL JDBC Driver not found"**  
→ Ensure `lib/mysql-connector-j.jar` exists and the filename matches exactly.

**"Connection refused" to MySQL**  
→ Make sure MySQL service is running. Check password in `DBConnection.java`.

**Port 8080 already in use**  
→ Change `PORT = 8080` in `GymServer.java` to another port (e.g. 9090).

**`Access denied for user 'root'`**  
→ Verify MySQL credentials. Try logging in via terminal: `mysql -u root -p`

**Blank page in browser**  
→ Ensure the server terminal shows "Connected to MySQL successfully."  
→ Check browser console (F12) for errors.
