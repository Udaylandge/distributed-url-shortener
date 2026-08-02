# Docker Deployment Guide for Shortify

This guide explains how to build and deploy **Shortify** using Docker and Docker Compose.

---

## 1. Multi-Stage Dockerfile Highlights

- **Base Image**: Alpine-based Eclipse Temurin Java 21 JRE for minimal attack surface and low memory footprint.
- **Security**: Runs under a dedicated non-root user (`appuser`).
- **JVM Optimization**: Configured with G1 Garbage Collector (`-XX:+UseG1GC`) and `-XX:MaxRAMPercentage=75.0`.
- **Health Check**: Automated health monitoring via `http://localhost:8080/actuator/health`.

---

## 2. Building the Docker Image

```bash
docker build -t shortify:latest .
```

---

## 3. Running Container Standalone

```bash
docker run -d \
  --name shortify-app \
  -p 8080:8080 \
  -e SPRING_DATA_MONGODB_URI="mongodb+srv://<user>:<password>@cluster.mongodb.net/shortify" \
  -e SPRING_DATA_REDIS_HOST="redis-host" \
  -e SPRING_DATA_REDIS_PORT="6379" \
  -e APP_BASE_URL="https://your-domain.com" \
  shortify:latest
```

---

## 4. Docker Compose Setup (App + Redis + Mongo)

Create a `docker-compose.yml` file:

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/shortify
      - SPRING_DATA_REDIS_HOST=redis
      - SPRING_DATA_REDIS_PORT=6379
      - APP_BASE_URL=http://localhost:8080
    depends_on:
      - mongo
      - redis

  mongo:
    image: mongo:latest
    ports:
      - "27017:27017"
    volumes:
      - mongo-data:/data/db

  redis:
    image: redis:alpine
    ports:
      - "6379:6379"

volumes:
  mongo-data:
```

Run compose:
```bash
docker-compose up -d
```
