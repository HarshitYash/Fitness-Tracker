# 🏋️ Fitness Tracker Application

A full-stack fitness and activity tracking web application built with **Spring Boot 3**, **Spring Security 6**, **JWT**, and **MySQL**, featuring automated activity logging, customized health recommendations, OTP verification workflows, and containerized deployment via **Docker**.

---

## 📑 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture & Directory Structure](#-architecture--directory-structure)
- [Prerequisites](#-prerequisites)
- [Configuration & Environment Variables](#-configuration--environment-variables)
- [Getting Started](#-getting-started)
  - [Run with Maven (Local)](#run-with-maven-local)
  - [Run with Docker Compose](#run-with-docker-compose)
- [API Endpoints Reference](#-api-endpoints-reference)
- [Running Tests](#-running-tests)
- [License](#-license)

---

## ✨ Features

- **Robust Authentication & Security**
  - Stateless authentication using JSON Web Tokens (JWT).
  - OAuth2 social login support (Google / GitHub).
  - OTP verification system for user registration and password recovery.
  - Role-based access control with `ROLE_USER` and `ROLE_ADMIN` permissions.
- **Activity & Workout Management**
  - Log, read, update, and delete fitness sessions across multiple activity types.
  - Track session durations, calories burned, distances covered, and timestamps.
- **Personalized Recommendations**
  - Algorithmic feedback and workout/diet advice generated from historical activity records.
- **API Documentation & Testing**
  - Integrated Springdoc OpenAPI / Swagger UI for live interactive testing.
  - Full suite of unit and integration tests covering security, controllers, and services.
- **Containerization Ready**
  - Pre-configured `Dockerfile` and `docker-compose.yml` for unified app and database orchestration.

---

## 🛠️ Tech Stack

- **Backend:** Java 17+, Spring Boot 3, Spring Data JPA, Spring Security 6
- **Database:** MySQL 8.0+ (Production/Local), H2 In-Memory (Test suite)
- **Security:** JWT (jjwt), OAuth2 Client, BCrypt Password Encoding
- **Documentation:** Springdoc OpenAPI (Swagger UI)
- **Frontend:** Vanilla JavaScript (ES6+), HTML5, CSS3
- **DevOps & Build:** Maven, Docker, Docker Compose

---

## 📂 Architecture & Directory Structure

```text
Fitness-Tracker-main/
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
├── docs/
│   ├── GITHUB_COMMIT_PLAN.md
│   └── LEARNING_ORDER.md
├── src/
│   ├── main/
│   │   ├── java/com/project/fitness/
│   │   │   ├── config/          # Security, OpenAPI, Web MVC, and OAuth configs
│   │   │   ├── controller/      # REST API Controllers (Auth, Activity, User, Admin)
│   │   │   ├── dto/             # Request & Response payload DTOs
│   │   │   ├── entity/          # JPA Entities (User, Activity, OtpCode, Recommendation)
│   │   │   ├── exception/       # GlobalExceptionHandler and custom exceptions
│   │   │   ├── repository/      # Spring Data JPA Repositories
│   │   │   ├── security/        # JwtFilter, JwtService, OAuth2 Login Success Handlers
│   │   │   └── service/         # Business domain logic and interfaces
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/          # Embedded UI files (HTML, CSS, JS, static assets)
│   └── test/                    # Unit and integration test suites
├── .env.example
├── pom.xml
└── README.md
