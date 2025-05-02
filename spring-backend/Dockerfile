# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app
COPY . .

# Install the local PayMongo jar into the Maven repository inside the container
COPY libs/paymongo-java-1.0.0.jar paymongo-java-1.0.0.jar
RUN mvn install:install-file -Dfile=paymongo-java-1.0.0.jar \
                             -DgroupId=com.paymongo \
                             -DartifactId=paymongo-java \
                             -Dversion=1.0.0 \
                             -Dpackaging=jar

# Run Maven build after installing dependencies
RUN mvn clean package -DskipTests

# Stage 2: Final Runtime Image
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]