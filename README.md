# FitG

Personal fitness tracker: calorie logging and activity tracking, with Apple Health and Strava integration.

- `backend/`: Spring Boot 4.2 · Java 21 · Gradle · PostgreSQL · Flyway
- `mobile/`: Expo SDK 58 · React Native · JavaScript (JSX)
- `docs/ARCHITECTURE.md`: design, data model, API, flows
- `docs/TASKS.md`: the implementation backlog (work through it in order)

## Prerequisites
JDK 21+, Node 20+, Docker, Xcode (for the iOS simulator and HealthKit).

## Run

```bash
# backend: also starts Postgres via backend/compose.yaml
cd backend && ./gradlew bootRun

# mobile
cd mobile && npx expo start      # press i for the iOS simulator
```

Copy `.env.example` to `.env` and fill it in once you reach the Strava tasks.
