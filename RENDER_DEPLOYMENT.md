# Render Deployment Guide for Shortify

This document details how to deploy **Shortify** on Render.com as a Docker Web Service.

---

## Step 1: Create a New Web Service on Render

1. Log in to your [Render Dashboard](https://dashboard.render.com).
2. Click **New +** -> **Web Service**.
3. Connect your Git repository containing the Shortify project.
4. Select **Docker** as the Environment.

---

## Step 2: Configure Environment Variables

Under the **Environment** settings tab in Render, configure the following environment variables:

| Key | Example Value | Description |
| :--- | :--- | :--- |
| `PORT` | `8080` | Application HTTP Port |
| `SPRING_DATA_MONGODB_URI` | `mongodb+srv://user:pass@cluster.mongodb.net/shortify` | Connection URI for MongoDB Atlas |
| `SPRING_DATA_REDIS_HOST` | `red-xxx.render.com` or Upstash Redis Host | Host address for Redis cache |
| `SPRING_DATA_REDIS_PORT` | `6379` | Redis Port |
| `SPRING_DATA_REDIS_PASSWORD` | `your_redis_password` | Redis password (if authentication enabled) |
| `APP_BASE_URL` | `https://shortify-app.onrender.com` | Production Base URL for short link generation |
| `SPRING_MAIL_HOST` | `smtp.gmail.com` | SMTP Server Host |
| `SPRING_MAIL_PORT` | `587` | SMTP Port |
| `SPRING_MAIL_USERNAME` | `your-email@gmail.com` | SMTP Username |
| `SPRING_MAIL_PASSWORD` | `app-specific-password` | SMTP App Password |

---

## Step 3: Health Check Path

Set the **Health Check Path** to:
```
/actuator/health
```

Render will poll this endpoint during deployment to verify that the JVM and MongoDB connection are healthy before switching live traffic.

---

## Step 4: Deploy

Click **Deploy Web Service**. Render will automatically build the Docker image, run maven package, start Java 21, and bind to `APP_BASE_URL`.
