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
    - `BlogApplication.java`, `config/` (incl. `UploadProperties` record), `controller/`, `dto/`, `exception/` (incl. `StorageException` carrying a `messageCode`), `flash/` (`FlashService` + `FlashMessage` record + `FlashType` enum — i18n flash via `MessageSource`), `model/`, `repository/`, `security/`, `service/` (incl. `StorageService` for upload boundary, `HtmlSanitizer` for allowlist XSS defense), `validation/`
- `src/main/resources/`
    - `application.properties` (profile-agnostic defaults; sets `spring.profiles.active=local`, `spring.flyway.locations=classpath:db/migration/{vendor}`, `spring.messages.basename=messages`)
    - `application-local.properties` (H2 in-memory, MySQL mode)
    - `application-mysql.properties` (MySQL 8+; env-var driven URL/username/password)
    - `application-postgres.properties` (Postgres; env-var driven; Phase 7 deployment target with Neon)
    - `db/migration/{h2,mysql,postgresql}/V1__init.sql` — per-vendor baseline schema, selected at startup by Flyway's `{vendor}` placeholder
    - `messages.properties` — every user-facing flash string, consumed via `FlashService` + `MessageSource`
    - `templates/` (Thymeleaf; `fragments/head.html` shared `<head>` with parameterized title, `fragments/navbar.html`, `fragments/flash.html` as semantic toast)
    - `static/` (incl. `css/theme.css` token-based design system, `images/vine.png` chinoiserie wallpaper tile)
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
- [x] **Phase 4** — Form DTOs + Bean Validation. `spring-boot-starter-validation` added. Four form records with `jakarta.validation` constraints: `RegisterRequest`, `PostForm`, `ProfileForm`, `CommentForm`. Every write endpoint (register, post create/edit, profile edit, comment) migrated to `@Valid @ModelAttribute <form>` + `BindingResult`; templates bind via `th:object="${...Form}"`, render per-field errors with `th:errors` / `th:errorclass`, and declare `novalidate` so the server is the authoritative validator. Custom `@NotBlankHtml` constraint (annotation + `ConstraintValidator`) in `com.project.blog.validation` for WYSIWYG-backed fields that `@NotBlank` lets through. Comment form uses the Post-Redirect-Get idiom with flashed form + binding result to survive the inline-on-post-detail round-trip. Mass-assignment is now structurally impossible across every form: the DTOs expose only the fields the audience is allowed to set.
- [x] **Phase 5** — Portability. Flyway owns schema (`ddl-auto=validate`); schema is versioned SQL under `db/migration/`. N+1 on tag loading collapsed via `@EntityGraph(attributePaths = {"user", "tags"})` on `findByUser`, `findTop10ByOrderByCreatedAtDesc`, and the overridden `findAllById`. The native MySQL-only `findRandomPosts()` is gone — replaced by `findAllIds()` + `Collections.shuffle` + `findAllById(picked)` in `PostService.getFeaturedPosts`, portable across H2 / MySQL / Postgres. DB layer pluggable by profile: `application-{local,mysql,postgres}.properties` each own only their vendor-specific lines; `application.properties` keeps only DB-agnostic defaults (ddl-auto, flyway.locations, multipart). Flyway's `{vendor}` placeholder resolves to `db/migration/{h2,mysql,postgresql}` at startup — three parallel `V1__init.sql` files, each in its vendor's native syntax (ENUM vs VARCHAR+CHECK for `users.role`; CLOB / LONGTEXT / TEXT for `comments.content`; identity-column syntax). `Comment` entity now `@Table(name = "comments")` so the table name clears Postgres's reserved-word list and matches the plural convention. Dependencies modernized: `mysql:mysql-connector-java` 8.0.33 → `com.mysql:mysql-connector-j` (BOM-managed); GCP Cloud SQL socket factory + `application-prod.properties` deleted; Postgres driver + `flyway-database-postgresql` added.
- [x] **Phase 6** — Security + hygiene. OSIV off (`spring.jpa.open-in-view=false`); `LazyInitializationException` during rendering is now a visible failure mode, not a silent per-row query storm. New fetch plans landed where templates demanded them: `PostRepository.findById` + `findAll` overridden with `@EntityGraph(attributePaths = {"user", "tags"})`; `CommentRepository.findByPost` with `@EntityGraph(attributePaths = "user")`. Debug surface deleted: `/whoami` endpoint and `test_logo.html` removed. `.gitignore` rewritten (grouped sections; negations placed after the patterns they override); the broken bare `application.properties` ignore silently excluded our main profile selector from version control — fixed, file now tracked. SLF4J-only enforcement completed: one remaining `e.printStackTrace()` in `ProfileController` replaced by `log.error`. New `StorageService` encapsulates all upload I/O behind a service seam — MIME whitelist (`image/jpeg`, `image/png`, `image/webp`), UUID filename, extension derived from sanctioned MIME (never from `file.getOriginalFilename()`), with path traversal / Windows reserved names / stored XSS via filename all structurally prevented; `UploadProperties` (`@ConfigurationProperties` record) replaced two `@Value` injections. Stored-XSS defense: `HtmlSanitizer` bean (OWASP Java HTML Sanitizer, allowlist policy = FORMATTING + BLOCKS + LINKS + IMAGES + STYLES) runs inside `PostService.createPost`/`updatePost`, so the DB holds only sanitized HTML and `th:utext` on post content is safe. Testing-hygiene follow-ups: `@MockBean` → `@MockitoBean`; new `PostControllerTest` `@WebMvcTest` slice using `SecurityMockMvcRequestPostProcessors.user(UserDetails)` so `@AuthenticationPrincipal AppUserDetails` resolves. Drive-by fixes found during smoke: `error/404.html` parse typo (`@{/'"}` → `@{/}`), navbar `/posts/create` → `@{/post/create}`, missing `fragments/flash` include on `profile.html`.
- [x] **Phase 6.5** — UI/UX polish. Shipped in two large slices. **Slice 1 (infrastructure):** flash copy centralized through `MessageSource` + a `FlashService` wrapping `RedirectAttributes` with `flash.success/.error/.warning/.info(attrs, messageCode, args)`; `messages.properties` carries every user-facing string, i18n-ready. `StorageException` carries a `messageCode` so controller catches translate to flash keys without stringly-typed mapping. All controller flash sites migrated; `flash.html` fragment reworked as a Bootstrap toast with semantic `.text-bg-*` variants driven by a `FlashType` enum + `FlashMessage` record. **Slice 2 (visual overhaul):** `theme.css` rewritten top to bottom as a tokens-first design system (CSS custom properties for colors, typography, spacing, radii, shadows); the old pastel + glassmorphism chaos is gone. Four-lane semantic palette, each color carrying one meaning only — **rose** (`--heading-rose`) for serif headings, **lavender** (`--accent`, pulled from logo wordmark) for calls to action, **mint** (`--soft-mint`) for people (bylines, commenters, "signed in as" — rendered via `.byline` class), **peach** (`--soft-peach`) for pulled quotes. Typography pair Fraunces (display italic serif) + Inter (UI/body) loaded via a shared `fragments/head.html` so font/stylesheet loading is declared once. All ten page templates rewritten against the new system (login, register, dashboard, profile, post_detail, create_post, edit_post, edit_profile, admin_dashboard, error/404). Chrome `-webkit-autofill` background override killed via the 1000px-inset box-shadow trick (dark-mode password fields were rendering as bright lavender). Transparent chinoiserie vine PNG painted behind all content via `body::before` + CSS filter chain (`invert` + `sepia` + `hue-rotate`) that recolors the one white PNG to muted lavender in light mode, leaves it white in dark mode — opacity 7% light / 4% dark. Also: ownership-rejection flashes landed on `PostController.updatePost`/`deletePost` (users now see "Not your post, bestie" instead of silent redirect). Remaining 6.5 cleanup items pushed to tech debt (loading states, `User.getRole()` string shadowing, `editProfileForm` `IllegalStateException`, client-side `required` re-add).
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
- **Form DTO per input boundary, not per entity.** Phase 4's four records (`RegisterRequest`, `PostForm`, `ProfileForm`, `CommentForm`) exist because *the shape of acceptable HTTP input* is a different concern from *the shape of storage*. A field appearing on a form DTO is an explicit statement that the form's audience is allowed to write to it; a field missing is an implicit refusal that Spring's binder enforces by silently dropping unknown parameters. Combined with Phase 3's service-seam primitives, this is defense in depth: binding and persistence refuse separately.
- **Custom `ConstraintValidator`s extend Jakarta Validation when a built-in isn't enough.** `@NotBlank` passes WYSIWYG empty shells like `<p><br></p>` because they're not whitespace-only; `@NotBlankHtml` strips tags before checking emptiness. Validators are plain classes with no Spring coupling unless they need a bean (then `@Component`). Unit-test them with `new` and plain JUnit.
- **Path identifiers and session identity never belong on a form DTO.** `postId` for post edit comes from `@PathVariable`; `currentUsername` for profile edit comes from `@AuthenticationPrincipal`. Putting them on the form would let an attacker POST a different value than the URL/session says — the DTO carries data the user is changing, nothing else.
- **Post-Redirect-Get for form submissions**, especially when the error-render path shares a template with another controller's GET. `RedirectAttributes.addFlashAttribute(...)` with the magic key `"org.springframework.validation.BindingResult.<attr>"` preserves validation errors across the redirect. The GET handler guards its form seeding with `model.containsAttribute(...)` so the flashed state isn't overwritten by the empty-form seed. Avoids re-POST on refresh and keeps template-population logic in one place.
- **`novalidate` on `<form>` tags** so the server is the single source of truth for validation. HTML5 `required` is UX, not security — any HTTP client bypasses it. Validation rules may be added back selectively as a Phase 6.5 UX polish, but the server-side rules remain authoritative.
- **Schema is versioned SQL, not inferred bytecode.** `ddl-auto=update` is never acceptable past the earliest prototype. Flyway owns schema — one `V__description.sql` per change, filename-based ordering, checksum-protected, applied idempotently via a `flyway_schema_history` table. `ddl-auto=validate` is Hibernate's complement: entity ↔ schema mismatch becomes a loud startup failure instead of silent `ALTER TABLE`.
- **Vendor-specific SQL goes in vendor-specific migration dirs.** Flyway's `{vendor}` placeholder (`spring.flyway.locations=classpath:db/migration/{vendor}`) resolves to the detected DB type at startup; each dir is a complete, standalone history. Three parallel `V1__init.sql` files (H2, MySQL, Postgres) accept slight duplication in exchange for unambiguous per-vendor review.
- **When SQL varies by dialect, push the varying part out of SQL and into Java.** `findRandomPosts()`'s dialect-locked `ORDER BY RAND()` became a two-query Java shuffle: `findAllIds()` (standard JPQL) → `Collections.shuffle` → `findAllById(picked)` (also standard). The non-portable step (randomness) now lives where portability isn't even a question.
- **Spring profiles for the DB layer: `application.properties` holds DB-agnostic defaults; profile files hold *only* their vendor's lines.** Keep datasource / dialect out of the base file — a `${DATABASE_URL:jdbc:h2:...}` fallback silently defaults to H2 when a prod env var is misspelled. Forcing each environment to declare its own datasource is the production-safe shape.
- **Let the Spring Boot BOM manage dependency versions wherever it can.** Explicit `<version>` on managed artifacts is a code smell: security patches arrive with Spring Boot upgrades otherwise, and pinned versions silently rot. Phase 5 flipped `mysql:mysql-connector-java 8.0.33` to `com.mysql:mysql-connector-j` (BOM-managed) as the demonstration case.
- **Table rename beats quote-everywhere for reserved-word collisions.** `comment` was reserved in Postgres; renaming the table to `comments` via one `@Table(name = "comments")` annotation on the entity costs one line and brings the plural-table convention into consistency with `posts`/`users`. Quoting at every SQL site is noisier and easier to forget.
- **Override `JpaRepository` methods to attach `@EntityGraph`.** `findAllById(Iterable<Long>)` is inherited; redeclaring it in the subinterface with `@EntityGraph(attributePaths = {"user", "tags"})` modifies the fetch plan without touching the implementation. Idiomatic Spring Data move whenever a specific call site needs eager-fetching the general default doesn't want.
- **OSIV off; service transactions are the only JPA session boundary.** Phase 6's keystone decision. `spring.jpa.open-in-view=false` kills the servlet filter that kept Hibernate sessions open through rendering. Three harms it closed: Phase 2's dirty-checking bug mechanism, silent per-row N+1 during template iteration, and fictional "read consistency" across the request. The follow-on rule: every entity a template touches must be pre-fetched by the service — via `@EntityGraph` on the repository method, or by projecting to a DTO. `LazyInitializationException` during rendering is now a feature: it marks missing fetch plans visibly instead of hiding them behind lazy proxies.
- **Boundary adapters for infrastructure, mirroring the Phase 3 service seam for business logic.** `StorageService` owns filesystem I/O; `HtmlSanitizer` owns XSS-safe transformation. Controllers never import `java.nio.file.*` or OWASP; they call a thin service. Same shape as `PostRepository` hiding Hibernate — a port the app depends on, with the current adapter swappable (local FS now, S3 / Neon Blob later; OWASP now, DOMPurify-equivalent later). Keeps Phase 7's deployment flexibility intact.
- **Upload hygiene: MIME whitelist + UUID filename + extension-from-MIME.** `file.getOriginalFilename()` is attacker-controlled data; nothing derived from it ever touches disk. The filename on disk is `<uuid>.<ext>` where `ext` comes from the whitelisted content type, so path traversal, Windows reserved names (`CON.jpg`), stored XSS via filename rendering, and case collisions are all structurally prevented — not "mitigated," prevented. Content type is whitelisted (`image/jpeg`, `image/png`, `image/webp`); nothing else gets past the service.
- **`@ConfigurationProperties` record over `@Value`** whenever config has more than one field or is read in more than one place. Typed, validated at bind time, discoverable. `UploadProperties` replaced two scattered `@Value("${profile.upload.dir}")` injections (in `ProfileController` and `WebConfig`), registered via `@EnableConfigurationProperties(UploadProperties.class)` on the consumer `@Configuration`. `@Value` stays acceptable for truly one-off reads of a scalar.
- **Sanitize HTML on write, not on read.** The service seam is the single trust boundary; DB holds only allowlisted HTML; templates can render freely with `th:utext`. Alternative (sanitize-on-read) buys policy flexibility at the cost of a runtime cost on every render and a correctness landmine on any new code path that forgets to sanitize. For a personal blog where reads outnumber writes 10:1 and the service seam already exists, sanitize-on-write dominates.
- **Allowlist beats blacklist, universally.** Applied in Phase 6 to two surfaces: upload MIME types (`StorageService`) and HTML tags/attributes (`HtmlSanitizer` using OWASP's composed `FORMATTING + BLOCKS + LINKS + IMAGES + STYLES`). Same principle, two different libraries. Interview-standard: name a security control that's allowlist-based and one that isn't; the one that isn't is usually broken.
- **`@MockitoBean` is the forward path** in Spring Boot 3.4+. Spring consolidated the Boot-specific `@MockBean` into `spring-test` core as `@MockitoBean` (`org.springframework.test.context.bean.override.mockito.MockitoBean`). Same semantics (replace a context bean with a Mockito mock). Migrate on sight.
- **For `@WebMvcTest` with a custom `@AuthenticationPrincipal` type, use `SecurityMockMvcRequestPostProcessors.user(UserDetails)` — not `@WithMockUser`.** The latter installs Spring Security's generic `User` as the principal, which fails the `@AuthenticationPrincipal AppUserDetails` type cast and silently injects `null`. Passing a real `AppUserDetails` via `.with(user(...))` wraps it in a `UsernamePasswordAuthenticationToken` with the right principal shape; the resolver then injects it correctly. This is the pattern that unblocks `@WebMvcTest` for every controller that takes `@AuthenticationPrincipal AppUserDetails`.
- **Design tokens are the single source of truth for every visual decision.** Phase 6.5's keystone. Color, type, spacing, radii, shadows — all declared once as CSS custom properties on `:root`, consumed via `var(--name)` by every component rule. Dark mode is three redefinitions of the same tokens, not a separate stylesheet. Re-skinning the app is editing ~15 lines in one block; the old `theme.css` had colors inlined across 60+ selectors with `!important` wars between overriding systems. Same discipline as Phase 6's `@ConfigurationProperties` record (`UploadProperties`) — one place writes the value, every reader asks.
- **Semantic color coding — "one color, one meaning."** Each supporting color in the palette carries exactly one semantic role across the whole app. **Rose** = serif headings (your voice). **Lavender** = calls to action (buttons, links, tags). **Mint** = people (bylines, commenters, "signed in as"). **Peach** = pulled quotes. Any use that breaks the rule (mint on a decorative divider for aesthetics, say) breaks the reader's unconscious color-as-map inference and must be caught in review. The principle is why the four-color palette reads as cohesive instead of chaotic: the brain starts using the color as a legend you can't read out loud but can always feel.
- **Flash messages through `MessageSource` + a thin `FlashService`.** Controllers call `flash.success(attrs, "post.created")` instead of hard-coding English copy. `messages.properties` is the single file to edit for copy changes; swapping a single `Locale` argument switches languages. The service wraps `RedirectAttributes.addFlashAttribute("flashMessage", ...)` with a `FlashMessage` record (text + `FlashType` enum) that the `flash.html` fragment reads to pick a semantic toast variant. Domain exceptions that need distinct user-facing copy (e.g., `StorageException`) carry a `messageCode` field so controllers translate via `flash.error(attrs, e.getMessageCode())` without stringly-typed mapping.
- **Shared `<head>` fragment with a parameterized title.** `fragments/head.html` declares `th:fragment="head(title)"` and holds every page's `<meta>`, font `<link>`s, Bootstrap, and `theme.css`. Pages invoke `<head th:replace="~{fragments/head :: head('Page title')}">`. Same reuse idea as a server-side layout/partial — one place to add Open Graph tags, one place to upgrade Bootstrap, one place to swap a font. The Thymeleaf fragment-with-arguments syntax is Thymeleaf's equivalent of a function call for templates.
- **One PNG, two themes via a CSS `filter` chain.** The vine wallpaper is a single transparent monochrome PNG painted by `body::before` with `background-image`. Light mode applies `filter: invert(1) sepia(.35) hue-rotate(220deg) saturate(1.6) brightness(.85)` to recolor the white motifs to a muted lavender; dark mode leaves them white (`filter: none`). The filter chain is the canonical CSS trick to recolor a monochrome image at runtime without a second asset, a `<canvas>` tint, or server-side image processing. Opacity is a separate CSS variable (`--vine-opacity`) that differs per theme.
- **Background on `body`, not `html`, when a decorative layer sits behind content.** Web (and Artifact) convention is that `body` carries the page's ground color. Giving `body` `position: relative` establishes a stacking context; a `body::before` at `z-index: 0` sits above `body`'s background but below `body > *` content (set to `position: relative; z-index: 1`). This is the stable recipe for a full-viewport decorative layer. Putting the background on `html` and leaving `body` transparent *also* works in a plain browser, but breaks in sandboxed/iframed contexts that paint their own ground behind an unstyled body — surfaced the hard way when the vine preview showed checkerboard transparency instead of ivory.
- **Browser autofill is a separate CSS trust boundary.** Chrome's `-webkit-autofill` ignores `background-color` and paints its own fill *over* your CSS layer. The canonical workaround is `-webkit-box-shadow: 0 0 0 1000px var(--surface) inset` on `input:-webkit-autofill` — a shadow large enough to overpaint Chrome's fill from inside the input, in your own color. Pair with `-webkit-text-fill-color: var(--ink)` to force the text color, and a 9999s transition on `background-color` so Chrome can't repaint. This is the dark-mode-password-field fix every production app ends up adding; worth knowing once.

## Known Tech Debt (With Phase Owner)

- **No live Postgres smoke** — H2 tests pass, Postgres `V1__init.sql` written to spec, but nothing verifies it against a real Postgres before Phase 7's first deploy. Testcontainers slice (per-test Postgres via Docker) could pre-empt dialect issues in CI. → Phase 5+ follow-up or Phase 7 pre-flight.
- **MySQL `V1__init.sql` uses `TIMESTAMP(6)`** which strict-mode MySQL deployments can reject (`NO_ZERO_DATE` + implicit default collision). Works on default MySQL 8; if the MySQL profile is reactivated for a real deployment, verify or switch to `DATETIME(6)`.
- **Flyway 10.20 warns on H2 2.3** at startup ("latest supported H2 is 2.2.224"). Noise only; functionally fine. Resolve when Spring Boot's BOM moves Flyway forward.
- **Client-side `required` was removed from forms** when migrating to `novalidate`; browsers no longer prompt on empty fields. Server-side validation catches it, but the UX hop (full form POST → re-render) is heavier than a native prompt. Can re-add `required` on fields that are also `@NotBlank` as a Phase 6.5 cleanup, keeping server as source of truth. → Phase 6.5 cleanup.
- **`User.getRole()` returns `String`** (overriding Lombok's enum getter via `role.name()`). A field typed `Role` exposed as `String` is a trap for any caller who assumes the Lombok shape. → Phase 6.5 cleanup (needs audit of Spring Security use sites first).
- **`AdminController.toggleUserRole` compares `Role` enum via `.name()` string equality** (`"ROLE_USER".equals(user.getRole())`) because of the `getRole()` String-shadowing above. Will fall out when the `User.getRole()` debt is fixed.
- **`AdminController.toggleUserRole` flash is misleading when user id does not exist** — fires the success-case message even on a no-op. → Phase 6.5 cleanup (fix once `User.getRole()` is cleaned).
- **`ProfileController.editProfileForm` throws `IllegalStateException`** if authenticated user is missing from DB (data corruption); needs logging + friendly error page. → Phase 6.5 cleanup (candidate for a `@ControllerAdvice`).
- **No loading states on long operations** — Toast Editor init, search-as-you-type, dashboard feed queries all show no spinner / skeleton while loading. Latency is low locally; will matter on free-tier Postgres from Neon. → Phase 6.5 cleanup (likely a single `.loading` CSS class + a few `aria-busy` wire-ups).
- **Vine wallpaper color is baked via a CSS `filter` chain, not the PNG itself.** Light-mode lavender comes from `filter: invert(1) sepia(.35) hue-rotate(220deg) saturate(1.6) brightness(.85)` applied to a white-on-transparent PNG. If the `--accent` lavender is ever changed to a different hue, the filter chain needs retuning (the filter is a hex-to-filter approximation, not driven by `--accent` directly). → Future accent-color changes.
- **No bootstrap admin provisioning** — `/admin` is unreachable without a pre-existing admin user; H2 console is also blocked by Spring Security defaults (unauthenticated path + CSRF + `X-Frame-Options`). For local admin smoke we currently need a manual DB update via a client that bypasses the web console. → Phase 7 pre-flight (either a profile-guarded seed admin via `CommandLineRunner`, or a one-time Flyway data migration for a known admin, or open `/h2-console` on `@Profile("local")` only).
- **Existing posts predate HTML sanitization** — anything created before the OWASP sanitizer slice still holds raw submitted HTML. Non-issue for a personal blog with no public submission history; would need a one-shot backfill migration before any multi-tenant use.
- **No service unit tests for `UserService`, `AdminService`, `CommentService`** — pattern established by `PostServiceTest`/`TagServiceTest`, follow-up slices can add them without new teaching. → Phase 3+ follow-up.
- **`PostControllerTest` covers only create + edit ownership redirect** — four cases is the demonstration of the `.with(user(AppUserDetails))` pattern; view/delete branches and the comment form seeding path are uncovered. → Phase 6+ follow-up.

## Test Suite Status

- Total: 43 passing
- Full-context (`@SpringBootTest`):
    - `BlogApplicationTests.contextLoads`
    - `AuthControllerTest.register_persistsUserWithEncodedPassword`
    - `SmokeTest.homePage_loadsForAnonymousUser`
    - `SmokeTest.loginPage_loadsForAnonymousUser`
    - `DataCorruptionBugTest.visitingHomePage_mustNotMutatePostContent`
- Web slice (`@WebMvcTest`):
    - `SearchControllerTest.searchPosts_returnsJsonArrayFromService`
    - `PostControllerTest.getCreateForm_seedsEmptyPostForm`
    - `PostControllerTest.savePost_withValidForm_delegatesToServiceAndRedirectsToDashboard`
    - `PostControllerTest.savePost_withBlankTitle_redisplaysFormWithBindingErrors`
    - `PostControllerTest.updatePost_whenServiceThrowsOwnership_redirectsToDashboard`
- Data slice (`@DataJpaTest`):
    - `PostRepositoryTest.findByUser_returnsOnlyThatUsersPosts`
    - `PostRepositoryTest.findTop10ByOrderByCreatedAtDesc_returnsRecentFirst`
    - `PostRepositoryTest.findAllIds_returnsEveryPostId`
    - `PostRepositoryTest.findAllById_returnsOnlyRequestedPosts`
- Service unit (Mockito, no Spring context):
    - `PostServiceTest.createPost_storesSanitizedContentNotRawInput`
    - `PostServiceTest.createPost_persistsPostWithAuthorTitleContentAndTags`
    - `PostServiceTest.updatePost_overwritesTitleContentAndTagsWhenOwner`
    - `PostServiceTest.updatePost_throwsPostOwnershipExceptionWhenRequesterIsNotOwner`
    - `PostServiceTest.getPostForView_throwsPostNotFoundExceptionWhenMissing`
    - `PostServiceTest.getFeaturedPosts_returnsAtMostThree`
    - `PostServiceTest.getFeaturedPosts_returnsAllWhenFewerThanThreePostsExist`
    - `PostServiceTest.getFeaturedPosts_returnsEmptyWhenNoPostsExist`
    - `TagServiceTest.resolveOrCreate_returnsEmptySetForBlankOrNullInput`
    - `TagServiceTest.resolveOrCreate_reusesExistingTagsLowercasesAndCreatesMissingOnes`
    - `StorageServiceTest.store_throwsWhenFileIsNull`
    - `StorageServiceTest.store_throwsWhenFileIsEmpty`
    - `StorageServiceTest.store_rejectsUnsupportedContentType`
    - `StorageServiceTest.store_writesFileAndReturnsUuidNameWithExtensionDerivedFromContentType`
    - `StorageServiceTest.store_usesJpgExtensionForJpegContentType`
    - `HtmlSanitizerTest.nullInputReturnsNull`
    - `HtmlSanitizerTest.stripsScriptTagsEntirely`
    - `HtmlSanitizerTest.stripsInlineEventHandlersOnImages`
    - `HtmlSanitizerTest.stripsJavascriptSchemeLinks`
    - `HtmlSanitizerTest.stripsIframeTags`
    - `HtmlSanitizerTest.stripsInlineEventHandlersOnDivs`
    - `HtmlSanitizerTest.preservesBasicFormatting`
    - `HtmlSanitizerTest.preservesSafeAnchorHrefs`
    - `HtmlSanitizerTest.preservesSafeImageSources`
- Validator unit (plain JUnit, no framework):
    - `NotBlankHtmlValidatorTest.nullIsInvalid`
    - `NotBlankHtmlValidatorTest.emptyStringIsInvalid`
    - `NotBlankHtmlValidatorTest.whitespaceOnlyIsInvalid`
    - `NotBlankHtmlValidatorTest.wysiwygEmptyShellIsInvalid`
    - `NotBlankHtmlValidatorTest.realContentIsValid`

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
- **Jakarta Bean Validation:** declarative input rules on the DTO, enforced by `@Valid` + `BindingResult` in the controller. Custom constraints (annotation + `ConstraintValidator` pair) extend the framework for domain-specific needs (`@NotBlankHtml` for WYSIWYG shells).
- **Post-Redirect-Get:** standard idiom for form submissions. Flash the form and binding result across the redirect; the GET re-renders with user input + inline errors; a page refresh after that doesn't resubmit.
- **Schema as code via Flyway:** versioned SQL migrations under `db/migration/`, checksum-protected, idempotent. Replaces Hibernate's inferred schema generation. `ddl-auto=validate` provides the entity ↔ schema matching check.
- **Portability through vendor-specific migrations:** the same logical schema in three physical representations via Flyway's `{vendor}` placeholder. Illustrates how dialect divergence is handled without forking the application code.
- **N+1 fix with `@EntityGraph`:** per-call-site declaration of which associations to fetch; keeps entity defaults LAZY while giving list queries a constant-query-count fetch plan.
- **Open Session In View anti-pattern:** Spring Boot's single most-criticized default. Phase 6 turned it off; the knock-on discipline (explicit fetch plans, services as transaction scopes, DTOs for list views) is what actually earns the "production-quality" label.
- **Defense in depth, surveyed:** by the end of Phase 6, the codebase had refused untrusted input at six distinct boundaries — entity shape (DTOs, Phase 2), service seam (primitive params, Phase 3), form binding (`@Valid` form DTOs, Phase 4), validation (`@NotBlankHtml`, Phase 4), upload I/O (MIME whitelist + UUID filename in `StorageService`, Phase 6), and rendered HTML (OWASP allowlist in `HtmlSanitizer`, Phase 6). Same principle (allowlist), same architectural shape (thin trust boundary), six independent surfaces.
- **Boundary adapters / Ports and Adapters (Hexagonal):** `StorageService` and `HtmlSanitizer` as filesystem / sanitization ports with swappable adapters; same shape as Spring Data's `JpaRepository` port over Hibernate. Interview-ready vocabulary for the "why isn't it just in the controller?" question.
- **Stored XSS and the OWASP allowlist principle:** `@NotBlankHtml` ≠ sanitization — validation checks shape, sanitization transforms content. Sanitize-on-write dominates sanitize-on-read for a read-heavy blog with a single service seam. OWASP Java HTML Sanitizer is the canonical Java library; compose `Sanitizers.*` policies rather than hand-rolling.
- **Testing-pyramid continuity:** `PostControllerTest` demonstrates the `.with(user(UserDetails))` principal-injection pattern required when `@WithMockUser`'s generic principal doesn't satisfy a custom `@AuthenticationPrincipal` type — the piece that had kept `PostController` out of `@WebMvcTest` since Phase 3.
- **Design tokens as the production discipline for CSS:** Phase 6.5 rebuilt `theme.css` from `:root` tokens on up — the same principle as Spring's `@ConfigurationProperties` record or a shared `i18n` bundle: one authoritative definition, many readers. The dark mode story becomes three redefinitions of the same tokens, not a separate stylesheet, which is also the modern replacement for the SASS `$variable` workflows older projects used.
- **Semantic color coding (one color, one meaning):** the editorial-palette discipline that separates a cohesive four-color scheme from a four-color zoo. Mint only ever means "a person"; rose only ever means "a heading"; etc. Interview-ready formulation: color is a visual analog to a type system — stable meaning for a stable visual lane lets readers infer without reading.
- **i18n-ready flash copy:** Phase 6.5 moved every user-facing string from controller literals into `messages.properties` consumed through a thin `FlashService` wrapper over Spring's `MessageSource`. The payoff isn't just translation — it's the standard enterprise pattern for keeping copy out of code. A `Locale` argument switches languages; a copy change edits one file.
- **Boundary adapter pattern applied to UX feedback, not just infra:** `FlashService` stands at the controller/transport seam the same way `StorageService` stands at the filesystem seam. Controllers call `flash.success(attrs, code)` — they know nothing about `RedirectAttributes` key names, `MessageSource` lookup, `Locale`, or the `FlashMessage` record. The service boundary makes copy, i18n, and toast rendering independently changeable.
- **One PNG, two themes through CSS `filter`:** the vine wallpaper is one transparent monochrome asset. Light mode recolors via `filter: invert(1) sepia(.35) hue-rotate(220deg) saturate(1.6) brightness(.85)`; dark mode leaves it white. The same discipline that keeps schema in versioned SQL instead of asset variants: single source, context-aware transform.
- **Chrome autofill as a CSS trust boundary:** `-webkit-autofill` ignores `background-color` by design — browsers paint autofill above the author's layer to avoid a dark-background invisible-text accessibility trap. The 1000px-inset box-shadow workaround is the production fix every real app learns the day someone tests the login form in dark mode.