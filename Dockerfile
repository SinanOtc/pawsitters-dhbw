# 1. Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Maven-Dependencies separat cachen — bleiben gültig solange pom.xml unverändert.
# Spart bei Code-only-Changes mehrere Minuten Build-Zeit.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Source kopieren und bauen. Tests werden hier übersprungen,
# weil CI sie auf jedem PR schon ausführt — Docker-Build = Packaging.
COPY src ./src
RUN ./mvnw clean package -DskipTests -B \
    && mv target/*.jar target/app.jar

# 2. Runtime
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Non-root User - Security-Hygiene, app läuft nicht als root im Container
RUN groupadd -r spring && useradd -r -g spring spring

# JAR aus Build-Stage übernehmen — bleibt root-owned, ist read-only zur Laufzeit
COPY --from=build /app/target/app.jar app.jar

USER spring

# Spring Boot Default-Port. Railway überschreibt via $PORT-Env-Var
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]