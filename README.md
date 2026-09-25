# Fitness Tracker

A personal fitness tracking application built with Spring Boot. Log workouts, view your activity history, and store personalized recommendations.

## Tech stack

- Java 21
- Spring Boot 4
- Spring Data JPA
- Spring Security + JWT
- MySQL
- Maven

## Project structure

```
fitness-springboot-project/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/project/fitness/
│   │   │   ├── FitnessApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/          # Frontend UI
│   └── test/
│       └── java/com/project/fitness/
└── docker/
```

## Run locally

1. Create a MySQL database named `fitness_tracker`.
2. Copy `.env.example` and set your database credentials and JWT secret.
3. Start the app:

```bash
mvn spring-boot:run
```

4. Open the app at [http://localhost:8080](http://localhost:8080).
5. API docs are available at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

## Docker

```bash
mvn clean package -DskipTests
docker compose -f docker/docker-compose.yml up --build
```

## API overview

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Create an account |
| POST | `/api/auth/login` | Sign in and receive a JWT |
| POST | `/api/activities/user/{userId}` | Log an activity |
| GET | `/api/activities/user/{userId}` | List user activities |
| POST | `/api/recommendations/user/{userId}` | Add a recommendation |
| GET | `/api/recommendations/user/{userId}` | List recommendations |

Protected routes require the header: `Authorization: Bearer <token>`

## Environment variables

| Variable | Description |
|----------|-------------|
| `DB_URL` | JDBC URL (default: `jdbc:mysql://localhost:3306/fitness_tracker`) |
| `DB_USER` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Secret key (minimum 32 characters) |
