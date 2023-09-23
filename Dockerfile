FROM maven:3.8.4-openjdk-17-slim AS builder

COPY pom.xml /build/
COPY src /build/src/

WORKDIR /build
RUN mvn -B -e -C -T 1C -DskipTests clean package \
    && rm -rf ~/.m2

FROM openjdk:17-slim

COPY --from=builder /build/target/consumer-*.jar /app/application.jar

WORKDIR /app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]
