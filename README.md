# 🚀 ProjectPulse — Full-Stack Project Management System

A complete SaaS-style project management web application built with **Spring Boot 3.2.5**, **Thymeleaf**, **Spring Security 6**, **PostgreSQL**, and **WebSockets**.

---

## 📋 Tech Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Backend    | Java 17, Spring Boot 3.2.5          |
| Frontend   | Thymeleaf + Bootstrap Icons + CSS   |
| Security   | Spring Security 6 (Session-based)   |
| Database   | PostgreSQL                          |
| ORM        | Spring Data JPA (Hibernate)         |
| Real-time  | WebSocket (STOMP + SockJS)          |
| Build      | Maven                               |

---

## 👥 Roles

| Role   | Capabilities                                                  |
|--------|---------------------------------------------------------------|
| ADMIN  | All access: users, projects, tasks, system overview           |
| LEADER | Create/manage projects, assign tasks, review submissions      |
| MEMBER | View assigned tasks, update status, upload files, chat        |

---

## ✅ Features

- 🔐 Session-based authentication with BCrypt password encoding
- 📁 Full project CRUD with leader/member assignment
- ✅ Task management with priorities, statuses, pin feature, and filtering
- 📎 File upload and submission review (approve/reject)
- 💬 Real-time team chat per project (WebSocket + HTTP fallback)
- 📅 Monthly calendar view with task deadline mapping
- 📊 Role-aware dashboards with stats
- 👤 User profile management and password change
- 🛡️ Admin panel: create users, change roles, enable/disable

---

## ⚙️ Setup Instructions

### 1. Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### 2. Create PostgreSQL Database

```sql
CREATE DATABASE projectpulse_db;
CREATE USER postgres WITH PASSWORD 'your_password_here';
GRANT ALL PRIVILEGES ON DATABASE projectpulse_db TO postgres;
```

### 3. Configure application.properties

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/projectpulse_db
spring.datasource.username=postgres
spring.datasource.password=your_password_here
app.upload.dir=./uploads
```

### 4. Build and Run

```bash
# Clone/extract project
cd projectpulse

# Build
mvn clean install -DskipTests

# Run
mvn spring-boot:run
```

### 5. Open in Browser

```
http://localhost:8080
```

---

## 🔑 Demo Credentials (auto-seeded on first run)

| Username  | Password     | Role   |
|-----------|--------------|--------|
| admin     | Admin@123    | Admin  |
| leader1   | Leader@123   | Leader |
| member1   | Member@123   | Member |
| member2   | Member@123   | Member |

A demo project **"IoT-Based Areca Nut Monitoring System"** with 5 tasks is also seeded automatically.

---

## 📁 Project Structure

```
src/main/java/com/projectpulse/
├── config/
│   ├── SecurityConfig.java          # Spring Security configuration
│   ├── UserDetailsServiceImpl.java  # DB-backed user auth
│   ├── WebSocketConfig.java         # STOMP WebSocket setup
│   ├── MvcConfig.java               # Static file serving
│   └── DataInitializer.java         # Seed roles + demo data
├── controller/
│   ├── AuthController.java
│   ├── DashboardController.java
│   ├── ProjectController.java
│   ├── TaskController.java
│   ├── SubmissionController.java
│   ├── ChatController.java
│   ├── CalendarController.java
│   ├── AdminController.java
│   └── ProfileController.java
├── service/
│   ├── UserService.java
│   ├── ProjectService.java
│   ├── TaskService.java
│   ├── SubmissionService.java
│   ├── ChatService.java
│   └── FileStorageService.java
├── entity/
│   ├── User.java
│   ├── Role.java
│   ├── Project.java
│   ├── Task.java
│   ├── TaskSubmission.java
│   └── ChatMessage.java
├── repository/          # Spring Data JPA interfaces
├── dto/                 # Form binding objects
├── exception/           # Custom exceptions + GlobalExceptionHandler
└── util/
    └── SecurityUtils.java

src/main/resources/
├── templates/
│   ├── auth/            login.html, register.html
│   ├── fragments/       sidebar.html, topbar.html
│   ├── projects/        list.html, detail.html, form.html
│   ├── tasks/           list.html, detail.html, form.html
│   ├── submissions/     list.html, upload.html, review.html, task-submissions.html
│   ├── chat/            room.html
│   ├── calendar/        view.html
│   ├── admin/           dashboard.html, users.html, user-detail.html, user-form.html
│   ├── shared/          profile.html
│   ├── error/           error.html
│   └── dashboard.html
├── static/
│   ├── css/app.css
│   └── js/app.js, chat.js
└── application.properties
```

---

## 🗄️ Database Tables (auto-created by Hibernate)

- `users` — user accounts
- `roles` — ROLE_ADMIN, ROLE_LEADER, ROLE_MEMBER
- `user_roles` — many-to-many join
- `projects` — project records
- `project_members` — many-to-many join
- `tasks` — task records with priority, status, pin
- `task_submissions` — file submissions per task
- `chat_messages` — project-scoped messages

---

## 📝 URL Reference

| URL                          | Access         | Description               |
|------------------------------|----------------|---------------------------|
| `/auth/login`                | Public         | Login page                |
| `/auth/register`             | Public         | Self-registration         |
| `/dashboard`                 | Authenticated  | Role-aware dashboard      |
| `/projects`                  | Authenticated  | Project list              |
| `/tasks?projectId={id}`      | Authenticated  | Task list for project     |
| `/submissions`               | Leader/Admin   | Pending submission review |
| `/chat/{projectId}`          | Authenticated  | Real-time project chat    |
| `/calendar`                  | Authenticated  | Monthly deadline calendar |
| `/profile`                   | Authenticated  | Edit own profile          |
| `/admin`                     | Admin only     | Admin dashboard           |
| `/admin/users`               | Admin only     | User management           |

---

## 🏆 Resume-Ready Features

- Role-Based Access Control (RBAC) with Spring Security 6
- Real-time messaging with WebSocket (STOMP protocol)
- File upload system with UUID-based secure storage
- Multi-layer architecture: Controller → Service → Repository
- Global exception handling and custom error pages
- Responsive UI with custom CSS design system
- Hibernate auto-DDL with seed data on startup
