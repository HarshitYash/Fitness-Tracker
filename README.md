# 🏋️‍♂️ Fitness Tracker Application - Backend API

A robust RESTful backend system for a Fitness Tracker Application built using **Spring Boot**, **Spring Security**, **Spring Data JPA**, and **MySQL**. The application handles user management, fitness activity logging, role-based access control, and secure token-based authentication using **JWT**.

---

## 🚀 Key Features

* **Authentication & Authorization:** Secure user registration and login using Spring Security and JWT (JSON Web Tokens).
* **Role-Based Access Control (RBAC):** Configured user roles (`ROLE_USER`, `ROLE_ADMIN`) for granular endpoint protection.
* **Activity & User Management:** Complete CRUD endpoints for logging workout routines, tracking user metrics, and progress history.
* **Clean Architecture:** Designed using the **DTO (Data Transfer Object) Pattern**, Bean Validation, and **Lombok** to ensure low coupling and clean code.
* **API Documentation:** Interactive documentation and testing available via **Swagger / OpenAPI**.
* **Containerization:** Packaged with **Docker** for seamless deployment and consistent execution across environments.

---

## 🛠️ Tech Stack & Tools

* **Backend Framework:** Spring Boot 3
* **Security:** Spring Security, JWT
* **Database & ORM:** MySQL, Spring Data JPA, Hibernate
* **Utilities:** Lombok, Jakarta Bean Validation
* **API Documentation:** Springdoc-OpenAPI / Swagger UI
* **Containerization:** Docker & Docker Compose
* **Build Tool:** Maven

---

## 📐 Architecture Overview

```text
Client (React / Postman) 
       │
       ▼ (HTTP Requests)
[ Spring Security Filter Chain ] ──(JWT Validation)──► [ Authentication Manager ]
       │
       ▼
[ Controller Layer ] ──(DTOs & Bean Validation)──► [ Service Layer ]
                                                         │
                                               (Entities & JPA Repositories)
                                                         │
                                                         ▼
                                                  [ MySQL Database ]
