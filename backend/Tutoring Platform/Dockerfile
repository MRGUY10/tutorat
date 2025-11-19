# -----------------------------
# Étape 1 : Build avec Maven
# -----------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copier uniquement le pom.xml d’abord (optimise le cache)
COPY pom.xml .

# Télécharger les dépendances avant de copier tout le code
RUN mvn dependency:go-offline -B

# Copier le reste du projet
COPY src ./src

# Construire le projet + générer le jar
RUN mvn clean package -DskipTests


# -----------------------------
# Étape 2 : Image de Runtime
# -----------------------------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copier le jar depuis l’étape de build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port 8080 (optionnel)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
