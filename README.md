# 🏋️‍♂️ Fitness Tracker Web Platform

<div align="center">

[![Live Demo](https://img.shields.io/badge/🌐_Live_Demo-Visit_Website-10B981?style=for-the-badge&logo=googlechrome&logoColor=white)](https://fitness-tracker-production-f4e4.up.railway.app)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

<br />

**A modern, full-stack fitness and wellness tracking application built with Spring Boot, Spring Security, JWT, MySQL, and a responsive glassmorphism UI.**

[Explore Live Demo 🚀](https://fitness-tracker-production-f4e4.up.railway.app) · [Report Bug 🐛](https://github.com/HarshitYash/Fitness-Tracker/issues) · [API Documentation 📚](#-api-documentation)

</div>

---

## 🌟 Overview

**Fitness Tracker** is an end-to-end workout logging and health insights platform. It empowers users to monitor their physical training, calculate real-time calorie expenditure with scientific MET formulas, receive automated workout recommendations, and secure their accounts with email OTPs and social OAuth2 logins.

---

## ✨ Key Features

- 🌓 **Modern Dual-Theme UI**: Clean, responsive, glassmorphic design with instant Light/Dark mode switching.
- 🔐 **Multi-Provider Authentication**:
  - Stateless JWT token-based authentication.
  - Social OAuth2 login with **Google** and **GitHub**.
  - 6-digit Email OTP verification for registrations and password resets via Gmail SMTP.
- ⚡ **Interactive Workout Estimator**:
  - Real-time calorie and MET calculation based on activity type, body weight, and duration before logging.
- 📊 **Workout & Activity Management**:
  - Comprehensive logging for Running, Cycling, Walking, Weight Training, and Cross Trainer.
  - Summary analytics displaying total workouts, hours logged, and calories burned.
- 💡 **Personalized Health Recommendations**:
  - Smart recommendations and improvement suggestions tailored to logged fitness routines.
- 🐳 **Production-Ready & Containerized**:
  - Multi-stage Docker build ready for instant 1-click cloud deployment on Railway, Render, or AWS.
- 📖 **Interactive Swagger / OpenAPI Docs**:
  - Full API exploration and test runner via Swagger UI.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 21, Spring Boot 3, Spring Data JPA, Spring Security, Hibernate |
| **Authentication** | JWT (JSON Web Tokens), OAuth2 Client, BCrypt, JavaMail Sender |
| **Database** | MySQL 8.4 (Production), H2 In-Memory (Automated Testing) |
| **Frontend** | Vanilla JavaScript (ES6+), Modern CSS3 (CSS Variables, Flexbox, Grid), HTML5 |
| **DevOps & Cloud** | Docker, Docker Compose, Multi-stage builds, Railway / Render deployment |
| **Testing** | JUnit 5, Mockito, Spring Boot Test (22 Automated Test Suites) |

---

## 📸 Architecture & Folder Structure

```text
Fitness-Tracker/
├── Dockerfile                   # Multi-stage container build for cloud deployment
├── docker/
│   └── docker-compose.yml       # Local orchestration (App + MySQL + Mailpit)
├── src/
│   ├── main/
│   │   ├── java/com/project/fitness/
│   │   │   ├── config/          # Security, OpenAPI, Web MVC, and OAuth client config
│   │   │   ├── controller/      # REST endpoints (Auth, Activities, Recommendations, Users)
│   │   │   ├── dto/             # Request & Response payload transfer objects
│   │   │   ├── entity/          # JPA entities (User, Activity, OtpCode, Recommendation)
│   │   │   ├── exception/       # Global exception handler & custom error responses
│   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   ├── security/        # JWT filter, token provider, OAuth2 success handler
│   │   │   └── service/         # Business domain logic & notification engines
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/          # Single-Page Frontend (HTML, CSS, JS, Assets)
│   │           ├── css/style.css
│   │           ├── js/app.js
│   │           └── img/logo.png
│   └── test/                    # Unit and integration test suites
├── pom.xml
└── README.md
```

---

## 🚀 Quickstart Guide

### Prerequisites
- [Java 21 JDK](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/)
- [MySQL 8.0+](https://dev.mysql.com/downloads/mysql/) or [Docker Desktop](https://www.docker.com/)

---

### Option 1: Run Locally with Maven

1. **Clone the repository:**
   ```bash
   git clone https://github.com/HarshitYash/Fitness-Tracker.git
   cd Fitness-Tracker
   ```

2. **Configure Environment Variables:**
   Copy `.env.example` to `.env` and fill in your database and email credentials:
   ```bash
   cp .env.example .env
   ```

3. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the App:**
   - Web App: `http://localhost:8080`
   - Swagger API Docs: `http://localhost:8080/swagger-ui.html`

---

### Option 2: Run with Docker Compose

Run the application, MySQL database, and local email tester (Mailpit) in one command:

```bash
docker compose -f docker/docker-compose.yml up --build
```

- **Frontend & API**: [http://localhost:8080](http://localhost:8080)
- **Mailpit Web Mailbox**: [http://localhost:8025](http://localhost:8025)

---

## 🌐 Cloud Deployment (Railway / Render)

This project includes a root **multi-stage `Dockerfile`** for zero-configuration cloud hosting.

1. **Database Setup**: Create a cloud MySQL instance on Railway, Aiven, or TiDB Cloud.
2. **Environment Variables**: Add the following keys in your hosting dashboard:
   - `DB_URL`: `jdbc:mysql://<host>:<port>/<db_name>?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false`
   - `DB_USER`: `<database_user>`
   - `DB_PASSWORD`: `<database_password>`
   - `JWT_SECRET`: `<32_character_secret_key>`
   - `MAIL_HOST`: `smtp.gmail.com`
   - `MAIL_PORT`: `587`
   - `MAIL_USERNAME`: `<your_gmail>`
   - `MAIL_PASSWORD`: `<your_gmail_app_password>`
   - `MAIL_AUTH`: `true`
   - `MAIL_STARTTLS`: `true`
   - `OTP_EXPOSE`: `false`
   - `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET`: *(Optional)*

---

## 📚 API Documentation

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user & trigger Email OTP | Public |
| `POST` | `/api/auth/verify-otp` | Verify 6-digit email OTP | Public |
| `POST` | `/api/auth/resend-otp` | Resend verification OTP code | Public |
| `POST` | `/api/auth/login` | Sign in with email & password (returns JWT) | Public |
| `POST` | `/api/auth/forgot-password` | Request password reset code | Public |
| `POST` | `/api/auth/reset-password` | Set new password with OTP | Public |
| `GET` | `/api/activities/user/{userId}` | Get all logged workouts for user | Authenticated |
| `POST` | `/api/activities/user/{userId}` | Log a new workout activity | Authenticated |
| `DELETE`| `/api/activities/{id}` | Delete a workout entry | Authenticated |
| `GET` | `/api/recommendations/user/{userId}`| Fetch health & workout recommendations | Authenticated |
| `POST` | `/api/recommendations/user/{userId}`| Save a custom recommendation | Authenticated |

---

## 🧪 Running Automated Tests

Run the full test suite (controllers, services, security filters, OTP validators):

```bash
mvn clean test
```

---

## 📄 License

This project is open-source and licensed under the [MIT License](LICENSE).
