<div align="center">

  # 🔗 Shortify -  Distributed URL Shortener

  <p align="center">
    <strong>An enterprise-grade, distributed URL shortener built for extreme scale, resilience, and real-time analytics.</strong>
  </p>


  <p align="center">
    <a href="#-live-demo">Live Demo</a> •
    <a href="#-features">Features</a> •
    <a href="#-architecture">Architecture</a> •
    <a href="#-installation-guide">Installation</a> •
    <a href="#-api-routes">API Documentation</a>
  </p>

</div>

---

## 📖 Project Description

**Shortify** is a production-ready, distributed URL shortening service modeled after systems like Bitly, TinyURL, and Dub.co. Built using **Java 21** and **Spring Boot 3.5**, it features a **Base62 sequence encoder**, **sub-millisecond Redis caching**, and **MongoDB Atlas persistent storage**.

Shortify solves the challenge of long, complex link management while providing:
- High-throughput 302 redirects backed by in-memory caching.
- 100% database availability via automatic Redis graceful degradation.
- Enterprise security features including BCrypt passkey link protection, self-destructing single-use links, soft-delete recycle bins, and real-time click tracking.
- A modern, responsive Light SaaS UI styled after Stripe, Linear, and Dub.co.

---

## 🚀 Live Demo

Shortify is configured for seamless containerized deployment on Render.

- **Production Live URL**: [https://distributed-url-shortener-0vg2.onrender.com]
- 

---

## ✨ Features

### 🔐 Authentication & Account Management
- **Spring Security 6 Integration**: Form-based authentication with session management and BCrypt password hashing.
- **Remember-Me Persistent Login**: Secure 30-day token persistence.
- **Forgot Password Flow**: Secure time-limited email reset tokens.
- **Email Verification**: User account verification flow supporting custom and college domain emails.

### 🔗 URL Shortening & Link Control
- **Base62 Short Code Encoding**: Collision-free, auto-incrementing Base62 sequence generator.
- **Custom Alias System**: User-defined human-readable aliases with strict validation (`a-z`, `A-Z`, `0-9`, `-`, `_`, 3–30 characters).
- **Protocol Auto-Completion**: Normalizes entries like `google.com` to `https://google.com` while blocking dangerous pseudo-protocols (`javascript:`, `data:`).
- **Passkey Protection**: Require a password before unlocking confidential target URLs.
- **One-Time Self-Destruct Links**: Links automatically deactivate after a single click.
- **Custom Expiration**: Flexible expiration periods (7 days, 30 days, 1 year, 10 years).

### ⚡ Resilience & Performance
- **Sub-Millisecond Redis Caching**: Cache hits bypass database lookups entirely for ultra-fast redirection.
- **Graceful Fallback**: `RedisFallbackHelper` catches Redis outages and falls back to MongoDB Atlas without throwing 500 errors.
- **Atomic Click Tracking**: Increments click counts only upon confirmed, valid 302 redirects.

### 📊 Analytics & Management
- **Real-Time Click Metrics**: Monitor top-performing links and traffic counts.
- **CSV Data Export**: Direct streaming export of full user link portfolios to CSV.
- **Instant QR Code Stream**: On-the-fly PNG QR code image generation via ZXing.
- **Soft Delete & Recycle Bin**: Safely move links to a trash bin for restoration or permanent purge.
- **Admin Panel**: Role-based access control (`ROLE_ADMIN`) for system-wide user and link management.

---

## 🖼️ Screenshots


---

## 🛠️ Tech Stack

### Backend
| Technology | Version | Usage |
| :--- | :--- | :--- |
| **Java** | 21 LTS | Core programming language |
| **Spring Boot** | 3.5.16 | Primary backend application framework |
| **Spring Security** | 6.x | Authentication, authorization, and CSRF protection |
| **Spring Data MongoDB** | 3.x | MongoDB object mapping & database operations |
| **Spring Data Redis** | 3.x | Lettuce Redis cache connection |
| **Spring Mail** | 3.x | Email verification & password reset delivery |
| **Spring Actuator** | 3.x | System health check (`/actuator/health`) |
| **ZXing** | 3.5.3 | PNG QR code matrix image generation |

### Frontend
| Technology | Version | Usage |
| :--- | :--- | :--- |
| **Thymeleaf** | 3.x | Server-side HTML template rendering |
| **Bootstrap** | 5.3.0 | Responsive layout framework |
| **Font Awesome** | 6.4.0 | Vector UI icons |
| **Chart.js** | 4.x | Traffic visualization and bar graphs |
| **Google Fonts** | Inter | Modern light SaaS typography |

### Database, Cache & Tools
| Technology | Usage |
| :--- | :--- |
| **MongoDB Atlas** | Primary NoSQL document storage (`users`, `urls`, `counters`) |
| **Redis** | In-memory key-value cache layer (`url:{identifier}`) |
| **Apache Maven** | Project build and dependency management |
| **Docker** | Multi-stage Alpine container packaging |
| **Render** | Cloud hosting platform |

---

## 🏗️ Architecture

```mermaid
flowchart TD
    Client([Client Browser / REST API]) -->|HTTP Request| Security[Spring Security & CSRF Filter]
    Security -->|Authorized| Router{Controller Router}
    
    Router -->|GET /{shortCode}| RedirectCtrl[RedirectController]
    Router -->|POST /urls/create| UrlCtrl[UrlController]
    Router -->|POST /api/shorten| ApiCtrl[PublicApiController]
    
    RedirectCtrl --> UrlService[UrlServiceImpl]
    UrlCtrl --> UrlService
    ApiCtrl --> UrlService

    UrlService -->|1. Check Cache| RedisHelper[RedisFallbackHelper]
    RedisHelper -->|Cache Hit| Redis[(Redis Cache)]
    RedisHelper -->|Cache Miss / Outage| Mongo[(MongoDB Atlas)]

    UrlService -->|2. Encode Sequence| Base62[Base62Encoder]
    UrlService -->|3. QR Code| QRService[QRCodeServiceImpl]
```

---

## 📁 Directory Tree

```
distributed-url-shortener/
├── Dockerfile
├── DOCKER_DEPLOYMENT.md
├── RENDER_DEPLOYMENT.md
├── PRODUCTION_CHECKLIST.md
├── TESTING_CHECKLIST.md
├── README.md
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/uday/urlshortener/
    │   │   ├── DistributedUrlShortenerApplication.java
    │   │   ├── config/
    │   │   │   ├── RedisConfig.java
    │   │   │   └── SecurityConfig.java
    │   │   ├── controller/
    │   │   │   ├── AdminController.java
    │   │   │   ├── AnalyticsController.java
    │   │   │   ├── AuthController.java
    │   │   │   ├── DashboardController.java
    │   │   │   ├── PasswordResetController.java
    │   │   │   ├── ProfileController.java
    │   │   │   ├── PublicApiController.java
    │   │   │   ├── RedirectController.java
    │   │   │   └── UrlController.java
    │   │   ├── dto/
    │   │   │   ├── DashboardStatsDto.java
    │   │   │   ├── UrlAnalyticsDto.java
    │   │   │   ├── UrlRequestDto.java
    │   │   │   └── UrlResponseDto.java
    │   │   ├── exception/
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   ├── model/
    │   │   │   ├── DatabaseSequence.java
    │   │   │   ├── Url.java
    │   │   │   └── User.java
    │   │   ├── repository/
    │   │   │   ├── CounterRepository.java
    │   │   │   ├── UrlRepository.java
    │   │   │   └── UserRepository.java
    │   │   ├── security/
    │   │   │   ├── CustomUserDetails.java
    │   │   │   └── CustomUserDetailsService.java
    │   │   ├── service/
    │   │   │   ├── EmailService.java
    │   │   │   ├── QRCodeService.java
    │   │   │   ├── SequenceGeneratorService.java
    │   │   │   ├── UrlService.java
    │   │   │   └── UserService.java
    │   │   └── util/
    │   │       ├── Base62Encoder.java
    │   │       ├── RedisFallbackHelper.java
    │   │       └── UrlSanitizerUtil.java
    │   └── resources/
    │       ├── application.properties
    │       ├── static/
    │       │   ├── css/
    │       │   │   ├── style.css
    │       │   │   ├── theme.css
    │       │   │   └── variables.css
    │       │   └── js/
    │       │       └── main.js
    │       └── templates/
    │           ├── index.html
    │           ├── admin/
    │           ├── analytics/
    │           ├── auth/
    │           ├── dashboard/
    │           ├── error/
    │           ├── layout/
    │           ├── profile/
    │           └── url/
    └── test/
        └── java/com/uday/urlshortener/
            ├── DistributedUrlShortenerApplicationTests.java
            ├── controller/
            ├── service/
            └── util/
```

---

## ⚙️ Installation Guide

### Prerequisites
- **Java 21 JDK** installed
- **Apache Maven 3.9+** installed
- **MongoDB** running locally or a **MongoDB Atlas URI**
- *(Optional)* **Redis** running on `localhost:6379`

### 1. Clone Repository
```bash
git clone https://github.com/Udaylandge/distributed-url-shortener.git
cd distributed-url-shortener
```

### 2. Environment Setup
Configure your environment variables or export them in your terminal:
```bash
export SPRING_DATA_MONGODB_URI="mongodb+srv://<username>:<password>@cluster.mongodb.net/shortify"
export APP_BASE_URL="http://localhost:8080"
export SPRING_DATA_REDIS_HOST="localhost"
export SPRING_DATA_REDIS_PORT="6379"
```

### 3. Build & Test
```bash
./mvnw clean test
```

### 4. Run Application
```bash
./mvnw spring-boot:run
```
Access the application at: `http://localhost:8080`

---

## 🐳 Docker Deployment

Shortify uses a multi-stage Docker build with an Alpine JRE 21 base image and non-root security privileges (`appuser`).

### Build Docker Image
```bash
docker build -t shortify:latest .
```

### Run Container Standalone
```bash
docker run -d \
  --name shortify-app \
  -p 8080:8080 \
  -e SPRING_DATA_MONGODB_URI="mongodb+srv://user:pass@cluster.mongodb.net/shortify" \
  -e APP_BASE_URL="http://localhost:8080" \
  shortify:latest
```

---

## 🔑 Environment Variables

| Variable Name | Required | Default Value | Description |
| :--- | :--- | :--- | :--- |
| `APP_BASE_URL` | Yes | `http://localhost:8080` | Public production domain used for generating short links |
| `SPRING_DATA_MONGODB_URI` | Yes | `mongodb://localhost:27017/shortify` | MongoDB Atlas / Local connection URI |
| `SPRING_DATA_REDIS_HOST` | No | `localhost` | Redis server hostname |
| `SPRING_DATA_REDIS_PORT` | No | `6379` | Redis server port |
| `SPRING_DATA_REDIS_PASSWORD` | No | `""` | Redis authentication password |
| `SPRING_MAIL_HOST` | No | `smtp.gmail.com` | SMTP host for password resets |
| `SPRING_MAIL_PORT` | No | `587` | SMTP port |
| `SPRING_MAIL_USERNAME` | No | `""` | SMTP authentication email |
| `SPRING_MAIL_PASSWORD` | No | `""` | SMTP app password |

---

## 🌐 API Routes

### 🔓 Public & Redirect Endpoints
| HTTP Method | Route | Description |
| :--- | :--- | :--- |
| `GET` | `/` | Shortify landing page & instant shortener |
| `GET` | `/{shortCode}` | Performs 302 redirect or renders status page |
| `POST` | `/url/pass/{shortCode}` | Verifies passkey for password-protected links |
| `POST` | `/api/shorten` | REST API endpoint to shorten URLs (JSON) |
| `GET` | `/actuator/health` | Health check endpoint for Docker & Render |

### 🔒 User Account Endpoints
| HTTP Method | Route | Description |
| :--- | :--- | :--- |
| `GET` | `/dashboard` | User metrics and recent links dashboard |
| `GET` / `POST` | `/urls/create` | Shorten URL creation page and submit |
| `GET` | `/urls/manage` | Paginated "My Short Links" portfolio & search |
| `POST` | `/urls/toggle-active/{id}` | Enable or disable short link redirection |
| `POST` | `/urls/delete/{id}` | Move short link to Recycle Bin |
| `GET` | `/urls/trash` | Recycle Bin page |
| `POST` | `/urls/restore/{id}` | Restore link from Recycle Bin |
| `GET` | `/urls/qr/{id}` | Stream PNG QR Code image |
| `GET` | `/urls/export/csv` | Download CSV metrics export |
| `GET` | `/analytics` | Traffic charts and click analytics |

---

## 🔒 Security Architecture

- **BCrypt Encryption**: Passwords and link passkeys are securely salted and hashed using BCrypt.
- **CSRF Protection**: All HTML forms utilize Thymeleaf CSRF tokens (`_csrf`).
- **Remember-Me Security**: Persistent login tokens stored with 30-day expiration.
- **Input Sanitization**: `UrlSanitizerUtil` validates URL scheme protocols (`http://`, `https://`, `ftp://`) and blocks malicious XSS schemes (`javascript:`, `data:`).
- **Custom Alias Enforcement**: Strict regex pattern `^[a-zA-Z0-9_-]{3,30}$` rejects spaces, dots, and emails.
- **Redis Graceful Fallback**: `RedisFallbackHelper` catches Redis exceptions, ensuring database operations continue seamlessly if Redis goes down.

---

## 🧪 Testing

Shortify includes automated test coverage with **JUnit 5**, **Mockito**, and **Spring MockMvc**.

```bash
./mvnw clean test
```

- **Total Test Suite**: 42 Tests
- **Test Result**: `BUILD SUCCESS` (0 Failures, 0 Errors)
- **Coverage**: Base62 sequence encoding, UrlService business logic, UserService authentication, RedirectController status routing, and UrlController MVC views.

---

## 📈 Future Enhancements

- [ ] **Geo-Location Analytics**: Track redirect clicks by country and region using MaxMind GeoIP2.
- [ ] **Custom Domain Support (CNAME)**: Allow enterprise teams to connect custom branding domains.
- [ ] **Team Workspaces**: Shared workspaces and role-based access for organization teams.
- [ ] **REST API Key Management**: Rate-limited developer API key generation.

---

## 📜 License

Distributed under the **MIT License**. See `LICENSE` for more information.

---

## 👨‍💻 Author

- **Author**: Uday Landge
- **Course**: MCA (Master of Computer Applications)
- **Project**: Shortify – Distributed URL Shortener
- **GitHub**: [@Udaylandge](https://github.com/Udaylandge)
- **Repository**: [https://github.com/Udaylandge/distributed-url-shortener](https://github.com/Udaylandge/distributed-url-shortener)

---

<div align="center">
  <p>⭐ <strong>Star this repository if you find it helpful!</strong> ⭐</p>
</div>
