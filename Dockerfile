FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN sed -i 's/\r$//' mvnw \
    && chmod +x mvnw \
    && ./mvnw -B dependency:go-offline

COPY src/ src/

RUN ./mvnw -B package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --uid 1001 --create-home studioops

COPY --from=build /workspace/target/*.jar app.jar

USER studioops

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]