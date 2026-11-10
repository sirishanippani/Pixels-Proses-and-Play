# 8. Technologies Used

This chapter describes each technology that makes up the Pixels, Proses & Play platform, explains how the technology is used in the project, lists its common real-life applications outside the project, and comments briefly on the alternatives that were considered and declined. The chapter is organised by architectural layer, from the language and runtime at the foundation to the hosting platform at the edge. Table 8.01 summarises every technology covered in this chapter with its version, its role, and the subsection in which it is discussed.

**Table 8.01: Summary of technologies used in the Pixels, Proses & Play platform.**

| Layer | Technology | Version | Role in the project | Section |
|---|---|---|---|---|
| Language and runtime | Java (OpenJDK, Temurin distribution) | 17 (LTS) | Implementation language and JVM | 8.2 |
| Application framework | Spring Boot | 3.4.5 | Auto-configuration, embedded Tomcat, dependency management | 8.3 |
|  | Spring MVC | (via Boot) | Request routing, binding, view resolution | 8.3 |
|  | Spring Security | (via Boot) | Authentication, authorization, CSRF, BCrypt | 8.3 |
| Persistence | Spring Data JPA | (via Boot) | Repository interfaces, derived queries, @EntityGraph | 8.4 |
|  | Hibernate | 6.6 | JPA implementation, SQL generation, caching | 8.4 |
|  | HikariCP | (via Boot) | JDBC connection pooling | 8.4 |
|  | Flyway | 10.20 | Versioned SQL schema migrations | 8.4 |
| Database | PostgreSQL | 16 | Production database (Neon) | 8.5 |
|  | H2 | 2.x | Development and test database | 8.5 |
| Presentation | Thymeleaf | 3.1 | Server-side HTML templating | 8.6 |
|  | Bootstrap | 5 | Base CSS framework and component library | 8.6 |
|  | Toast UI Editor | 3.x | Rich-text / Markdown post editor | 8.6 |
| Validation & sanitization | Jakarta Bean Validation | 3 | Declarative input validation on form DTOs | 8.7 |
|  | OWASP Java HTML Sanitizer | current | Allowlist HTML sanitization for post content | 8.7 |
| Build & code hygiene | Apache Maven | 3.9 | Build tool, dependency manager | 8.8 |
|  | Project Lombok | current | Boilerplate reduction on entities and constructors | 8.8 |
| Testing | JUnit Jupiter | 5 | Test runner and assertions | 8.9 |
|  | Mockito | (via Boot) | Mocking framework for unit tests | 8.9 |
|  | Spring Test | (via Boot) | `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest` slices | 8.9 |
| Containerisation and hosting | Docker | (OCI) | Reproducible application image | 8.10 |
|  | Render | free tier | Application runtime (web service) | 8.10 |
|  | Neon | free tier | Managed PostgreSQL | 8.10 |
|  | UptimeRobot | free tier | External uptime prober and keep-alive | 8.10 |

## 8.2 Language and Runtime: Java 17

**What it is.** Java 17 is the current long-term support release of the Java platform from Oracle, the OpenJDK community, and the various downstream distributors (Temurin, Corretto, Azul Zulu). Long-term support status means that the version receives security and bug-fix updates for an extended horizon independently of the half-yearly feature release cadence, which is the property that matters for a production codebase.

**How the project uses it.** The project is written in Java 17 and runs on the Temurin 17 JRE in production. Three Java-17-era language features are used routinely. Records are the canonical representation for every DTO and form in the `com.project.blog.dto` package (`PostSummaryDto`, `PostForm`, `RegisterRequest`, `CommentForm`, `ProfileForm`, `PostSearchResultDto`, `FlashMessage`); a record declares the field list, generates an all-arguments constructor, final accessors, `equals`, `hashCode`, and `toString`, and is deeply immutable by construction. Text blocks are used for the OWASP policy configuration and for long SQL constants where JPQL or `@Query` annotations carry them. Enhanced `switch` with pattern matching is used where branching on `instanceof` would otherwise read as a ladder. The JVM is run inside a container with the `-XX:MaxRAMPercentage=75.0` flag, which is the production-safe setting for a container-aware JVM, covered in detail in Chapter 7.

**Real-life applications.** Java 17 is the current baseline for most enterprise Java deployments: it is the minimum version for Spring Boot 3 and for the Jakarta EE 10 specifications, both of which require the JDK's module system and the newer `javax` to `jakarta` package migration. The language is dominant in financial services (trading systems at every large bank), e-commerce (Amazon, eBay), online video (Netflix), and distributed-systems infrastructure (Apache Kafka, Apache Cassandra, Elasticsearch). The enterprise case for Java rests on the JVM's tooling maturity, the backwards-compatibility guarantees across major versions, and the depth of the available library ecosystem.

**Alternatives.** Kotlin on the JVM was considered because Spring Boot has first-class Kotlin support and because Kotlin's null-safety and data classes would simplify the DTO layer further. The project stayed with Java because the capstone is submitted against a Scaler curriculum that teaches Java, and because the project's educational value is higher when the language matches the curriculum. Rewriting a service or two in Kotlin without rewriting the whole application is a natural post-submission exercise.

## 8.3 Application Framework: Spring Boot, Spring MVC, Spring Security

**What they are.** Spring Boot is an opinionated distribution of the broader Spring ecosystem that packages auto-configuration, an embedded servlet container, and a curated set of dependency versions through a managed BOM. Spring MVC is the servlet-stack web framework within the Spring family; Spring Security is the framework-level authentication and authorization component.

**How the project uses them.** Spring Boot 3.4.5 is the single-artefact dependency root of the project. The `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-thymeleaf`, `spring-boot-starter-validation`, and `spring-boot-starter-actuator` starters are declared in the project's `pom.xml`; Boot's BOM governs every downstream version. The `@SpringBootApplication` entry class (`BlogApplication.java`) runs the embedded Tomcat on startup.

Spring MVC is used for every request-handling controller in `com.project.blog.controller`. The framework resolves `@GetMapping`, `@PostMapping`, and `@RequestMapping` on the eight controllers; its argument-resolution chain handles `@PathVariable`, `@RequestParam`, `@ModelAttribute`, `@Valid`, `@AuthenticationPrincipal`, `BindingResult`, and `RedirectAttributes` on the handler methods. The framework's view resolver maps a returned view name (such as `home` or `redirect:/dashboard`) to a Thymeleaf template or to a server-side redirect response.

Spring Security configures the filter chain declaratively in `SecurityConfig.java`. The chain enforces authorization by URL pattern (`/admin/**` requires `ROLE_ADMIN`, `/post/create` and `/dashboard` require any authenticated user, everything else is anonymous), configures BCrypt as the password encoder, binds the form-login flow on the standard `/login` endpoint with a redirect to the originally requested URL on success, and permits anonymous access to `/actuator/health` so that UptimeRobot can prober the application. CSRF protection is on the default (enabled for POST and other write methods, disabled for GET); every form template renders the CSRF token through the `_csrf` placeholder that Spring Security's Thymeleaf dialect supplies.

**Real-life applications.** Spring Boot is the dominant Java backend framework. Representative public users include Netflix (microservices substrate), Uber (rider and driver services), Trivago (hotel-search backend), Deliveroo (order-fulfilment system), and essentially every bank that has modernised onto a service-oriented architecture in the past decade. The framework's documented adoption is sustained by the combination of opinionated defaults (which lower the cost of starting new services), a mature auto-configuration system (which removes most boilerplate), and strong backwards-compatibility across minor versions.

**Alternatives.** Micronaut and Quarkus were considered as lighter alternatives with faster startup times and smaller memory footprints; both were declined because the project's teaching goals include the Spring idiom specifically. Spring WebFlux (reactive) was considered and declined because a reactive stack is incompatible with a servlet-stack Thymeleaf front-end and because none of the project's workloads are I/O-bound in the way that reactive stacks reward.

## 8.4 Persistence: Spring Data JPA, Hibernate, HikariCP, Flyway

**What they are.** Spring Data JPA is the Spring sub-project that provides repository interfaces and derived-query parsing on top of the Jakarta Persistence API. Hibernate is the JPA implementation that Spring Data uses by default. HikariCP is the JDBC connection pool that Spring Boot wires in by default. Flyway is a database-migration tool that applies versioned SQL files in filename order and records their checksums in a history table.

**How the project uses them.** The four repository interfaces (`PostRepository`, `UserRepository`, `TagRepository`, `CommentRepository`) extend `JpaRepository<T, Long>`. Spring Data generates the implementations at runtime and parses the method names into JPQL (`findByUser`, `findByUsername`, `findByTitleContainingIgnoreCase`, `findTop10ByOrderByCreatedAtDesc`). Three repository methods override inherited methods to decorate them with `@EntityGraph(attributePaths = {"user", "tags"})`; one method is declared with an explicit `@Query` (`findAllIds()`). The pattern is examined in depth in Chapter 6.

Hibernate is run with `spring.jpa.open-in-view=false` and `spring.jpa.hibernate.ddl-auto=validate`. The first setting removes the Open Session In View servlet filter so that Hibernate sessions close at the service boundary. The second setting rejects a startup where the entity model and the live schema disagree, which turns an accidental mapping mismatch into a loud failure before the application accepts traffic.

HikariCP is configured for the free-tier container's memory envelope: `maximum-pool-size=5`, `minimum-idle=0`, `idle-timeout=240000`, `connection-timeout=10000`. The values are discussed in Chapter 7. Hikari's defaults are tuned for mid-size VMs and would be wasteful on the free-tier instance.

Flyway holds the schema under `src/main/resources/db/migration/{vendor}`, with three parallel migration directories (`h2`, `mysql`, `postgresql`). The `{vendor}` placeholder is resolved at startup against the detected database type. Each directory holds a complete, standalone `V1__init.sql` in the vendor's native dialect. The decision to accept per-vendor duplication over lowest-common-denominator SQL is documented in Chapter 5.

**Real-life applications.** Spring Data JPA and Hibernate together dominate the Java persistence layer in enterprise deployments. Specific adopters include the banking core-banking platforms Temenos and Finastra, the e-commerce platform Spree, and the open-source project management tool OpenProject. HikariCP is the de-facto standard JDBC pool for JVM applications and ships as Spring Boot's default. Flyway is the standard schema-migration tool in the Java world for applications that reject `ddl-auto=update`; its principal commercial competitor is Liquibase. Both tools are used by Netflix, Google Cloud's internal Java tooling, and the AWS SDK sample applications.

**Alternatives.** jOOQ was considered as an alternative to JPA for the data-access layer. jOOQ generates type-safe Java code from the live schema and offers a fluent SQL DSL; it would have made the dialect-portability work of Phase 5 simpler and would have surfaced the N+1 problem of Chapter 6 much earlier because jOOQ does not offer lazy proxies. The project stayed with JPA because the Scaler curriculum teaches JPA and because the N+1 problem is itself a teaching case; the rewrite onto jOOQ is a natural post-submission exercise.

## 8.5 Databases: PostgreSQL and H2

**What they are.** PostgreSQL is a widely-deployed open-source relational database management system with a reputation for correctness, extensibility, and SQL-standard compliance. H2 is an open-source in-process relational database written in Java, used predominantly for development and testing.

**How the project uses them.** PostgreSQL 16 is the production database, hosted on Neon's managed service (covered in Chapter 7). The application connects through Neon's pooler endpoint with `prepareThreshold=0` on the JDBC URL. H2 2.x is used in-memory for the local development profile and for every automated test; the H2 instance is configured in MySQL compatibility mode (`MODE=MySQL`) during development, which is what the earlier phases of the project ran against before Phase 5 introduced the Postgres profile. The Flyway migration directory selects the correct vendor at startup, so the application uses each database on its native dialect.

**Real-life applications.** PostgreSQL is used by Apple's iCloud services, Instagram's user graph, Reddit's content store, Spotify's analytics stack, and most of the hosted-database offerings on major clouds (AWS RDS Postgres, Google Cloud SQL Postgres, Azure Database for PostgreSQL). The database's extensibility (custom types, custom functions, foreign data wrappers) makes it a common choice for applications that would otherwise outgrow a plain relational model; PostGIS, TimescaleDB, and pgvector are extensions that respectively add geospatial, time-series, and vector-similarity capabilities to a standard PostgreSQL install.

H2 is used by essentially every Java test suite that wants an in-memory relational database for `@DataJpaTest` slices and integration tests. The database is small enough to boot in milliseconds and fully supports the subset of SQL the JPA/Hibernate stack generates, which is the set of properties a test harness actually needs.

**Alternatives.** MySQL was the project's original production database (before Phase 5). It was retired in favour of PostgreSQL because PostgreSQL is better supported by Neon's free tier, has stricter default behaviour around invalid data, and is the current default choice in Scaler's curriculum. SQLite was considered for local development as an alternative to H2 and declined because Hibernate's SQLite support is less mature than its H2 support. Testcontainers backed by a real PostgreSQL in a Docker container was considered for the test suite and is tracked as a post-submission enhancement; the current H2-backed tests catch most regressions and the Phase 7 smoke script against a real PostgreSQL catches the dialect-specific ones.

## 8.6 Presentation: Thymeleaf, Bootstrap 5, Toast UI Editor

**What they are.** Thymeleaf is a server-side Java templating engine that renders XML-compliant HTML by interpreting attribute-based directives on otherwise valid HTML markup. Bootstrap 5 is a widely-adopted CSS framework providing a grid system, typography, form controls, and a component library. Toast UI Editor is a dual-mode WYSIWYG and Markdown editor implemented in JavaScript.

**How the project uses them.** Thymeleaf renders every page in the application other than the single JSON-returning search endpoint. Ten page templates sit under `src/main/resources/templates/`, with four shared fragments under `templates/fragments/`: a parameterised `head.html` fragment that every page replaces its own `<head>` with, a `navbar.html` that every page reuses, a `flash.html` toast fragment that the `FlashService` output renders through, and a single footer fragment. Thymeleaf's natural-template property means the same HTML file can be opened statically in a browser for design review and will degrade gracefully with the attributes ignored; the project exploits the property during CSS iteration.

Bootstrap 5 is loaded through a CDN link in the shared `head` fragment. The project uses Bootstrap's grid system for responsive layout on every page, Bootstrap's form components for input styling (overridden by the project's own design tokens for colour and typography), and Bootstrap's `toast` component for the semantic flash messages described in Chapter 2.

Toast UI Editor is loaded on the post-creation and post-edit pages and is initialised against the hidden `<textarea>` that the form binds to. The editor emits HTML into the hidden field on submit. The project's Phase 6 slice and a Phase 7 follow-up added support for the editor's Markdown mode alongside its default WYSIWYG mode. The HTML the editor produces is sanitised by `HtmlSanitizer` inside `PostService` before it reaches the database.

**Real-life applications.** Thymeleaf is the standard choice for server-side Java templating when the application has not committed to a JavaScript-heavy single-page architecture; adopters include the JHipster project-generation framework, which shipped Thymeleaf as its default template engine for most of its lifetime, and most tutorial-grade Spring projects. Bootstrap is the most-used front-end framework on the public internet by a wide margin and is the default CSS choice for admin dashboards, marketing sites, and prototypes across every language. Toast UI Editor is used by naver.com's content management stacks (the editor is a NAVER project) and by a long tail of blog and knowledge-base platforms that need a dual-mode editor.

**Alternatives.** React with a Spring Boot JSON backend was the alternative front-end stack considered. The combination offers stronger client-side interactivity and a cleaner separation between presentation and transport, but it doubles the codebase, requires a separate build pipeline, and would force every teaching case in the server-side Java world into a secondary role. The project stayed with Thymeleaf because the chapter structure of the report, the teaching goals of the Scaler curriculum, and the fit with the stack all argue for a server-side template engine. A tailwind-first CSS approach was considered as an alternative to Bootstrap and declined because the project's design tokens live in a plain CSS custom-property block that works without a compilation step.

## 8.7 Validation and Sanitization: Jakarta Bean Validation, OWASP Java HTML Sanitizer

**What they are.** Jakarta Bean Validation (JSR 380, previously known as Bean Validation) is a specification for declarative validation on Java objects through annotation-driven constraints. The reference implementation is Hibernate Validator. The OWASP Java HTML Sanitizer is an allowlist-based HTML sanitization library maintained by the Open Web Application Security Project.

**How the project uses them.** Jakarta Bean Validation is applied to every form DTO in the project: `RegisterRequest`, `PostForm`, `ProfileForm`, `CommentForm`. The standard constraints `@NotBlank`, `@Email`, `@Size`, and `@Positive` are used throughout. One custom constraint, `@NotBlankHtml`, is defined in the `com.project.blog.validation` package; the annotation plus `ConstraintValidator` pair rejects WYSIWYG empty shells (`<p><br></p>` and variants) that `@NotBlank` lets through. The constraint fires on `PostForm.content`, which is the one field where the WYSIWYG empty-shell problem is realistic.

The OWASP Java HTML Sanitizer is wrapped in the `HtmlSanitizer` service bean. The service composes the OWASP library's `Sanitizers.FORMATTING`, `Sanitizers.BLOCKS`, `Sanitizers.LINKS`, `Sanitizers.IMAGES`, and `Sanitizers.STYLES` policies into a single `PolicyFactory` and exposes a `sanitize(String html)` method. The method is called inside `PostService.createPost` and `PostService.updatePost` so that the database only ever holds allowlisted HTML and the Thymeleaf templates can render the content through `th:utext` without a runtime sanitization step.

**Real-life applications.** Jakarta Bean Validation is the standard validation layer for every Spring Boot web application and for Jakarta EE applications more broadly; the specification is implemented in both reference (Hibernate Validator) and alternative (Apache BVal) forms. The OWASP Java HTML Sanitizer is used by Google (the library is maintained by Google employees as an OWASP-hosted project), by content-management platforms that accept user-submitted HTML, and by email clients that render incoming HTML.

**Alternatives.** Manual validation inside controller methods was considered and declined for the obvious reasons: validation logic in the controller does not re-use across endpoints, is easy to forget on a new endpoint, and reads worse than annotations on the DTO. For HTML sanitization, the JSoup library was considered as an alternative; JSoup offers a sanitisation API through its `Safelist` abstraction but is primarily a parser and would require more wiring to achieve the same policy. The OWASP library is the specialised tool for the job and was chosen on that basis.

## 8.8 Build and Code Hygiene: Maven, Lombok

**What they are.** Apache Maven is a build and dependency-management tool for JVM projects, driven by a declarative XML descriptor. Project Lombok is an annotation-driven code-generation library that reduces boilerplate on JVM classes by injecting getters, setters, constructors, and other mechanical code at compile time.

**How the project uses them.** The project is built with the Maven Wrapper (`./mvnw`) for reproducibility; the wrapper pins the Maven version against the repository and removes the need for a developer to install Maven locally. The Spring Boot BOM governs every dependency version; the `pom.xml` lists starters and application-specific libraries (OWASP sanitizer, Toast UI integration) without version numbers, because the BOM supplies them. Explicit version pins are flagged as code smells in the project's commit discipline (see the Phase 5 commit that retired an explicit `mysql-connector-java` pin in favour of the BOM-managed `com.mysql:mysql-connector-j`).

Lombok is used on entities (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` on `User`, `Post`, `Comment`, `Tag`) and on services and controllers (`@RequiredArgsConstructor`, which generates the constructor that Spring's constructor injection uses). The Lombok dependency is scoped `provided` in Maven and is compile-time only.

**Real-life applications.** Maven is one of two dominant JVM build tools (the other being Gradle); it powers approximately half of the open-source Java projects on GitHub and is the convention in enterprise Java shops. Lombok is adopted widely enough to be considered standard in the Spring community and comes pre-integrated with every major IDE through plugins.

**Alternatives.** Gradle was considered as an alternative to Maven and declined because the project's size does not warrant Gradle's build-logic flexibility and because Maven's declarative XML is easier for a new reader to audit. Java 17 records remove much of Lombok's motivation for DTOs and value classes; the project uses records throughout for exactly that reason, and Lombok remains in use only for the JPA entities and for the service/controller constructor-injection pattern. A gradual retirement of Lombok in favour of hand-written constructors is a plausible post-submission cleanup.

## 8.9 Testing: JUnit 5, Mockito, Spring Test

**What they are.** JUnit Jupiter 5 is the current generation of the JUnit testing framework. Mockito is a mocking library for JVM unit tests. Spring Test is the family of test-support classes within the Spring Framework that provides application-context bootstrapping, context slicing (`@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`), and request-simulation utilities (`MockMvc`).

**How the project uses them.** The test suite comprises forty-three passing tests across three shapes (the shape conventions are described in Chapter 2's phased pipeline and are catalogued in Chapter 3's NFR-MNT-04). Mockito-based unit tests exercise service business logic in milliseconds with no Spring context. `@WebMvcTest` slices exercise individual controllers with the MVC and security infrastructure wired up and the service bean mocked. `@DataJpaTest` slices exercise repository methods against a real Hibernate stack and an in-memory H2 database, with each test rolled back on completion. `@SpringBootTest` is reserved for cross-cutting smoke tests such as `BlogApplicationTests.contextLoads` and for the one regression guard (`DataCorruptionBugTest`) that needs a full context.

Three testing-specific patterns are worth noting. First, `@MockitoBean` replaces the earlier Spring Boot `@MockBean` across the test suite; the migration was completed in Phase 6. Second, the `@WebMvcTest` tests that require a custom `@AuthenticationPrincipal AppUserDetails` use `SecurityMockMvcRequestPostProcessors.user(UserDetails)` rather than `@WithMockUser`; the latter installs a generic principal that fails the type cast. Third, the tests favour unit over integration coverage, in line with the testing-pyramid shape documented in the project's architectural decisions.

**Real-life applications.** JUnit 5 is the standard test framework for every modern JVM codebase. Mockito is the standard mocking library in the JVM world. Spring Test's slice annotations are used by essentially every Spring Boot application; the combination of `@WebMvcTest` for controller tests, `@DataJpaTest` for repository tests, and `@SpringBootTest` for cross-cutting tests is the canonical shape of a Spring Boot test suite.

**Alternatives.** TestNG was considered as an alternative test framework; JUnit 5 was chosen because Spring Boot's documentation and tooling assume JUnit 5 and because TestNG's parallel-execution advantages do not apply at the project's scale. AssertJ was adopted as the fluent-assertion library alongside JUnit's own assertion API; Hamcrest was considered and declined because AssertJ's error messages read better on failure. Testcontainers for integration testing against a real PostgreSQL is tracked as a post-submission enhancement.

## 8.10 Containerisation and Hosting: Docker, Render, Neon, UptimeRobot

**What they are.** Docker is the dominant implementation of the Open Container Initiative image format and container runtime. Render is a platform-as-a-service provider that accepts OCI images and manages the orchestration, networking, and TLS termination. Neon is a serverless PostgreSQL provider with a scale-to-zero compute plane. UptimeRobot is a hosted uptime-monitoring service that issues periodic HTTP probes against a configured URL.

**How the project uses them.** The project's `Dockerfile` is a two-stage multi-build file: a Maven-and-JDK builder stage that produces the application jar, and a slim JRE runtime stage that runs the jar as a non-root user. The image is approximately two hundred megabytes and boots in under a minute on the free-tier runtime. The production deployment on Render is a single web service pointed at the project's GitHub repository, configured with the seven environment variables documented in Chapter 7. Neon hosts the PostgreSQL instance behind a transaction-pooled endpoint; the application connects through HikariCP with the `prepareThreshold=0` parameter that the pooler requires. UptimeRobot probes `/actuator/health` every five minutes to catch outages and to keep the free-tier runtime warm against its fifteen-minute spin-down threshold.

**Real-life applications.** Docker is essentially universal in modern application deployment; container orchestration platforms from Kubernetes down to small platforms-as-a-service operate on OCI images produced by Dockerfile builds. Render is used by a long tail of indie and startup projects and is operated by a team that previously built similar developer-experience tooling at GitHub and Rackspace. Neon is operated by a team formerly from PostgreSQL's core contributor roster and is one of the current generation of serverless Postgres providers (alongside Supabase and Xata). UptimeRobot is used by small operations teams and solo developers who need a simple external probe without the overhead of configuring Pingdom or Datadog Synthetics.

**Alternatives.** Kubernetes with a provider-managed Postgres (GKE + Cloud SQL, EKS + RDS) was considered for the deployment and declined on cost and complexity for a solo capstone. Fly.io was considered alongside Render; both would have worked equally well. Supabase was considered alongside Neon; Neon won on its connection-pooling story. Pingdom and StatusCake were considered alongside UptimeRobot; UptimeRobot's free tier is the most generous of the three for the project's probe-interval needs.

The complete deployment stack costs zero per month at the time of submission. The cost model is understood to be sustainable so long as traffic stays within the free-tier envelopes, and is straightforward to migrate off (any component can be replaced individually without rewriting the application) if that envelope is ever exceeded.
