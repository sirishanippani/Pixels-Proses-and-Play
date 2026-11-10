# 9. Conclusion

This chapter closes the report. Section 9.1 states what the project set out to do and what it delivered. Section 9.2 enumerates the engineering takeaways that the work produced. Section 9.3 argues for the practical applicability of the techniques outside this project. Section 9.4 states the project's limitations candidly. Section 9.5 lists the post-submission work that the project will continue with.

## 9.1 What the Project Set Out to Do and What It Delivered

The Pixels, Proses & Play project began as a Spring Boot capstone built against an early stage of the Scaler Neovarsity Backend Specialization curriculum. The original build shipped the feature set of a working blogging platform (registration, authoring, commenting, tagging, administration) but did so with the shortcuts that a learning project typically carries: field injection in place of constructor injection, JPA entities bound directly to Thymeleaf templates, no validation beyond browser-side `required` attributes, no HTML sanitization, no schema migrations, no separation between production and development configuration, and no live deployment that persisted long enough for a grader to visit.

The project rebuilt the application under a phased refactor pipeline, documented in Chapter 2 and threaded through Chapters 4 through 7 of this report. The refactor reshaped the codebase into a form that holds up under the scrutiny expected of a production application: constructor injection throughout, a service seam that accepts primitives rather than entities, form data transfer objects with Jakarta Bean Validation constraints enforced server-side, OWASP-allowlisted HTML stored at the persistence layer, versioned Flyway migrations with a per-vendor dialect strategy, Open Session In View disabled with explicit `@EntityGraph` fetch plans at each call site, a token-based design system supporting light and dark themes from one source, a multi-stage container image sized for a free-tier runtime, and a live deployment at `https://pixels-proses-and-play.onrender.com` backed by a managed PostgreSQL on Neon and monitored by an external uptime prober.

The automated test suite grew alongside the refactor and stands at forty-three passing tests at submission time, distributed across unit, slice, and full-context shapes. The repository's git history is retained as design documentation; every commit carries a conventional-commit prefix and a body that argues for the change and flags any technical debt the change introduced or revealed.

## 9.2 Key Takeaways

The refactor pipeline produced ten engineering takeaways that the project considers generalizable beyond its own scope. Each is stated as a durable principle followed by one or two sentences on its concrete realisation in this codebase.

**Safety-net tests come before structural change.** Phase 1 wrote six baseline tests against the inherited codebase before the first refactor commit landed. The tests did not need to be comprehensive; they needed to catch the broad class of regressions that a large reshaping would cause. The pattern is the standard precondition for any strangler-fig refactor of a running system and is what separates a safe rewrite from a rolling bet.

**Separation of view model from persistence entity.** The data-corruption bug fixed in Phase 2 was the first argument for the pattern; Chapter 6's N+1 fix was the second. A Thymeleaf template that binds to a JPA entity is a bug waiting for its trigger; a template that binds to a DTO is not. The seam between the two shapes is the service method's return type, and the discipline of projecting there removes an entire category of lifecycle hazards.

**Constructor injection and primitive-service seam.** The refactor removed every field-level `@Autowired` from the codebase and reshaped the service layer to accept primitive parameters at its public methods. The two patterns together make dependencies explicit (any class's dependencies are listed in its constructor signature) and make mass assignment structurally impossible at the service boundary (an entity-shaped payload cannot cross a primitive-parameter method call).

**Schema as code through Flyway.** The project owns its schema as versioned SQL under source control, applied idempotently at startup, and validated by Hibernate at startup through `ddl-auto=validate`. The pattern replaces Hibernate's inferred `ALTER TABLE` behaviour with an explicit, auditable, checksum-protected migration history. The decision is non-optional for any application that reaches production.

**Allowlist over blacklist, applied consistently across trust boundaries.** The codebase refuses untrusted input at six boundaries using the same principle each time: entity shape (DTOs), service seam (primitive parameters), form binding (form DTOs), validation (`@NotBlankHtml`), upload I/O (MIME whitelist plus UUID filename), and rendered HTML (OWASP allowlist policy). The uniformity is the design feature.

**Open Session In View off plus explicit `@EntityGraph` fetch plans.** Chapter 6 argued the case at length. The two changes together reshape the mental model of the persistence layer: the service is the only session boundary, every template field is pre-materialised, and `LazyInitializationException` during render is reclassified as a loud signal for a missing fetch plan. The pair is the single highest-leverage architectural decision the project made.

**Boundary adapters for infrastructure.** `StorageService` and `HtmlSanitizer` each stand at an infrastructure seam (filesystem and sanitization) in the same way that `PostRepository` stands at the persistence seam. The controllers do not import `java.nio.file.*` or OWASP; they call a thin service. The current adapters can be swapped (S3 for local disk, a different sanitizer policy) without any change to the callers.

**Design tokens as a tiny, enforceable style system.** The Phase 6.5 rewrite of `theme.css` reduced the colour, typography, and spacing decisions to a block of CSS custom properties on `:root`. The block is redefined under `@media (prefers-color-scheme: dark)` and under `:root[data-theme="dark"]`, so dark mode is three redefinitions of the same tokens rather than a separate stylesheet. The discipline makes re-skinning the application a fifteen-line edit.

**Portability claims need per-vendor verification.** Phase 5 claimed portability through Flyway's `{vendor}` placeholder; Phase 7's smoke against a real PostgreSQL found the `@Lob String content` to `pg_largeobject` mapping bug that H2's MySQL-compatibility mode had masked for several phases. The lesson is that a happy test against one database is not evidence for another; the per-vendor smoke script is cheap enough that there is no reason not to have it.

**Containers as the deployment contract.** The Dockerfile is the single source of truth for what the application needs to run. Render runs `docker build`; Fly.io runs `docker build`; a hypothetical Kubernetes deploy would run `docker build`. The platform choice becomes a weekend migration rather than a rewrite, which is the property that keeps a free-tier deployment viable: when the free tier changes its terms, the application moves.

## 9.3 Practical Applications

The techniques used in this project transfer directly to industry and interview contexts across three broad application domains.

**Backend web applications with server-side rendering.** The archetypal application is the admin console or internal tool: a Spring Boot (or Rails, or Django) codebase that renders HTML server-side, holds relational data in PostgreSQL, authenticates against form login with session cookies, and ships via a container image. The techniques of this project apply directly: the DTO pattern for templates, the form-DTO pattern for input binding, the Flyway schema-as-code discipline, the `@EntityGraph` fetch-plan declarations, and the container-plus-managed-database deployment model are all the current defaults in enterprise Java shops. The project's code can be read as a reference implementation of the pattern.

**REST or GraphQL backends with a separate front-end.** The same techniques apply, with two substitutions: the Thymeleaf templates are replaced by a JSON view layer (Jackson for REST; a Spring GraphQL or Netflix DGS integration for GraphQL), and the form DTOs become request-body DTOs. Everything else transfers unchanged. The N+1 story in Chapter 6 is if anything more important in a GraphQL backend, because GraphQL resolver-per-field patterns naturally invite N+1 regressions.

**Financial services and e-commerce platforms.** The combination of Java 17 LTS, Spring Boot 3, PostgreSQL, and Flyway is the current baseline stack across finance and e-commerce: core banking at Temenos and Finastra, payments at Stripe's Java SDK consumers, e-commerce at Spree and JHipster-generated applications, order fulfilment at Deliveroo. The patterns the project demonstrates (constructor injection, DTO projection, allowlist sanitization, schema migrations) are directly the patterns these shops use. The interview-grade value of the project lies in being able to point at a specific commit on a specific codebase and name the pattern behind the change.

**Educational and reference applications.** The project is written with a teaching register deliberately. Chapter 6's deep-dive on `@EntityGraph` and Open Session In View is a complete, measurable, before-and-after case study of the N+1 anti-pattern that the project believes is more useful as a teaching resource than most of the articles on the subject in circulation. The chapter can be read in isolation by a developer encountering the pattern in their own codebase and reused as a reference implementation of the fix.

## 9.4 Limitations

The project is submitted with known limitations, catalogued honestly. The submission does not claim that these are open research problems; they are items that a next iteration of the project would address.

**No continuous-integration pipeline.** The repository does not carry a GitHub Actions workflow at submission time. Tests are run locally and the container builds on Render on every push; a merged change that broke the test suite would still deploy. The missing piece is a workflow that runs `./mvnw test` on every push and gates the Render deploy behind a green run. The slice is small (one YAML file) and is at the top of the post-submission backlog.

**No metrics beyond liveness.** The Actuator exposure is restricted to `/actuator/health`; `/metrics`, `/prometheus`, and `/info` are not exposed to the public internet. An outage detection story is in place through UptimeRobot, but a performance-degradation story (p99 latency rose, GC time jumped, pool saturation) is not. A follow-up would expose Micrometer metrics on a protected endpoint and scrape them into a hosted Grafana Cloud free-tier dashboard.

**Inline images are stored as base-64 data URIs.** The current post editor inlines images as base-64 `data:` URIs inside the post HTML rather than uploading them through a separate endpoint. The maximum content length was raised to five million characters to accommodate the pattern. The correct architecture is a `/post/image` endpoint that delegates to `StorageService.store` and returns a URL that the editor inserts as a plain `<img src="…">` tag. The follow-up is tracked in the project's known-debt log.

**No cache tier.** The application does not run a Redis or Memcached cache. The decision is defensible for the current traffic profile but limits the application's ability to absorb a traffic spike. A future iteration would add a Spring-cache-backed layer in front of `PostService.getRecentPosts` and `PostService.getFeaturedPosts`, with a one-minute time-to-live.

**No internationalisation beyond the string table.** The `messages.properties` bundle that drives flash messages is in English only. The infrastructure to switch locales is in place (the bundle reads through Spring's `MessageSource`, a `Locale` is resolved on every request); the content to populate a second locale is not. Adding a second language is work, not design.

**Admin flash message is misleading on no-op.** `AdminController.toggleUserRole` fires the success flash message even when the target user does not exist. The bug is caused by the `User.getRole()` method's string-shadowing of Lombok's enum getter, which complicates the fix slightly. The two items are tracked together in the known-debt log.

**No Testcontainers in the test suite.** Repository slice tests run against H2 in `MODE=MySQL`, which is a happy approximation of a real database. The Phase 7 smoke script catches the dialect-specific regressions that H2 would miss, but a continuous-integration run against a real PostgreSQL through Testcontainers would catch them earlier and more reliably. The slice is a natural extension of the existing test suite.

**Free-tier cold start is visible to the first user.** The combination of Render's fifteen-minute spin-down threshold and Neon's five-minute scale-to-zero means that the first visitor to the live application after a long idle window sees a one-minute first-page load. The UptimeRobot prober keeps the chain warm during business hours but does not run frequently enough during overnight hours to prevent an occasional cold start. The pattern is the cost of zero hosting; the trade-off is accepted knowingly.

## 9.5 Future Work

The following items are planned for the first post-submission iteration of the project, listed in rough priority order.

- Add a GitHub Actions workflow that runs the Maven test suite on every push and gates the Render deployment on a green run.
- Implement the `/post/image` upload endpoint and migrate the Toast UI Editor configuration off base-64 inlining.
- Add Testcontainers-backed PostgreSQL integration tests to the slice-test suite.
- Expose Micrometer metrics on a protected Actuator endpoint and wire up a Grafana Cloud free-tier dashboard.
- Introduce a Spring-cache layer in front of the home-page feed queries with a short time-to-live.
- Resolve the `User.getRole()` string-shadowing debt and the associated `AdminController.toggleUserRole` flash-message bug together.
- Backfill service unit tests for `UserService`, `AdminService`, and `CommentService` using the pattern established by the existing `PostServiceTest`.
- Extend `@WebMvcTest` coverage of `PostController` to the view and delete branches.

Beyond the backlog, two broader directions are open. The first is a REST-plus-React fork of the project that keeps the service, persistence, and security layers unchanged and replaces the Thymeleaf templates with a JSON API. The fork would exercise the service seam's shape under a different presentation layer and would surface any coupling that the current server-side-rendering choice hides. The second is a rewrite of the data-access layer onto jOOQ instead of JPA, which would surface the N+1 pattern of Chapter 6 at compile time rather than at runtime and would simplify the per-vendor migration strategy of Chapter 5.

The project as submitted is the first stable version of the codebase that holds up under the standard the Scaler Backend Specialization curriculum sets. The report in this document is the record of the decisions that produced it. The live deployment at `https://pixels-proses-and-play.onrender.com` is the running instance that the decisions describe.
