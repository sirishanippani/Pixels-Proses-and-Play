# Pixels, Prose, and Play — Project Memory

> **Read this first.** This file is the durable context for all AI-assisted work on this project. Update it at the end of every session (see "Session Protocol" at the bottom).

## Project Identity

- **Repo:** sirishanippani/Pixels-Proses-and-Play
- **Purpose:** Spring Boot blogging platform. Started as a Scaler Neovarsity capstone project. Was briefly hosted on GCP App Engine + Cloud SQL (MySQL), then taken down due to cost.
- **Current goal:** Refactor into a production-quality codebase, write the capstone report against the Scaler Neovarsity Backend Specialization template, host for free, submit.

## Working Mode

- **Strict teacher/student mode.** The user is learning production engineering, not shipping fast. Every non-trivial change gets:
    1. **Why** we are doing it (the bigger-picture principle).
    2. **What** the change is.
    3. **How** it maps to concepts the user can defend in a code review or interview.
- Depth should be calibrated to the concept's weight. Scoped teaching, not lectures. If the user says "deviating too much," compress to one-line "why" per step until they ask for depth again.
- **Verify before suggesting.** Do not guess framework behavior. If uncertain, run a diagnostic (print, test, log) before claiming a cause.
- **Pair every critique with a constructive path forward.** Be critical, not pessimistic.
- **Lead with the bottom line.** No greetings, no filler. Scannable formatting.

## Tech Stack

- Java 17, Spring Boot 3.4.5, Hibernate 6.6, Thymeleaf 3.1
- Spring Security (form login + BCrypt), `thymeleaf-extras-springsecurity6`
- Maven (via `./mvnw`)
- H2 (local dev + tests), MySQL (originally prod); Postgres targeted for free hosting in Phase 5
- Lombok (`@Getter`/`@Setter` on entities)

## Repository Shape

- `src/main/java/com/project/blog/` — code
    - `BlogApplication.java`, `config/`, `controller/`, `dto/`, `model/`, `repository/`, `security/`, `service/`
- `src/main/resources/`
    - `application.properties` (default; sets `spring.profiles.active=local`)
    - `application-local.properties` (H2 in-memory, MySQL mode)
    - `application-prod.properties` (MySQL via Cloud SQL socket factory — needs rework in Phase 5)
    - `templates/` (Thymeleaf), `static/`
- `src/test/java/com/project/blog/` — tests

## Conventions

- **Constructor injection** everywhere. No `@Autowired` on fields in new code. Migrate existing code to constructor injection as we touch it.
- **DTOs are Java `records`.** Carry primitives/strings/collections of primitives — never JPA entities.
- **Mappers are `@Component` classes.** One mapper per DTO. Centralize transformation logic.
- **Commit messages** follow Conventional Commits (`feat:`, `fix:`, `refactor:`, `chore:`, `test:`) with a body explaining *why* when the change isn't self-evident. Flag known tech debt in the commit body.
- **SLF4J for logging**, never `System.out.println`. Delete any `println` or `e.printStackTrace()` we encounter.
- **Thymeleaf expressions bind to DTO fields** on list views, never to JPA entities (prevents dirty-checking corruption).

## Phased Refactor Plan

- [x] **Phase 0** — Secrets purge *(skipped by user: creds were dummy, repo never leaked public)*
- [x] **Phase 1** — Safety net: H2 local profile, `application.properties` default, 6 passing safety-net tests (`PostRepositoryTest`, `AuthControllerTest`, `SmokeTest`)
- [x] **Phase 2** — Data-corruption bug: `PostSummaryDto` (record, 6 fields including `tagNames`), `PostSummaryMapper` (`@Component`, centralized HTML-strip + truncate + tag sort). `HomeController` and `DashboardController` migrated to constructor injection + DTO mapping. `DataCorruptionBugTest` added as regression guard. All 7 tests passing.
- [ ] **Phase 3** — Service layer (**next**). Extract `PostService`, `UserService`, `CommentService`, `AdminService`, `ProfileService`. Move business logic out of controllers one at a time. Migrate every controller to constructor injection as we go. Teaching focus: `@Transactional` boundaries, anemic domain vs transaction script, framework-type leakage (principal shape).
- [ ] **Phase 4** — Form DTOs + Bean Validation. `RegisterRequest`, `PostForm`, `ProfileForm`, `CommentForm`. Closes mass-assignment vulnerability on `/register` (currently binds raw `User` entity, lets attacker set `role=ROLE_ADMIN`).
- [ ] **Phase 5** — Portability. Replace `RAND()` native query with a portable approach. Add Flyway; set `ddl-auto=validate`. Fix N+1 on tags via `@EntityGraph`. Split `application-mysql.properties` and `application-postgres.properties`. Make the DB layer genuinely pluggable via Spring profiles.
- [ ] **Phase 6** — Security + hygiene. Disable OSIV (`spring.jpa.open-in-view=false`). Delete `/whoami` debug endpoint and `test_logo.html`. Harden file upload (MIME whitelist, drop original filename). Remove `target/` from git tracking, fix `.gitignore`. Replace stack-trace prints with SLF4J.
- [ ] **Phase 7** — Deployment. Recommended stack: Render (free web service) + Neon (free Postgres). Dockerfile with `-XX:MaxRAMPercentage=75.0`, env-var secrets, `/health` endpoint, UptimeRobot keep-alive every 14 minutes.
- [ ] **Report** — 40+ pages against Scaler Neovarsity Backend Specialization template. Sections: Abstract, Project Description, Requirement Gathering, Class Diagrams, DB Schema Design, Feature Development Process, Deployment Flow, Technologies Used, Conclusion, References. Report writing happens in parallel with Phase 5 onward.

## Decisions Made (Don't Re-Litigate)

- **H2 with `MODE=MySQL`** for local/test, not Testcontainers. Trade-off accepted: faster feedback over full prod parity. Testcontainers can be added later for integration tests.
- **Spring profiles** for env config, not a custom config class. Twelve-Factor App alignment.
- **Records for DTOs**, not Lombok `@Value` or plain POJOs. Immutability + modern Java signal.
- **One mapper per DTO**, not a static factory on the DTO itself. Testable in isolation, injectable.
- **Tag names as `List<String>` on `PostSummaryDto`**, not `List<TagDto>`. View only needs display strings.
- **Mapper sorts tag names** to avoid non-deterministic Set iteration order in UI.
- **`DataCorruptionBugTest` kept as a forward-looking invariant**, even though it didn't reproduce the bug in MockMvc. The invariant "post content survives a page visit" is correct regardless.
- **OSIV investigation deferred to Phase 6.** MockMvc doesn't fully replicate the OSIV filter chain. Fix is to disable OSIV globally, not to perfectly reproduce the bug.

## Known Tech Debt (With Phase Owner)

- **N+1 queries** on tag loading in home/dashboard list views → Phase 5 (`@EntityGraph`).
- **`findRandomPosts()` native query uses MySQL `RAND()`** → Phase 5 (replace with portable approach).
- **OSIV still enabled** (Spring Boot default) → Phase 6 (`spring.jpa.open-in-view=false`).
- **`application-prod.properties` tied to GCP Cloud SQL socket factory** → Phase 5/7 (rework for Render + Neon).
- **No Flyway migrations** → Phase 5.
- **Mass-assignment risk** on `/register` (entity binding) → Phase 4.
- **Debug endpoint `/whoami`** in `AdminController` → Phase 6 (delete).
- **`test_logo.html`** debug template committed → Phase 6 (delete).
- **`target/`** committed to git; `.gitignore` has malformed `target/` block → Phase 6.
- **MySQL driver uses old `mysql-connector-java` coordinates** → Phase 5 (switch to `com.mysql:mysql-connector-j`, let BOM manage version).
- **No real tests** beyond safety-net; need @DataJpaTest for `PostRepository.findRandomPosts`, service-layer unit tests with Mockito → Phase 3+.

## Test Suite Status

- Total: 7 passing
- `BlogApplicationTests.contextLoads` (default)
- `PostRepositoryTest.findByUser_returnsOnlyThatUsersPosts`
- `PostRepositoryTest.findTop10ByOrderByCreatedAtDesc_returnsRecentFirst`
- `AuthControllerTest.register_persistsUserWithEncodedPassword`
- `SmokeTest.homePage_loadsForAnonymousUser`
- `SmokeTest.loginPage_loadsForAnonymousUser`
- `DataCorruptionBugTest.visitingHomePage_mustNotMutatePostContent`

## Commit Discipline

- Commit at the end of every logical unit of work, not at the end of a session.
- Multi-line commit messages with `why` in the body.
- Flag tech debt discovered during the commit in the body.

## Session Protocol

**At the start of a session:**
- Read `CLAUDE.md` fully (this file).
- Read recent git log: `git log --oneline -20`.
- Ask the user: "Resuming from \[last commit\]. Current phase: \[phase from checklist\]. Ready to proceed, or any detour first?"

**At the end of a session, the user says:**
> **"Update CLAUDE.md"**
>
> When the user says this, you (Claude) must:
> 1. Update the phase checklist (mark completed items, note partial progress).
> 2. Append any new decisions to "Decisions Made".
> 3. Append any new tech debt to "Known Tech Debt" with a phase owner.
> 4. Update "Test Suite Status" if new tests were added or old ones changed.
> 5. **Do not** add session narrative or dates. This is a living design doc, not a diary.
> 6. Commit the updated `CLAUDE.md` with message: `docs: update project memory [brief summary]`

## Report Context (For Later)

Report template is Scaler Neovarsity Backend Specialization — see uploaded template in prior web chat. Sections to fill: Abstract (300 words max), Project Description with diagrams, Requirement Gathering (functional + non-functional + use cases + feature table), Class Diagrams (draw.io), Database Schema Design (textual + diagrammatic), Feature Development Process, Deployment Flow, Technologies Used, Conclusion (key takeaways, applications, limitations), References. 40+ pages. Font: Times New Roman, 14/12, 1.5 line spacing, 1.25" side margins.

## Project Report Narrative Threads (Capture As We Go)

Use these as talking points when writing the report. Add examples as they arise.

- **TDD / safe refactoring:** safety-net tests before structural change.
- **DTO pattern as SRP:** entity vs view model separation; dirty-checking consequences.
- **Constructor injection as DIP:** making dependencies explicit; testability.
- **Spring profiles as Twelve-Factor config:** environment-selected config without code change.
- **Anti-Corruption Layer (DDD):** keeping framework types (`AppUserDetails`) out of business logic.
- **Conventional Commits + commit-as-documentation:** `git log` as a readable design history.
- **Known-debt discipline:** flagging in commit messages, not hiding.