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

This project is licensed under the [MIT License](LICENSE).
