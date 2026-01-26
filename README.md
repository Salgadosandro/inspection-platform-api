# inspection-platform-api
Backend REST API built with **Java 21** and **Spring Boot** for managing **technical inspections**, **checklists** and **compliance workflows** (NR-12).

This project was designed as a **production-ready backend**, following clean architecture principles, strong validation layers and modern security practices.
---
## 🚀 Main Features

- RESTful API with Spring Boot
- Authentication with **JWT**
- Social login via **OAuth2 (Google & GitHub)**
- Role-Based Access Control (RBAC)
- Validation layer with custom validators
- Database migrations with **Flyway**
- OpenAPI / Swagger documentation
- Payment integration foundation (MercadoPago)
- Environment-based configuration (12-Factor App)
---
## 🛠️ Tech Stack

- **Java 21**
- **Spring Boot**
- Spring Security (JWT + OAuth2)
- Spring Data JPA (Hibernate)
- PostgreSQL
- Flyway
- OpenAPI / Swagger
- Maven
---

## 📂 Project Structure (simplified)

src/main/java/com/vectorlabs
├── config # Security, OAuth2, JWT and infrastructure configs
├── controller # REST controllers
├── dto # Request / Response DTOs
├── exception # Global exception handling
├── mapper # MapStruct mappers
├── model # JPA entities
├── repository # Spring Data repositories
├── service # Business logic
└── validator # Domain validation layer.
