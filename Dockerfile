# syntax=docker/dockerfile:1.7
# Multi-stage build: fat Maven/JDK image builds the jar; slim JRE image runs it.
# The runtime image carries no build tools, no source code, no .m2 cache.

# ---------- Stage 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# Prime the dependency cache in its own layer so iterative code changes
# don't re-download the internet. If pom.xml is unchanged, Docker reuses
# this layer and skips go-offline entirely.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Now copy sources and build. -DskipTests because the container build is
# an artifact step, not a test step; tests run in CI / locally via Maven.
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Run as non-root. The container filesystem is read-only to the app except
# for /app/uploads, which is volume-mounted at runtime for user uploads.
RUN groupadd --system app && useradd --system --gid app --home /app app \
    && mkdir -p /app/uploads \
    && chown -R app:app /app

COPY --from=builder --chown=app:app /build/target/*.jar /app/app.jar

USER app

EXPOSE 8080

# MaxRAMPercentage tells the JVM to use 75% of the container's memory limit
# as its heap ceiling. Required because the JVM otherwise sees the HOST's
# RAM, not the container's — on Render's 512MB free instance, that math is
# catastrophic. Container-aware JVM has been default since Java 10, but the
# percentage knob still needs to be set.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
