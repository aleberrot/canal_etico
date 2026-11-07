FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn --batch-mode -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/canal-etico-0.0.1-SNAPSHOT.jar /app/canal-etico.jar
USER 10001:10001
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=50.0", "-jar", "/app/canal-etico.jar"]
