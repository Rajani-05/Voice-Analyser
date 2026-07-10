# Stage 1: Build React Frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build Spring Boot Backend
FROM maven:3.9.6-eclipse-temurin-21-alpine AS backend-builder
WORKDIR /app/backend-java
COPY backend-java/pom.xml ./
RUN mvn dependency:go-offline
COPY backend-java/src ./src
RUN mvn package -DskipTests

# Stage 3: Run Application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app/backend-java

# Copy built frontend assets to the expected relative path
COPY --from=frontend-builder /app/frontend/dist /app/frontend/dist

# Copy Spring Boot built JAR
COPY --from=backend-builder /app/backend-java/target/*.jar app.jar

# Run the backend on port 8000
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
