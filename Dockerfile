# Etapa 1: Construcción
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Usamos -DskipTests para compilar más rápido y evitar errores de test de conexión en el build
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución
FROM eclipse-temurin:21-jdk
WORKDIR /app

# CORRECCIÓN: Copiamos cualquier .jar generado en target (Spring Boot genera uno solo ejecutable)
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]