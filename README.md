# Pixels, Proses & Play

A small, opinionated blogging platform written in Spring Boot — rich-text posts, tags, comments, an admin surface, and a design system that leans editorial rather than cute.

Originally a Scaler Neovarsity capstone project. Rebuilt into a production-quality codebase across seven phases: a safety net of tests, a service layer, form DTOs with validation, portable schema migrations, defense-in-depth security, an i18n-ready UI, and a containerised deployment story.

![Home](https://github.com/user-attachments/assets/33fa7e8c-6d5a-43cd-91f6-ac7332437e64)

---

## Live demo

<!-- Deployed on Render, Postgres on Neon. The free dyno sleeps after 15 minutes
     of inactivity; the first request after a nap takes about a minute. -->

Coming soon — the production URL will land here once the first deploy is green.

---

## Features

- Rich-text post editor (Toast UI Editor) with XSS-safe HTML sanitisation on write
- Tagging, search-as-you-type, featured-post carousel on the home page
- User registration and login (Spring Security form login + BCrypt)
- Profile pages with avatar uploads
- Comment threads on each post
- Admin dashboard for user and post management
- Light / dark themes with a per-viewer preference that survives reloads
- Thymeleaf server-rendered — no JavaScript framework, no SPA tax
- Flash messages in a shared `MessageSource` bundle, ready for localisation

## Architecture notes

A few things that aren't obvious from the file tree:

- **Schema is versioned SQL.** Flyway owns every change under `src/main/resources/db/migration/{h2,mysql,postgresql}/` — one baseline per vendor, selected by Flyway's `{vendor}` placeholder at startup. Hibernate runs with `ddl-auto=validate`, so entity ↔ schema drift becomes a boot-time failure instead of a silent `ALTER TABLE` in prod.
- **Open Session In View is off.** `spring.jpa.open-in-view=false` means service transactions are the only JPA session boundary. Templates get entities with their fetch plans already resolved (`@EntityGraph` where needed, DTOs for list views); lazy loads during rendering are a loud failure, not a silent per-row query storm.
- **Defense in depth.** Untrusted input is refused at six independent boundaries: form DTOs with Jakarta Bean Validation, service-seam primitive parameters (no entity-shaped writes), a custom `@NotBlankHtml` constraint for WYSIWYG empty shells, upload MIME allowlist with UUID filenames (`StorageService`), OWASP HTML allowlist on write (`HtmlSanitizer`), and a CSRF-protected form login.
- **Design tokens as the source of truth for CSS.** Colors, typography, spacing, radii, and shadows live as CSS custom properties on `:root`. Dark mode is three redefinitions of the same tokens, not a second stylesheet. One PNG recolors through a `filter` chain for the light-mode vine wallpaper.

## Tech stack

| Layer | Tech |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4, Spring Security, Spring Data JPA |
| View | Thymeleaf 3.1, Bootstrap 5, Toast UI Editor |
| Persistence | PostgreSQL in production (Neon), H2 for local dev and tests |
| Migrations | Flyway with per-vendor migration directories |
| Security | BCrypt, OWASP Java HTML Sanitizer, CSRF via Spring Security |
| Build | Maven (`./mvnw`), multi-stage Dockerfile |
| Hosting | Render (web service), Neon (managed Postgres), UptimeRobot (keep-alive) |

## Getting started

### Prerequisites

- Java 17
- Maven (or use the included `./mvnw`)
- Docker Desktop, if you want to run against a real Postgres locally

### Run against H2 (fastest)

```bash
./mvnw spring-boot:run
```

The default `local` profile starts an in-memory H2 database and applies the H2 flavour of the Flyway migrations. Visit [http://localhost:8080](http://localhost:8080).

### Run against a real Postgres (closest to production)

```bash
docker compose up --build
```

This builds the production Docker image and runs it against a Postgres 16 container. The app waits on Postgres's `pg_isready` healthcheck before starting Flyway migrations. A PowerShell smoke script wraps the whole thing with health and anonymous-page assertions:

```powershell
.\scripts\smoke-postgres.ps1
```

### Run the tests

```bash
./mvnw test
```

Forty-three tests across four shapes: Mockito unit tests for services, `@WebMvcTest` slices for controllers, `@DataJpaTest` slices for repositories, and a handful of `@SpringBootTest` smoke tests for cross-cutting invariants.

## Deployment

The production build is a single JAR inside a 300MB image, runnable on any PaaS that accepts a `Dockerfile`:

- `-XX:MaxRAMPercentage=75.0` so the JVM respects container memory limits
- Non-root user inside the image
- Graceful shutdown with a 25-second drain so in-flight requests finish during redeploys
- HikariCP sized for a 512MB instance (`max-pool-size=5`, `min-idle=0`) to play nicely with Neon's scale-to-zero free tier
- `/actuator/health` exposed for platform liveness checks

Environment variables the container expects:

| Variable | Purpose |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `postgres` in production |
| `DATABASE_URL` | JDBC URL of the Postgres database |
| `DATABASE_USERNAME` | Postgres user |
| `DATABASE_PASSWORD` | Postgres password |
| `PORT` | Port the app binds on (Render injects this automatically) |
| `ADMIN_USERNAME` | Seeded on first boot if no admin exists |
| `ADMIN_EMAIL` | Seeded on first boot if no admin exists |
| `ADMIN_PASSWORD` | Seeded on first boot if no admin exists |

## Screenshots

### Login
![Login](https://github.com/user-attachments/assets/87690ed5-1342-4e0d-b4e3-4c14d77b0fd6)

### Dashboard
![Dashboard](https://github.com/user-attachments/assets/65e352de-36b8-4ae3-8eba-ca49ea4db49f)

### Post detail
![Post detail](https://github.com/user-attachments/assets/8104ad0c-200d-474c-91aa-ed5f0efbd012)

## Project memory

The codebase's design history lives in [`CLAUDE.md`](CLAUDE.md) — a running log of decisions, trade-offs, and known tech debt, organised by phase. If you want to understand *why* the project looks the way it does rather than just *what* it does, start there.

## Author

Built by [Sirisha Nippani](https://github.com/sirishanippani) as a Scaler Neovarsity Backend Specialization capstone. Teacher-student collaboration with Claude; every design decision committed with a `why`.
