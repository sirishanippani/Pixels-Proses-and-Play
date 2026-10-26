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
- [x] **Phase 3** — Service layer. `PostService`, `TagService`, `CommentService`, `AdminService`, `UserService` extracted. Every controller on constructor injection; field injection count = 0. New `com.project.blog.exception` package: `PostOwnershipException`, `PostNotFoundException`, `CommentOwnershipException` (carries `postId`), `CommentNotFoundException`, `UserNotFoundException`. Not-found exceptions annotated `@ResponseStatus(NOT_FOUND)` for direct Spring HTTP mapping; ownership exceptions caught by controllers and translated to redirects. Password encoding moved from `AuthController` into `UserService.register`; service accepts primitives, structurally closing the `/register` mass-assignment hole ahead of Phase 4. Services accept primitive parameters throughout — entity never crosses the service seam on writes. Test suite established the project's three-pattern baseline (unit via Mockito, `@WebMvcTest`, `@DataJpaTest`). Pre-existing bug fixes landed opportunistically: `edit_post.html` content pre-fill, `/post/<deleted-id>` 500 → 404, upload directory auto-create.
- [ ] **Phase 4** — Form DTOs + Bean Validation (**next**). `RegisterRequest`, `PostForm`, `ProfileForm`, `CommentForm`. Closes mass-assignment at the binding target (Phase 3 already closed it at the service seam — this is defense in depth). Teaching focus: `@Valid` + `BindingResult`, `jakarta.validation` constraints, field-level error rendering in Thymeleaf, DTO vs entity binding targets.
- [ ] **Phase 5** — Portability. Replace `RAND()` native query with a portable approach. Add Flyway; set `ddl-auto=validate`. Fix N+1 on tags via `@EntityGraph`. Split `application-mysql.properties` and `application-postgres.properties`. Make the DB layer genuinely pluggable via Spring profiles.
- [ ] **Phase 6** — Security + hygiene. Disable OSIV (`spring.jpa.open-in-view=false`). Delete `/whoami` debug endpoint and `test_logo.html`. Harden file upload (MIME whitelist, drop original filename). Remove `target/` from git tracking, fix `.gitignore`. Replace stack-trace prints with SLF4J.
- [ ] **Phase 6.5** — UI/UX polish. Aesthetic target: *cozy but editorial* — Medium-grade typography, muted palette, generous whitespace, intentional small personality moments (random homepage greeting, post-creation copy, 404 page) rather than emoji-per-sentence density. Centralize hard-coded flash messages into `messages.properties` (i18n-ready; one line in Technologies Used for the report). Add loading states (Toast Editor, search, feed queries). Rewrite copy currently in controllers (`✨ Your thoughts are now out in the wild!`, `🧍‍♀️ Whoopsie!`, `👑 New role unlocked!`, etc.) to editorial-grade register with occasional warmth.
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
- **Strangler-fig refactor over big-bang** for Phase 3. Each controller migrates in its own commit; the codebase is temporarily mixed-state (some controllers through service, some direct to repo) and that's accepted — small reversible steps over one giant unreviewable change.
- **Service methods accept primitives, not entities.** `createPost(user, title, content, tagCsv)` rather than `createPost(Post)`. Structurally confines mass-assignment to the controller's binding layer; even if an attacker sets `role=ROLE_ADMIN` on the form, it cannot cross the service seam. Phase 4's form DTOs close the remaining gap at the binding target.
- **One mapper per DTO even for trivial projections.** `PostSearchResultMapper` is one line of code; kept as a `@Component` because it's a seam — a future JPA constructor projection or caching layer hides behind it without touching the service or controller.
- **Single-item reads return the JPA entity**, not a DTO. CLAUDE.md mandates DTOs for list views; view/edit of one post is a judgment call we take so slices stay small. Reliance on OSIV for lazy-loaded `post.getTags()` in the template is already-logged Phase 6 debt — not new.
- **Ownership failure as a domain exception**, not `Optional`/boolean. Service throws `PostOwnershipException` (no `@ResponseStatus` — handling is a redirect, not an HTTP status). "Not found" is a different cause and gets its own `PostNotFoundException` with `@ResponseStatus(NOT_FOUND)` so Spring maps it to the custom 404 template.
- **Transaction propagation stays at Spring's default (`REQUIRED`).** `TagService.resolveOrCreate` joins `PostService.updatePost`'s write transaction, so tag inserts and post updates commit (or roll back) together. No hand-rolled transaction management.
- **Exceptions in a dedicated package** (`com.project.blog.exception`) rather than alongside the service that throws them. Keeps the service file focused and gives us one import root for Phase 6's eventual `@ControllerAdvice`.
- **UI aesthetic direction set early** (see Phase 6.5): *cozy but editorial*, Medium-adjacent typography and spacing, personality via copy restraint rather than emoji density. Captured now so we don't drift and so copy work isn't done twice (controller flash messages will move to `messages.properties` in the same phase).
- **Testing pyramid in miniature** — the project standardizes on three test shapes. (1) Mockito unit tests (`@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks`) for service business logic — no Spring context, millisecond runtime, cover branches and exception paths. (2) `@WebMvcTest` + `@MockBean` for controller slices — loads MVC + Security + Jackson only, verifies HTTP translation and routing. (3) `@DataJpaTest` for repository slices — real Hibernate + H2, transactional rollback per test, essential for native queries and derived-query spellings. `@SpringBootTest` reserved for cross-cutting smoke and migration bugs; everything else uses the slice tests. The suite grows at the base of the pyramid, not the top.
- **One mapper per DTO even when the mapping is one line.** Reaffirmed by `PostSearchResultMapper`. The mapper is the seam where projection strategy or caching can be swapped without touching the service or controller.
- **Services may call other services for business lookups**, but talk to repositories directly for pure data access. `CommentService.createComment` delegates post lookup to `PostService.getPostForView` so the same `PostNotFoundException` → 404 behavior applies everywhere. Pure index-into-repo reads stay on the repo.
- **Exceptions carry context the caller needs**, not just a message. `CommentOwnershipException` holds the parent `postId` so the controller can redirect back to the post without re-querying. Service decides what context to publish; controller consumes.
- **Admin operations are their own service (`AdminService`), not spread across `UserService`.** Role-privileged ops (`deleteUser`, `toggleUserRole`, `getAllPosts`) and user-privileged ops (`register`, `updateProfile`) have different authorization models and lifecycles — mixing them hides that difference.
- **`Optional` for "may be absent as a normal input domain outcome"; exception for "absent means caller's assumption was violated."** `UserService.findByUsername` returns `Optional` (URL path may name a nonexistent user). `UserService.updateProfile` throws `UserNotFoundException` (authenticated caller is expected to exist; miss means data corruption).

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
- **PostController silently redirects to `/dashboard`** on ownership failure for update/delete — users receive no feedback that their action was rejected → Phase 4 (form error state) or Phase 6.5 (flash message copy).
- **`@ModelAttribute Post`/`@ModelAttribute User` form binding** still permits mass-assignment on fields the service happens to ignore. The service seam protects persistence; the binding target does not. → Phase 4 (`PostForm`, `RegisterRequest`, `ProfileForm` records).
- **Emoji-dense, hard-coded flash messages** in `PostController`, `ProfileController`, `CommentController`, `AdminController` — reads as AI-generated → Phase 6.5 (rewrite + `messages.properties`).
- **`User.getRole()` returns `String`** (overriding Lombok's enum getter via `role.name()`). A field typed `Role` exposed as `String` is a trap for any caller who assumes the Lombok shape. → Phase 4 or 6 (needs audit of Spring Security use sites first).
- **`admin/user/toggle-role/{id}` flash is misleading when user id does not exist** — currently says "Someone just got a promotion…" even on a no-op. → Phase 6.5 (copy pass).
- **`AdminController.toggleUserRole` compares `Role` enum via `.name()` string equality** (`"ROLE_USER".equals(user.getRole())`) because of the `getRole()` String-shadowing above. Will fall out when the `User.getRole()` debt is fixed.
- **Upload directory bug fixed inline** (`Files.createDirectories(path.getParent())`) but the whole upload path still has structural issues: no MIME whitelist, original filename preserved (path traversal / XSS vector), `e.printStackTrace()` instead of SLF4J, working-directory-relative path. → Phase 6 (dedicated `StorageService` + hardening).
- **`ProfileController` still uses `@Value` for `uploadDir`** — belongs in a typed `@ConfigurationProperties` record. → Phase 6 hygiene.
- **`editProfileForm` throws `IllegalStateException`** if authenticated user is missing from DB (data corruption); needs logging + friendly error page → Phase 6.
- **`@MockBean` is deprecated** in Spring Boot 3.4+ (replacement: `@MockitoBean` from `org.springframework.test.context.bean.override.mockito`) → testing hygiene pass, Phase 6.
- **No `@WebMvcTest` for `PostController`** — needs `@AuthenticationPrincipal AppUserDetails` handling (custom principal argument resolver or `.with(user(appUserDetails))`). → Phase 3+ follow-up.
- **No service unit tests for `UserService`, `AdminService`, `CommentService`** — pattern established by `PostServiceTest`/`TagServiceTest`, follow-up slices can add them without new teaching. → Phase 3+ follow-up.

## Test Suite Status

- Total: 15 passing
- Full-context (`@SpringBootTest`):
    - `BlogApplicationTests.contextLoads`
    - `AuthControllerTest.register_persistsUserWithEncodedPassword`
    - `SmokeTest.homePage_loadsForAnonymousUser`
    - `SmokeTest.loginPage_loadsForAnonymousUser`
    - `DataCorruptionBugTest.visitingHomePage_mustNotMutatePostContent`
- Web slice (`@WebMvcTest`):
    - `SearchControllerTest.searchPosts_returnsJsonArrayFromService`
- Data slice (`@DataJpaTest`):
    - `PostRepositoryTest.findByUser_returnsOnlyThatUsersPosts`
    - `PostRepositoryTest.findTop10ByOrderByCreatedAtDesc_returnsRecentFirst`
    - `PostRepositoryTest.findRandomPosts_returnsAtMostThreePosts`
- Service unit (Mockito, no Spring context):
    - `PostServiceTest.createPost_persistsPostWithAuthorTitleContentAndTags`
    - `PostServiceTest.updatePost_overwritesTitleContentAndTagsWhenOwner`
    - `PostServiceTest.updatePost_throwsPostOwnershipExceptionWhenRequesterIsNotOwner`
    - `PostServiceTest.getPostForView_throwsPostNotFoundExceptionWhenMissing`
    - `TagServiceTest.resolveOrCreate_returnsEmptySetForBlankOrNullInput`
    - `TagServiceTest.resolveOrCreate_reusesExistingTagsLowercasesAndCreatesMissingOnes`

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
- **Strangler-fig refactor:** replacing a dependency piece by piece behind a thin interface, keeping the system runnable at every step. Phase 3 is the project's demonstration case.
- **Exceptions as boundary signals:** service throws domain exceptions; `@ResponseStatus` or a `catch` in the controller translates to HTTP. The exception travels; the transport mapping is pluggable (same exception could become `403 Forbidden` in a REST API).
- **Transaction propagation (`REQUIRED`):** cross-service atomicity (`PostService` + `TagService`) without hand-rolled transaction management; the single declarative annotation replaces explicit begin/commit/rollback.
- **Mass-assignment, defense in depth:** structural prevention at the service seam (primitive parameters, no entity-shaped input), binding-level fix in Phase 4 (form DTOs replace raw-entity `@ModelAttribute`).