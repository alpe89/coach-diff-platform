FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /coach-diff
# Get the pom to cache it as it rarely changes
COPY pom.xml .

RUN mvn dependency:go-offline

COPY src/ ./src
# Build the project skipping tests, they are ran in CI
RUN mvn package -DskipTests

FROM eclipse-temurin:25-jre AS runtime

# Create non-root group and user
RUN groupadd docker-users && useradd -G docker-users coach-diff-user

USER coach-diff-user

WORKDIR /coach-diff

# Get the built artifact from previous stage
COPY --from=builder /coach-diff/target/*.jar coach-diff.jar

EXPOSE 8080

CMD ["java", "--enable-preview", "-jar", "coach-diff.jar"]