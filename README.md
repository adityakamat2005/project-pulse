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
- **Responsive Custom UI** — CSS design system built from scratch with CSS variables; Bootstrap Icons for iconography

---

## 🏗️ Architecture

```text
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
│  │  User · Project · Task · Submission · FileStorage · Chat │   │
│  └──────────────────────┬──────────────────────────────────┘   │
│                         │                                       │
│  ┌──────────────────────▼──────────────────────────────────┐   │
│  │               Repositories (6) — JPA                    │   │
│  │  Custom JPQL with LEFT JOIN FETCH for eager loading      │   │
│  └──────────────────────┬──────────────────────────────────┘   │
│                         │                                       │
│  ┌──────────────────────▼──────────────────────────────────┐   │
│  │            WebSocket Message Broker (STOMP)              │   │
│  │  /app/chat.send → SimpleBroker → /topic/chat/{id}       │   │
│  └─────────────────────────────────────────────────────────┘   │
└───────────────────────────────┬─────────────────────────────────┘
                                │  JDBC (HikariCP)
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     PostgreSQL Database                         │
│                                                                 │
│  users ── user_roles ── roles                                   │
│  projects ── project_members ── users                           │
│  tasks ── task_submissions ── users                             │
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
4. Controller calls the `@Service` layer for business logic and access checks
5. Service calls the `@Repository` which executes JPQL with `LEFT JOIN FETCH` to load associations eagerly
6. Hibernate translates JPQL to SQL and executes against PostgreSQL via HikariCP connection pool
7. Result entities are returned up the chain; Controller adds them to the Spring `Model`
8. Thymeleaf engine renders the HTML, resolving `${...}` expressions and `sec:authorize` attributes
9. For chat: the STOMP broker receives the message at `/app/chat.send`, persists it, and broadcasts to `/topic/chat/{projectId}`

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
| Connection Pool | HikariCP 5.0.1 |
| Real-Time | Spring WebSocket + STOMP + SockJS |
| File Handling | `MultipartFile` → local disk (`./uploads/`) with UUID filenames |
| Validation | Jakarta Bean Validation (`@NotBlank`, `@Email`, `@Size`, `@Future`) |
| Build Tool | Apache Maven 3.8+ |
| Dev Tooling | Spring Boot DevTools |
| Boilerplate | Lombok 1.18.32 |
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

### 1. Clone the repository

```bash
git clone https://github.com/adityakamat2005/projectpulse.git
cd projectpulse
```

### 2. Create the PostgreSQL database

```sql
CREATE DATABASE projectpulse_db;
```

### 3. Configure application.properties

Open `src/main/resources/application.properties` and set your credentials:

```properties
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/projectpulse_db
spring.datasource.username=postgres
spring.datasource.password=your_postgres_password_here
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=true

app.upload.dir=./uploads
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=15MB

server.servlet.session.timeout=60m
```

> All 8 tables are created automatically by Hibernate on first startup.

### 4. Build

```bash
mvn clean install -DskipTests
```

### 5. Run

```bash
mvn spring-boot:run
```

### 6. Open in browser

Open `http://localhost:8080` in your browser.

**Demo credentials (auto-seeded on first run):**

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin@123` | Administrator |
| `leader1` | `Leader@123` | Project Leader |
| `member1` | `Member@123` | Team Member |
| `member2` | `Member@123` | Team Member |

---

## 📡 URL Routes

| Method | Endpoint | Role Required | Description |
|---|---|---|---|
| `GET` | `/auth/login` | Public | Login page |
| `POST` | `/auth/login` | Public | Process login |
| `GET` | `/auth/register` | Public | Registration page |
| `POST` | `/auth/register` | Public | Process registration |
| `GET` | `/auth/logout` | Authenticated | Logout |
| `GET` | `/dashboard` | Authenticated | Role-aware dashboard |
| `GET` | `/projects` | Authenticated | List projects |
| `GET` | `/projects/{id}` | Authenticated | Project detail |
| `GET` | `/projects/new` | Admin / Leader | New project form |
| `POST` | `/projects/new` | Admin / Leader | Create project |
| `GET` | `/projects/{id}/edit` | Admin / Leader | Edit project form |
| `POST` | `/projects/{id}/edit` | Admin / Leader | Update project |
| `POST` | `/projects/{id}/delete` | Admin | Delete project |
| `POST` | `/projects/{id}/status` | Admin / Leader | Update project status |
| `GET` | `/tasks` | Authenticated | Task list (`?projectId=`) |
| `GET` | `/tasks/{id}` | Authenticated | Task detail |
| `GET` | `/tasks/new` | Admin / Leader | New task form |
| `POST` | `/tasks/new` | Admin / Leader | Create task |
| `GET` | `/tasks/{id}/edit` | Admin / Leader | Edit task form |
| `POST` | `/tasks/{id}/edit` | Admin / Leader | Update task |
| `POST` | `/tasks/{id}/status` | Authenticated | Update task status |
| `POST` | `/tasks/{id}/pin` | Admin / Leader | Toggle pin |
| `POST` | `/tasks/{id}/delete` | Admin / Leader | Delete task |
| `POST` | `/tasks/{id}/approve` | Admin / Leader | Approve task |
| `POST` | `/tasks/{id}/reject` | Admin / Leader | Reject task |
| `GET` | `/submissions` | Admin / Leader | Pending submissions |
| `GET` | `/submissions/task/{taskId}` | Authenticated | Submissions for a task |
| `GET` | `/submissions/upload/{taskId}` | Member | Upload form |
| `POST` | `/submissions/upload/{taskId}` | Member | Submit file |
| `GET` | `/submissions/review/{id}` | Admin / Leader | Review page |
| `POST` | `/submissions/review/{id}/approve` | Admin / Leader | Approve submission |
| `POST` | `/submissions/review/{id}/reject` | Admin / Leader | Reject submission |
| `GET` | `/submissions/download/{id}` | Authenticated | Download file |
| `GET` | `/chat/{projectId}` | Authenticated | Chat room |
| `POST` | `/chat/{projectId}/send` | Authenticated | HTTP fallback send |
| `WS` | `/ws` | Authenticated | WebSocket endpoint |
| `WS` | `/app/chat.send` | Authenticated | Send via STOMP |
| `WS` | `/topic/chat/{projectId}` | Authenticated | Subscribe to chat |
| `GET` | `/calendar` | Authenticated | Monthly calendar |
| `GET` | `/profile` | Authenticated | View profile |
| `POST` | `/profile/update` | Authenticated | Update profile |
| `POST` | `/profile/change-password` | Authenticated | Change password |
| `GET` | `/admin` | Admin | Admin dashboard |
| `GET` | `/admin/users` | Admin | User list |
| `GET` | `/admin/users/new` | Admin | Create user form |
| `POST` | `/admin/users/new` | Admin | Create user |
| `GET` | `/admin/users/{id}` | Admin | User detail |
| `POST` | `/admin/users/{id}/role` | Admin | Change role |
| `POST` | `/admin/users/{id}/toggle-status` | Admin | Enable / disable user |

---

## 🗂️ Project Structure

```text
projectpulse/
├── pom.xml
├── .gitignore
├── README.md
├── uploads/
└── src/
    └── main/
        ├── java/com/projectpulse/
        │   ├── ProjectPulseApplication.java
        │   ├── config/
        │   │   ├── DataInitializer.java
        │   │   ├── MvcConfig.java
        │   │   ├── SecurityConfig.java
        │   │   ├── UserDetailsServiceImpl.java
        │   │   └── WebSocketConfig.java
        │   ├── controller/
        │   │   ├── AdminController.java
        │   │   ├── AuthController.java
        │   │   ├── CalendarController.java
        │   │   ├── ChatController.java
        │   │   ├── DashboardController.java
        │   │   ├── ProfileController.java
        │   │   ├── ProjectController.java
        │   │   ├── SubmissionController.java
        │   │   └── TaskController.java
        │   ├── dto/
        │   │   ├── ChatMessageDto.java
        │   │   ├── ProjectDto.java
        │   │   ├── TaskDto.java
        │   │   └── UserRegistrationDto.java
        │   ├── entity/
        │   │   ├── ChatMessage.java
        │   │   ├── Project.java
        │   │   ├── Role.java
        │   │   ├── Task.java
        │   │   ├── TaskSubmission.java
        │   │   └── User.java
        │   ├── exception/
        │   │   ├── AccessDeniedException.java
        │   │   ├── FileStorageException.java
        │   │   ├── GlobalExceptionHandler.java
        │   │   └── ResourceNotFoundException.java
        │   ├── repository/
        │   │   ├── ChatMessageRepository.java
        │   │   ├── ProjectRepository.java
        │   │   ├── RoleRepository.java
        │   │   ├── TaskRepository.java
        │   │   ├── TaskSubmissionRepository.java
        │   │   └── UserRepository.java
        │   ├── service/
        │   │   ├── ChatService.java
        │   │   ├── FileStorageService.java
        │   │   ├── ProjectService.java
        │   │   ├── SubmissionService.java
        │   │   ├── TaskService.java
        │   │   └── UserService.java
        │   └── util/
        │       └── SecurityUtils.java
        └── resources/
            ├── application.properties
            ├── static/
            │   ├── css/
            │   │   └── app.css
            │   └── js/
            │       ├── app.js
            │       └── chat.js
            └── templates/
                ├── dashboard.html
                ├── admin/
                │   ├── dashboard.html
                │   ├── user-detail.html
                │   ├── user-form.html
                │   └── users.html
                ├── auth/
                │   ├── login.html
                │   └── register.html
                ├── calendar/
                │   └── view.html
                ├── chat/
                │   └── room.html
                ├── error/
                │   └── error.html
                ├── fragments/
                │   ├── sidebar.html
                │   └── topbar.html
                ├── projects/
                │   ├── detail.html
                │   ├── form.html
                │   └── list.html
                ├── shared/
                │   └── profile.html
                ├── submissions/
                │   ├── list.html
                │   ├── review.html
                │   ├── task-submissions.html
                │   └── upload.html
                └── tasks/
                    ├── detail.html
                    ├── form.html
                    └── list.html
```

---

## 📌 Roadmap

- [ ] **REST API Layer** — Expose `@RestController` endpoints returning JSON alongside existing MVC controllers, enabling mobile or React frontends
- [ ] **JWT Authentication** — Replace session-based auth with stateless JWT tokens to support the REST API
- [ ] **Email Notifications** — Send alerts via Spring Mail when a task is assigned, a submission is reviewed, or a deadline is approaching
- [ ] **Pagination & Search** — Add `Pageable` support to project and task listings with full-text search

---

## 👤 Author

**Aditya Kamat**
- GitHub: [@adityakamat2005](https://github.com/adityakamat2005)
- LinkedIn: [linkedin.com/in/adityakamat2005](https://linkedin.com/in/adityakamat2005)
- Portfolio: [adityakamat2005.github.io](https://adityakamat2005.github.io)

---

## 📄 License

This project is licensed under the MIT License.

Copyright (c) 2025 Aditya Kamat

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
