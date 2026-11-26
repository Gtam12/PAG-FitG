# FitG — Implementation Tasks

**How we work**
1. One branch per task: `git checkout -b be-02-auth`.
2. Implement until the **Done when** checks pass.
3. Ask Claude: *"review BE-02"*. I'll review the diff against the acceptance criteria and the architecture.
4. Fix, merge to `main`, tick the box, move on.

Sizes: **S** ≈ ½ day, **M** ≈ 1 day, **L** ≈ 2 days. Total ≈ 3 weeks part-time.
Order matters: each phase builds on the previous one.

---

## Phase 0 — Setup

### [ ] SETUP-01 · Tooling & first run (S)
- Install Docker Desktop (or OrbStack) and Xcode with an iOS Simulator.
- `cd backend && ./gradlew bootRun`. Postgres should start through `compose.yaml`. **Expected:** startup fails at Flyway/JPA validation or comes up with no tables. That's fine, BE-01 fixes it.
- `cd mobile && npx expo start` → press `i` → the app shows in the simulator.
- `git init`, first commit of the scaffold.

**Done when:** both apps start; `curl localhost:8080/actuator/health` responds (401 is OK, since security is on by default).

---

## Phase 1 — Backend foundation

### [ ] BE-01 · Initial schema with Flyway (S)
- Create `db/migration/V1__users.sql` with the `users` table from ARCHITECTURE §4.
- `User` entity in `user/` (UUID id, `@Column` names match the SQL).
- Add `common/BaseEntity` with `createdAt`/`updatedAt` (`@CreationTimestamp`/`@UpdateTimestamp` or JPA auditing).

**Done when:** the app boots, `ddl-auto=validate` passes, and `BackendApplicationTests` is green (it uses Testcontainers).
**Architect notes:** never edit a migration that has run. Add `V2__...` instead. Use `timestamptz`, not `timestamp`.

### [ ] BE-02 · Auth: register / login / JWT (L)
- `config/SecurityConfig`: stateless, CSRF off, `permitAll` for `/api/auth/**`, `/actuator/health`, `/api/webhooks/**`, `/api/integrations/strava/callback`. Everything else authenticated. `.oauth2ResourceServer(o -> o.jwt(...))`.
- `JwtEncoder`/`JwtDecoder` beans (Nimbus, HS256, secret from `fitg.jwt.secret`). Bind config with a `@ConfigurationProperties` record.
- `AuthController`: `POST /api/auth/register` and `POST /api/auth/login` → `{ "token": "..." }`. Hash passwords with `BCryptPasswordEncoder`. JWT `sub` = user UUID.
- A helper to get the current user id from the `Jwt` principal (`@AuthenticationPrincipal Jwt jwt`).

**Done when:** register → login → `GET /api/me` with `Authorization: Bearer` returns the profile, and without the token returns 401. You have MockMvc tests for: register OK, duplicate email → 409, wrong password → 401.
**Architect notes:** don't write a custom JWT filter. The resource server already does it. Use the same error message for unknown email and wrong password.

### [ ] BE-03 · Profile + error handling (S)
- `GET /api/me`, `PATCH /api/me` (displayName, dailyCalorieGoal, timezone, validated with `ZoneId.of`).
- `common/GlobalExceptionHandler` (`@RestControllerAdvice`) → `ProblemDetail` for validation errors (400), not found (404), conflict (409).

**Done when:** an invalid body returns a 400 ProblemDetail listing the field errors.

---

## Phase 2 — Calorie logging (first usable feature)

### [ ] BE-04 · Food entries CRUD (M)
- `V2__food_entries.sql`, `FoodEntry` entity, repository, service, controller (ARCHITECTURE §5).
- `GET /api/food-entries?date=2026-11-26` uses the **user's timezone** to compute the `[start, end)` UTC range.
- Users can only see and modify their own entries: return 404, not 403, for other users' ids.

**Done when:** you have tests for create/list-by-date/update/delete, plus a timezone test: an entry at 23:30 local time is listed under the local date.

### [ ] MO-01 · App shell: routing, API client, auth screens (L)
- `npx expo install expo-router expo-secure-store @tanstack/react-query` and move to Expo Router (`src/app/`). Delete `App.jsx` and point `main` to `expo-router/entry`.
- Routes: `(auth)/login`, `(auth)/register`, `(tabs)/index` (Today), `(tabs)/food`, `(tabs)/activity`, `(tabs)/settings`.
- `src/api/client.js`: a fetch wrapper that injects the token, sets the base URL from `EXPO_PUBLIC_API_URL`, and logs out on 401.
- `src/features/auth/`: an auth context that keeps the token in SecureStore and redirects to login when no token is stored.

**Done when:** you can register in the app, kill it, reopen it, and still be logged in. Logout works.
**Architect notes:** the iOS simulator can reach `http://localhost:8080`. A physical phone needs your Mac's LAN IP.

### [ ] MO-02 · Food log screen (M)
- The Food tab lists today's entries grouped by meal, shows a total vs. goal, and has a date switcher.
- An "Add food" form with name, calories, meal type, and optional macros. Edit and delete (swipe or long-press).
- Use TanStack Query: `useQuery(['food', date])`, then invalidate after each mutation.

**Done when:** you can log your real meals for a day comfortably.

---

## Phase 3 — Activities & dashboard

### [ ] BE-05 · Activities + manual entry (M)
- `V3__activities.sql` (`activities` + `daily_metrics`, including the unique constraint), entities, `GET /api/activities?from&to`, `POST /api/activities` (source=MANUAL).

**Done when:** tests pass, and the unique constraint is in the migration.

### [ ] BE-06 · Dashboard endpoint (M)
- `GET /api/dashboard?date=` → caloriesIn, activeCaloriesOut, net, goal, steps, activities[].
- Implement the **cross-source dedupe** rule from ARCHITECTURE §7 here, in one well-tested method.

**Done when:** you have unit tests for the dedupe (same type, start within 2 min → one entry, Strava wins).

### [ ] MO-03 · Today + Activity screens (M)
- Today: a calories in/out ring or bar, steps, and a list of today's activities.
- Activity tab: a history list with a source badge (Manual/Apple/Strava) and an add-manual form.

---

## Phase 4 — Apple Health

### [ ] MO-04 · Dev build + HealthKit permissions (M)
- `npx expo install @kingstinct/react-native-healthkit expo-dev-client`, then add the config plugin in `app.json` with usage descriptions.
- `npx expo run:ios` (local build). From here on you use the dev build, not Expo Go.
- Settings screen has a "Connect Apple Health" button that requests read access to workouts, steps, and active energy.

**Done when:** the permission sheet appears, and after you grant access, `src/health/` can log the last 5 workouts.
**Architect notes:** iOS never tells you whether *read* access was denied. Design the UI to say "no data found", not "permission denied". The simulator has no workouts, so add sample data in the Health app or test on your phone.

### [ ] BE-07 · HealthKit sync endpoint (M)
- `POST /api/sync/healthkit` with `{ workouts: [...], deletedWorkoutIds: [...], dailyMetrics: [...] }`.
- Upsert by `(user, APPLE_HEALTH, external_id = HK UUID)`. Use `INSERT ... ON CONFLICT` in a native query, or find-then-save inside the transaction.
- Cap the batch size (for example 500) and validate the input.

**Done when:** posting the same payload twice produces no duplicates (test it).

### [ ] MO-05 · HealthKit sync engine (L)
- `src/health/sync.js`: an anchored workout query that sends new and deleted workouts, plus 7-day daily statistics for steps and active energy. Save the anchor **only after** a 2xx response.
- Trigger the sync on app start, on `AppState` → active, and from pull-to-refresh on Today.
- Map HK activity types to our `RUN|RIDE|WALK|SWIM|STRENGTH|OTHER`.

**Done when:** a new workout recorded on the Watch shows up in FitG after you reopen the app, and deleting it in Health removes it.

---

## Phase 5 — Strava

### [ ] BE-08 · Strava OAuth connect (L)
- Register an app at strava.com/settings/api. Set the callback domain to your `PUBLIC_BASE_URL` host.
- `V4__strava_connections.sql`, entity with an encrypted-token `AttributeConverter`.
- `GET /authorize-url` (signed `state`), `GET /callback` (verify state → exchange code via `RestClient` → save → redirect to `fitg://strava?status=ok|error`), `DELETE` (call Strava deauthorize, delete the row).
- `StravaTokenService.getValidAccessToken(userId)` refreshes when the token has under 5 minutes left.

**Done when:** you have a full connect from the app (MO-06) and the tokens are stored encrypted (check the DB).
**Architect notes:** never send tokens to the mobile app. Check the callback's `scope` param, because the user can untick `activity:read_all`.

### [ ] MO-06 · Connect Strava in the app (S)
- Settings has a "Connect Strava" button: fetch the authorize URL, call `WebBrowser.openAuthSessionAsync(url, 'fitg://strava')`, then refetch the connection status.
- Show the connected state with a Disconnect button.

### [ ] BE-09 · Strava import (backfill) (M)
- After connecting, run `@Async` import of `GET /athlete/activities?after=<30 days ago>` with paging. Map to `Activity(source=STRAVA)` and upsert.
- Map `sport_type` to our types. Use `calories` if present (needs the detail endpoint), otherwise `kilojoules` for rides.

**Done when:** your last 30 days of Strava activities appear in the Activity tab.

### [ ] BE-10 · Strava webhooks (M)
- `GET /api/webhooks/strava` echoes `hub.challenge` if `hub.verify_token` matches.
- `POST`: return 200 immediately, process `@Async` (create/update → fetch + upsert, delete → remove, deauthorize → delete the connection).
- Run `cloudflared tunnel --url http://localhost:8080` (or ngrok) and create the subscription with Strava's `push_subscriptions` API (document the curl in the README).

**Done when:** a new activity recorded in Strava appears in FitG within about a minute, without opening the app.

---

## Phase 6 — Hardening (pick what you need)

- [ ] **HARD-01** Integration tests for the Strava client with a mocked server (`MockRestServiceServer` or WireMock).
- [ ] **HARD-02** Dockerfile for the backend, deployed somewhere cheap (Fly.io / Railway / Render), with managed Postgres and real secrets.
- [ ] **HARD-03** EAS build to TestFlight so the app is on your phone without a cable.
- [ ] **HARD-04** Food search through the Open Food Facts API, plus a "recent foods" quick-add.
- [ ] **HARD-05** Weekly trends chart (calories in/out over 7 or 30 days).
