
# 🚀 ProjectPulse

> A full-stack SaaS-style Project Management System with role-based workflows, real-time chat, and file submission approval — built with Java Spring Boot.



![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)




![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)




![Spring Security](https://img.shields.io/badge/Spring%20Security-6.2.4-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)




![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1.2-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)




![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-316192?style=for-the-badge&logo=postgresql&logoColor=white)




![Hibernate](https://img.shields.io/badge/Hibernate-6.4.4-59666C?style=for-the-badge&logo=hibernate&logoColor=white)




![WebSocket](https://img.shields.io/badge/WebSocket-STOMP%20%2B%20SockJS-010101?style=for-the-badge&logo=socket.io&logoColor=white)




![Maven](https://img.shields.io/badge/Maven-3.8+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)




![Lombok](https://img.shields.io/badge/Lombok-1.18.32-C71A36?style=for-the-badge&logo=lombok&logoColor=white)



---

## 📖 Overview

ProjectPulse is a production-grade, multi-role project management web application where Admins manage users, Project Leaders create and oversee projects, and Team Members execute tasks and submit work for review. It implements a complete task lifecycle — from creation and assignment through submission, approval or rejection — with real-time per-project team chat powered by WebSockets. Built with a clean layered MVC architecture, it demonstrates enterprise Java patterns including role-based access control, secure file handling, and eager-fetch JPQL queries to eliminate N+1 and lazy loading issues.

---

## ✨ Features

- **Role-Based Access Control** — Three distinct roles (`ROLE_ADMIN`, `ROLE_LEADER`, `ROLE_MEMBER`) enforced via Spring Security 6, each with dedicated dashboards and URL-level restrictions
- **Project Management** — Full CRUD for projects with leader assignment, multi-member selection, deadline tracking, status management (`ACTIVE`, `COMPLETED`, `ON_HOLD`, `CANCELLED`), and progress percentage computed from task completion
- **Task Lifecycle** — Tasks progress through `TODO → IN_PROGRESS → SUBMITTED → APPROVED / REJECTED` with priority levels (`LOW`, `MEDIUM`, `HIGH`) and a pin/highlight feature for important tasks
- **Task Filtering** — Filter tasks per project by status, priority, or pinned state
- **File Submission & Review** — Team members upload files against tasks; leaders review, approve, or reject with written feedback; full submission history per task; secure UUID-based storage with path traversal protection
- **File Download** — Leaders and submitters can download submitted files directly from the review panel
- **Real-Time Team Chat** — Per-project WebSocket chat using STOMP + SockJS with HTTP POST fallback; messages persist to the database with sender name and timestamp
- **Monthly Calendar** — Deadline calendar grouping tasks by day with priority colour-coding and pinned task indicators; filterable by project
- **Role-Aware Dashboards** — Admin sees system-wide stats; Leader sees pending submissions queue and project progress; Member sees active task list and upcoming deadlines
- **Admin Panel** — Create users, assign/change roles, enable or disable accounts
- **User Profile** — Edit full name, bio, phone; change password with current password verification
- **BCrypt Password Encryption** — Strength-12 BCrypt hashing for all stored passwords
- **CSRF Protection** — Enabled by default through Spring Security's filter chain
- **Session Management** — 60-minute session timeout with concurrent session control (1 session per user)
- **Global Exception Handling** — `@ControllerAdvice` with custom error pages for 403, 404, and 500 errors
- **Demo Data Seeding** — Roles, four demo users, a sample project, and five tasks auto-seeded on first startup via `DataInitializer`
- **Responsive Custom UI** — CSS design system built from scratch with CSS variables, no Bootstrap dependency; Bootstrap Icons for iconography

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         BROWSER (Client)                        │
│   Thymeleaf HTML + CSS (app.css) + JS (app.js, chat.js)        │
│   SockJS / STOMP WebSocket Client                               │
└───────────────────┬─────────────────────────┬───────────────────┘
                    │  HTTP Request            │  WS Frame
                    ▼                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                     SPRING BOOT APPLICATION                     │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              Spring Security Filter Chain               │   │
│  │  BCrypt · Session · CSRF · Role URL Authorization       │   │
│  └──────────────────────┬──────────────────────────────────┘   │
│                         │                                       │
│  ┌──────────────────────▼──────────────────────────────────┐   │
│  │                   Controllers (9)                        │   │
│  │  Auth · Dashboard · Project · Task · Submission          │   │
│  │  Chat · Calendar · Admin · Profile                       │   │
│  └──────────────────────┬──────────────────────────────────┘   │
│                         │                                       │
│  ┌──────────────────────▼──────────────────────────────────┐   │
│  │                   Services (6)                           │   │
│  │  User · Project · Task · Submission                      │   │
│  │  FileStorage · Chat                                      │   │
│  └──────────────────────┬──────────────────────────────────┘   │
│                         │                                       │
│  ┌──────────────────────▼──────────────────────────────────┐   │
│  │               Repositories (6) — JPA                    │   │
│  │  Custom JPQL with LEFT JOIN FETCH for eager loading      │   │
│  └──────────────────────┬──────────────────────────────────┘   │
│                         │                                       │
│  ┌──────────────────────▼──────────────────────────────────┐   │
│  │            WebSocket Message Broker (STOMP)              │   │
│  │  /app/chat.send  →  SimpleBroker  →  /topic/chat/{id}   │   │
│  └─────────────────────────────────────────────────────────┘   │
└───────────────────────────────┬─────────────────────────────────┘
                                │  JDBC (HikariCP)
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     PostgreSQL Database                         │
│                                                                 │
│  users ── user_roles ── roles                                   │
│    │                                                            │
│  projects ── project_members ── users                           │
│    │                                                            │
│  tasks ── task_submissions ── users                             │
│    │                                                            │
│  chat_messages ── projects ── users                             │
└─────────────────────────────────────────────────────────────────┘
                                │
                           ./uploads/
                    (UUID-named files on disk)
```

**Request Flow — Step by Step:**

1. Browser sends an HTTP request (or WebSocket frame for chat)
2. Spring Security Filter Chain intercepts — checks session, CSRF token, and role-based URL authorization
3. If authenticated and authorized, the request reaches the appropriate `@Controller`
4. Controller calls the `@Service` layer for business logic (access checks, data transformation)
5. Service calls the `@Repository` (Spring Data JPA) which executes JPQL with `LEFT JOIN FETCH` to load associations eagerly
6. Hibernate translates JPQL to SQL and executes against PostgreSQL via HikariCP connection pool
7. Result entities are returned up the chain; Service maps to DTOs or passes entities directly
8. Controller adds data to the Spring `Model` and returns a Thymeleaf template name
9. Thymeleaf engine renders the HTML, resolving `${...}` expressions and `sec:authorize` attributes
10. For chat: the STOMP broker receives the message at `/app/chat.send`, persists it, and broadcasts to `/topic/chat/{projectId}` — all subscribers receive it in real time

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Web Layer | Spring MVC (`@Controller`, `Model`, `RedirectAttributes`) |
| Security | Spring Security 6.2.4 — session-based auth, BCrypt, CSRF, role URL rules |
| Template Engine | Thymeleaf 3.1.2 + `thymeleaf-extras-springsecurity6` |
| ORM | Spring Data JPA + Hibernate 6.4.4 |
| Database | PostgreSQL 14+ |
| Connection Pool | HikariCP 5.0.1 (bundled with Spring Boot) |
| Real-Time | Spring WebSocket + STOMP + SockJS |
| File Handling | `MultipartFile` → local disk (`./uploads/`) with UUID filenames |
| Validation | Jakarta Bean Validation (`@NotBlank`, `@Email`, `@Size`, `@Future`) |
| Build Tool | Apache Maven 3.8+ |
| Dev Tooling | Spring Boot DevTools (live reload) |
| Boilerplate Reduction | Lombok 1.18.32 (`@Data`, `@RequiredArgsConstructor`, `@Slf4j`) |
| Frontend Icons | Bootstrap Icons 1.11.3 (CDN) |
| Chat Client | SockJS 1.x + StompJS 2.3.3 (CDN) |

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version |
|---|---|
| Java JDK | 17 or 21 |
| Apache Maven | 3.8+ |
| PostgreSQL | 14+ |
| Git | Any recent version |

---

### 1. Clone the repository

```bash
git clone https://github.com/adityakamat2005/projectpulse.git
cd projectpulse
```

---

### 2. Create the PostgreSQL database

Connect to PostgreSQL and run:

```sql
CREATE DATABASE projectpulse_db;
```

---

### 3. Configure application properties

Open `src/main/resources/application.properties` and update the database credentials:

```properties
# Server
server.port=8080

# Database — update username and password to match your PostgreSQL setup
spring.datasource.url=jdbc:postgresql://localhost:5432/projectpulse_db
spring.datasource.username=postgres
spring.datasource.password=your_postgres_password_here
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA — Hibernate will auto-create/update all tables on startup
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=true

# File upload directory — relative to project root
app.upload.dir=./uploads
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=15MB

# Session timeout
server.servlet.session.timeout=60m
```

> All tables (`users`, `roles`, `user_roles`, `projects`, `project_members`, `tasks`, `task_submissions`, `chat_messages`) are created automatically by Hibernate on first startup.

---

### 4. Build the project

```bash
mvn clean install -DskipTests
```

---

### 5. Run the application

```bash
mvn spring-boot:run
```

---

### 6. Open in browser

```
http://localhost:8080
```

The `DataInitializer` seeds all roles and the following demo accounts automatically on first run:

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin@123` | Administrator |
| `leader1` | `Leader@123` | Project Leader |
| `member1` | `Member@123` | Team Member |
| `member2` | `Member@123` | Team Member |

A demo project **"IoT-Based Areca Nut Monitoring System"** with 5 tasks is also seeded.

---

## 📡 URL Routes

| Method | Endpoint | Role Required | Description |
|---|---|---|---|
| `GET` | `/auth/login` | Public | Login page |
| `POST` | `/auth/login` | Public | Process login |
| `GET` | `/auth/register` | Public | Registration page |
| `POST` | `/auth/register` | Public | Process registration |
| `GET` | `/auth/logout` | Authenticated | Logout and invalidate session |
| `GET` | `/dashboard` | Authenticated | Role-aware dashboard |
| `GET` | `/projects` | Authenticated | List all accessible projects |
| `GET` | `/projects/{id}` | Authenticated | Project detail page |
| `GET` | `/projects/new` | Admin / Leader | New project form |
| `POST` | `/projects/new` | Admin / Leader | Create project |
| `GET` | `/projects/{id}/edit` | Admin / Leader | Edit project form |
| `POST` | `/projects/{id}/edit` | Admin / Leader | Update project |
| `POST` | `/projects/{id}/delete` | Admin | Delete project |
| `POST` | `/projects/{id}/status` | Admin / Leader | Update project status |
| `GET` | `/tasks` | Authenticated | Task list for a project (`?projectId=`) |
| `GET` | `/tasks/{id}` | Authenticated | Task detail |
| `GET` | `/tasks/new` | Admin / Leader | New task form |
| `POST` | `/tasks/new` | Admin / Leader | Create task |
| `GET` | `/tasks/{id}/edit` | Admin / Leader | Edit task form |
| `POST` | `/tasks/{id}/edit` | Admin / Leader | Update task |
| `POST` | `/tasks/{id}/status` | Authenticated | Update task status |
| `POST` | `/tasks/{id}/pin` | Admin / Leader | Toggle task pin |
| `POST` | `/tasks/{id}/delete` | Admin / Leader | Delete task |
| `POST` | `/tasks/{id}/approve` | Admin / Leader | Approve task |
| `POST` | `/tasks/{id}/reject` | Admin / Leader | Reject task |
| `GET` | `/submissions` | Admin / Leader | Pending submissions list |
| `GET` | `/submissions/task/{taskId}` | Authenticated | All submissions for a task |
| `GET` | `/submissions/upload/{taskId}` | Member | File upload form |
| `POST` | `/submissions/upload/{taskId}` | Member | Submit file for review |
| `GET` | `/submissions/review/{id}` | Admin / Leader | Review submission page |
| `POST` | `/submissions/review/{id}/approve` | Admin / Leader | Approve submission |
| `POST` | `/submissions/review/{id}/reject` | Admin / Leader | Reject submission |
| `GET` | `/submissions/download/{id}` | Authenticated | Download submitted file |
| `GET` | `/chat/{projectId}` | Authenticated | Project chat room |
| `POST` | `/chat/{projectId}/send` | Authenticated | HTTP fallback message send |
| `WS` | `/ws` (STOMP) | Authenticated | WebSocket endpoint |
| `WS` | `/app/chat.send` | Authenticated | Send chat message via STOMP |
| `WS` | `/topic/chat/{projectId}` | Authenticated | Subscribe to project chat |
| `GET` | `/calendar` | Authenticated | Monthly calendar view |
| `GET` | `/profile` | Authenticated | View/edit own profile |
| `POST` | `/profile/update` | Authenticated | Update profile |
| `POST` | `/profile/change-password` | Authenticated | Change password |
| `GET` | `/admin` | Admin | Admin dashboard |
| `GET` | `/admin/users` | Admin | User management list |
| `GET` | `/admin/users/new` | Admin | Create user form |
| `POST` | `/admin/users/new` | Admin | Create user |
| `GET` | `/admin/users/{id}` | Admin | User detail |
| `POST` | `/admin/users/{id}/role` | Admin | Change user role |
| `POST` | `/admin/users/{id}/toggle-status` | Admin | Enable / disable user |

---

## 🗂️ Project Structure

```
projectpulse/
├── src/
│   ├── main/
│   │   ├── java/com/projectpulse/
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java        # Seeds roles + demo users + demo project
│   │   │   │   ├── MvcConfig.java              # Serves ./uploads/ as /uploads/** URL
│   │   │   │   ├── SecurityConfig.java         # Filter chain, role URL rules, login/logout
│   │   │   │   ├── UserDetailsServiceImpl.java # Loads User from DB for Spring Security
│   │   │   │   └── WebSocketConfig.java        # STOMP broker, /ws endpoint, /app prefix
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java        # /admin/** — user management
│   │   │   │   ├── AuthController.java         # /auth/login, /auth/register
│   │   │   │   ├── CalendarController.java     # /calendar — monthly deadline grid
│   │   │   │   ├── ChatController.java         # /chat/** + WebSocket @MessageMapping
│   │   │   │   ├── DashboardController.java    # / and /dashboard — role-aware stats
│   │   │   │   ├── ProfileController.java      # /profile — view, update, change password
│   │   │   │   ├── ProjectController.java      # /projects/** — full CRUD
│   │   │   │   ├── SubmissionController.java   # /submissions/** — upload, review, download
│   │   │   │   └── TaskController.java         # /tasks/** — full CRUD + pin + approve
│   │   │   ├── dto/
│   │   │   │   ├── ChatMessageDto.java         # WebSocket message payload
│   │   │   │   ├── ProjectDto.java             # Project create/edit form binding
│   │   │   │   ├── TaskDto.java                # Task create/edit form binding
│   │   │   │   └── UserRegistrationDto.java    # Registration form binding + validation
│   │   │   ├── entity/
│   │   │   │   ├── ChatMessage.java            # chat_messages table
│   │   │   │   ├── Project.java                # projects table + computed progress
│   │   │   │   ├── Role.java                   # roles table (ROLE_ADMIN/LEADER/MEMBER)
│   │   │   │   ├── Task.java                   # tasks table + isOverdue()
│   │   │   │   ├── TaskSubmission.java         # task_submissions table
│   │   │   │   └── User.java                   # users table + hasRole(), getPrimaryRole()
│   │   │   ├── exception/
│   │   │   │   ├── AccessDeniedException.java      # 403 — thrown when role check fails
│   │   │   │   ├── FileStorageException.java        # 500 — thrown on file I/O errors
│   │   │   │   ├── GlobalExceptionHandler.java      # @ControllerAdvice — maps exceptions to error.html
│   │   │   │   └── ResourceNotFoundException.java   # 404 — thrown when entity not found
│   │   │   ├── repository/
│   │   │   │   ├── ChatMessageRepository.java       # findByProjectOrderBySentAtAsc
│   │   │   │   ├── ProjectRepository.java           # JPQL with LEFT JOIN FETCH leader/members/createdBy
│   │   │   │   ├── RoleRepository.java              # findByName
│   │   │   │   ├── TaskRepository.java              # JPQL with LEFT JOIN FETCH project/assignedTo
│   │   │   │   ├── TaskSubmissionRepository.java    # Pending submissions per leader
│   │   │   │   └── UserRepository.java              # findByUsername, findByRoleName
│   │   │   ├── service/
│   │   │   │   ├── ChatService.java            # Send, persist, convert to DTO
│   │   │   │   ├── FileStorageService.java     # UUID store, load, delete, path traversal guard
│   │   │   │   ├── ProjectService.java         # CRUD, member assignment, access checks
│   │   │   │   ├── SubmissionService.java      # Submit file, approve/reject, review permission
│   │   │   │   ├── TaskService.java            # CRUD, pin toggle, approve/reject, calendar queries
│   │   │   │   └── UserService.java            # Register, update profile, change password, role update
│   │   │   ├── util/
│   │   │   │   └── SecurityUtils.java          # getCurrentUser() from SecurityContextHolder
│   │   │   └── ProjectPulseApplication.java    # @SpringBootApplication entry point
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── admin/
│   │       │   │   ├── dashboard.html          # System stats + user/project tables
│   │       │   │   ├── user-detail.html        # User info + change role + toggle status
│   │       │   │   ├── user-form.html          # Admin create user form
│   │       │   │   └── users.html              # Paginated user list with actions
│   │       │   ├── auth/
│   │       │   │   ├── login.html              # Login form with demo credentials hint
│   │       │   │   └── register.html           # Self-registration form (ROLE_MEMBER)
│   │       │   ├── calendar/
│   │       │   │   └── view.html               # 7-column monthly grid with task dots
│   │       │   ├── chat/
│   │       │   │   └── room.html               # WebSocket chat with data-* attribute pattern
│   │       │   ├── error/
│   │       │   │   └── error.html              # Branded 403/404/500 error page
│   │       │   ├── fragments/
│   │       │   │   ├── sidebar.html            # th:fragment="sidebar" — nav + user info
│   │       │   │   └── topbar.html             # th:fragment="topbar" + th:fragment="alerts"
│   │       │   ├── projects/
│   │       │   │   ├── detail.html             # Project overview, progress bar, task preview
│   │       │   │   ├── form.html               # Create/edit form with multi-select members
│   │       │   │   └── list.html               # Card grid of accessible projects
│   │       │   ├── shared/
│   │       │   │   └── profile.html            # Edit profile + change password tabs
│   │       │   ├── submissions/
│   │       │   │   ├── list.html               # Pending submissions table for leader
│   │       │   │   ├── review.html             # Side-by-side file info + approve/reject
│   │       │   │   ├── task-submissions.html   # All submissions history for one task
│   │       │   │   └── upload.html             # Drag-and-drop style file upload form
│   │       │   ├── tasks/
│   │       │   │   ├── detail.html             # Task info + status update + submission history
│   │       │   │   ├── form.html               # Create/edit with priority, assignee, pin
│   │       │   │   └── list.html               # Filtered task list with filter bar
│   │       │   └── dashboard.html              # Role-conditional dashboard (Admin/Leader/Member)
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── app.css                 # Full custom design system (CSS variables, components)
│   │       │   └── js/
│   │       │       ├── app.js                  # Alerts, confirm dialogs, sidebar toggle, file preview
│   │       │       └── chat.js                 # SockJS connect, STOMP subscribe, DOM message append
│   │       └── application.properties
│   └── test/
│       └── java/com/projectpulse/
│           └── ProjectPulseApplicationTests.java
├── uploads/                                    # Runtime file storage (git-ignored)
├── .gitignore
├── pom.xml
└── README.md
```

---

## 📌 Roadmap

- [ ] **REST API Layer** — Expose `@RestController` endpoints returning JSON alongside the existing MVC controllers, enabling mobile or React frontends to consume the same backend
- [ ] **JWT Authentication** — Replace session-based auth with stateless JWT tokens to support the REST API and mobile clients
- [ ] **Email Notifications** — Send email alerts via Spring Mail when a task is assigned, a submission is reviewed, or a project deadline is approaching
- [ ] **Pagination & Search** — Add `Pageable` support to project and task listings, and a full-text search bar across tasks and projects

---

## 👤 Author

**Aditya Kamat**
- GitHub: [@adityakamat2005](https://github.com/adityakamat2005)
- LinkedIn: [linkedin.com/in/adityakamat2005](https://linkedin.com/in/adityakamat2005)
- Portfolio: [adityakamat2005.github.io](https://adityakamat2005.github.io)

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

```
MIT License

Copyright (c) 2025 Aditya Kamat

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```
```
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
