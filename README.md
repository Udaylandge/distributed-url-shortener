# Shortify - Distributed URL Shortener

**Shortify** is a production-ready, high-performance distributed URL shortener built with Java 21 and Spring Boot 3.5. Designed similarly to Bitly and TinyURL, it provides sub-millisecond caching via Redis, MongoDB Atlas storage, custom aliases, QR code generation, password-protected links, one-time self-destructing links, soft-delete recovery, and analytics.

---

## Technical Architecture

```
[ Client Browser / REST API ]
             │
             ▼
   [ Spring Security & CSRF ]
             │
             ▼
   [ Controller Layer ] ──> (RedirectController, UrlController, AuthController, etc.)
             │
             ▼
   [ Service Layer ]
     ├── UrlServiceImpl ──> [ Redis Cache (Lettuce) ]
     │                └──> [ MongoDB Atlas (urls collection) ]
     ├── UserServiceImpl ──> [ MongoDB Atlas (users collection) ]
     └── EmailServiceImpl ──> [ SMTP Mail Server ]
```

---

## Key Features

- **Protocol Auto-Completion**: Shortens links entered without protocol (e.g. `google.com` -> `https://google.com`). Supports `http://`, `https://`, and `ftp://`.
- **Fault-Tolerant Redis Caching**: Redis cache hits bypass database reads. If Redis is unavailable, operations automatically fallback to MongoDB without application crashes or exceptions.
- **Custom Aliases**: Allows users to specify human-readable short links (e.g., `shortify.app/my-campaign`).
- **Passkey Protection**: Require a password before granting access to confidential short links.
- **One-Time Links**: Self-destructing links that automatically deactivate after a single click.
- **Soft Delete & Recycle Bin**: Deleted links are safely moved to a Trash Bin for easy restoration or permanent purging.
- **QR Code PNG Stream**: Generate and download print-ready QR codes for any shortened link.
- **CSV Analytics Export**: Stream complete link click data and metrics directly as a CSV download.
- **Global Exception Handler**: Custom `404`, `403`, `400`, and `500` error pages instead of technical stack traces.

---

## Tech Stack

- **Java**: 21 LTS
- **Framework**: Spring Boot 3.5.16
- **Security**: Spring Security 6 (Form login, Remember-Me, CSRF protection)
- **View Engine**: Thymeleaf with Thymeleaf Spring Security 6
- **Database**: MongoDB Atlas
- **Cache**: Redis (Spring Data Redis / Lettuce)
- **Build Tool**: Apache Maven
- **Containerization**: Docker (Multi-stage build)

---

## Getting Started Locally

### 1. Prerequisites
- Java 21 JDK
- Maven 3.9+
- Running MongoDB instance (or MongoDB Atlas URI)
- Optional: Running Redis instance on `localhost:6379`

### 2. Environment Configuration
Create an environment file or pass system properties:

```bash
export SPRING_DATA_MONGODB_URI="mongodb://localhost:27017/shortify"
export APP_BASE_URL="http://localhost:8080"
```

### 3. Run Application
```bash
./mvnw spring-boot:run
```

Access the application in your browser at: `http://localhost:8080`
