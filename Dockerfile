FROM amazoncorretto:21-alpine AS builder

WORKDIR /app

RUN apk add --no-cache curl unzip bash

RUN curl -fsSL "https://services.gradle.org/distributions/gradle-8.12-bin.zip" \
         -o /tmp/gradle.zip && \
    unzip -q /tmp/gradle.zip -d /opt && \
    rm /tmp/gradle.zip
ENV PATH="/opt/gradle-8.12/bin:$PATH"

COPY build.gradle      build.gradle
COPY settings.gradle   settings.gradle
COPY gradle.properties gradle.properties

RUN gradle dependencies --no-daemon --quiet 2>&1 | tail -5 || true

COPY src/ src/
RUN gradle bootJar --no-daemon -x test

RUN ls -lh build/libs/

# ── Runtime ──────────────────────────────────────────
FROM amazoncorretto:21-alpine AS runtime

RUN apk add --no-cache wget

RUN addgroup -g 1001 -S appgroup && \
    adduser  -u 1001 -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /app/build/libs/rag-knowledge-base.jar app.jar

RUN mkdir -p /app/uploads /app/logs && \
    chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Use exec form - no shell, no quoting issues, JVM flags explicit
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseG1GC", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=docker", \
  "-jar", "app.jar"]
