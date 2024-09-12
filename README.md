# To-Do List REST API

This project is a RESTful API for managing a To-Do List application, developed using **Spring Boot** with **H2** as the database. The application allows users to register, authenticate, and manage their tasks (including CRUD operations and file attachments)

## Features

- **User Registration:** Create new users with unique email addresses.
- **User Authorization:** Secure endpoints with JWT-based authentication.
- **Task Management:**
    - Create tasks with optional file attachments.
    - View, update, and delete tasks.
- **File Attachments:** Attach files to tasks (e.g., documents or images).
- **H2 Database:** An in-memory database is used for ease of setup and testing.

## Technologies

- **Spring Boot** (version 3.3.3)
- **Java** (version 21)
- **Spring Security** (JWT Authentication)
- **Spring Data JPA**
- **H2 Database** (for in-memory testing)
- **PostgreSQL** (production database)
- **Flyway** (Database migrations)
- **Swagger/OpenAPI** (API Documentation)
- **JUnit 5, TestContainers** (Testing)
- **Gradle** (Build and Dependency Management)

---