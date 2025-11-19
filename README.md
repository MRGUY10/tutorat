# 📚 Plateforme de Tutorat - Guide d'Installation Complet

Une application web full-stack pour gérer les sessions de tutorat, connecter les étudiants avec les tuteurs, et faciliter les interactions éducatives.

---

## 📋 Table des Matières

- [Technologies](#technologies)
- [Prérequis](#prérequis)
- [Structure du Projet](#structure-du-projet)
- [Configuration du Backend](#configuration-du-backend)
- [Configuration du Frontend](#configuration-du-frontend)
- [Démarrage de l'Application](#démarrage-de-lapplication)
- [Identifiants par Défaut](#identifiants-par-défaut)
- [Documentation API](#documentation-api)
- [Dépannage](#dépannage)

---

## 🛠️ Technologies

### Backend
- **Java 21**
- **Spring Boot 3.5.5**
- **Spring WebFlux** (Réactif)
- **Spring Security** avec JWT
- **Spring Data R2DBC** (Base de données réactive)
- **PostgreSQL** (Base de données)
- **Maven** (Outil de build)

### Frontend
- **Angular 19.2.0**
- **TypeScript 5.8.2**
- **Tailwind CSS 4.1.13**
- **Angular Material 19.2.18**
- **RxJS 7.8.1**
- **Chart.js 4.5.0**
- **FontAwesome 6.7.2**

---

## ✅ Prérequis

Avant d'exécuter le projet, assurez-vous d'avoir installé les éléments suivants :

### Logiciels Requis

| Logiciel | Version | Lien de Téléchargement |
|----------|---------|------------------------|
| **Java JDK** | 21 ou supérieur | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) ou [OpenJDK](https://adoptium.net/) |
| **Node.js** | 18.x ou supérieur | [nodejs.org](https://nodejs.org/) |
| **npm** | 9.x ou supérieur | (Inclus avec Node.js) |
| **PostgreSQL** | 14 ou supérieur | [postgresql.org](https://www.postgresql.org/download/) |
| **Maven** | 3.8+ | [maven.apache.org](https://maven.apache.org/download.cgi) |
| **Git** | Dernière version | [git-scm.com](https://git-scm.com/) |

### Optionnel (Recommandé)
- **Angular CLI**: `npm install -g @angular/cli`
- **IntelliJ IDEA** ou **Eclipse** (pour le développement backend)
- **VS Code** (pour le développement frontend)
- **Postman** (pour tester l'API)

---

## 📁 Structure du Projet

```
tutorat_etudiants/
├── backend/
│   └── Tutoring Platform/
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/iiil/
│       │   │   └── resources/
│       │   │       ├── application.yml
│       │   │       ├── application-dev.yml
│       │   │       └── schema.sql
│       │   └── test/
│       ├── pom.xml
│       ├── mvnw
│       └── mvnw.cmd
│
└── front-end/
    ├── src/
    │   ├── app/
    │   │   ├── core/
    │   │   │   ├── guards/
    │   │   │   ├── interceptors/
    │   │   │   ├── models/
    │   │   │   └── services/
    │   │   ├── components/
    │   │   ├── layouts/
    │   │   └── shared/
    │   ├── assets/
    │   ├── environments/
    │   └── styles.scss
    ├── angular.json
    ├── package.json
    └── tsconfig.json
```

---

## 🗄️ Configuration du Backend

### Étape 1 : Configuration de la Base de Données

1. **Démarrer le service PostgreSQL**

2. **Créer la Base de Données**
   ```sql
   CREATE DATABASE tutoring_db;
   ```

3. **Créer un Utilisateur** (Optionnel)
   ```sql
   CREATE USER tutoring_user WITH PASSWORD 'votre_mot_de_passe';
   GRANT ALL PRIVILEGES ON DATABASE tutoring_db TO tutoring_user;
   ```

4. **Configurer la Connexion à la Base de Données**

   Modifier `backend/Tutoring Platform/src/main/resources/application-dev.yml` :

   ```yaml
   spring:
     r2dbc:
       url: r2dbc:postgresql://localhost:5432/tutoring_db
       username: tutoring_user
       password: votre_mot_de_passe
   ```
5.Avant de lancer l'application, exécute le fichier schema.sql situé dans le dossier resources

### Étape 2 : Compiler le Backend

Naviguer vers le répertoire backend :

```bash
cd backend/Tutoring\ Platform
```

**Option A : Utiliser Maven Wrapper (Recommandé)**
```bash
# Windows
mvnw.cmd clean install

# Linux/Mac
./mvnw clean install
```

**Option B : Utiliser Maven**
```bash
mvn clean install
```

### Étape 3 : Exécuter le Backend

**Option A : Utiliser Maven**
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**Option B : Utiliser un IDE**
- Ouvrir le projet dans IntelliJ IDEA ou Eclipse
- Exécuter la classe principale (généralement `Application.java` ou `TutoringApplication.java`)

**Option C : Utiliser le fichier JAR**
```bash
java -jar target/tutoring-0.0.1-SNAPSHOT.jar
```

Le serveur backend démarrera sur **http://localhost:8080**

### Vérifier que le Backend Fonctionne
```bash
curl http://localhost:8080/api/health
# ou ouvrir dans le navigateur
```

---

## 💻 Configuration du Frontend

### Étape 1 : Installer les Dépendances

Naviguer vers le répertoire frontend :

```bash
cd front-end
```

Installer les packages npm :

```bash
npm install
```

> **Note** : Cela peut prendre 2-5 minutes selon votre connexion internet.

### Étape 2 : Configurer l'Environnement

Le frontend est préconfiguré pour se connecter à `http://localhost:8080`.

Si votre backend s'exécute sur un port différent, modifier `src/environments/environment.ts` :

```typescript
export const environment = {
  production: false,
  BASE_URL: "http://localhost:8080"  // Changer si nécessaire
};
```

### Étape 3 : Exécuter le Frontend

Démarrer le serveur de développement :

```bash
npm start
# ou
ng serve
```

Le frontend démarrera sur **http://localhost:4200**

### Compiler pour la Production

```bash
npm run build
# ou
ng build
```

Les fichiers de production seront dans le dossier `dist/`.

---

## 🚀 Démarrage de l'Application

### Démarrage Rapide (Les Deux Serveurs)

#### Terminal 1 - Backend :
```bash
cd backend/Tutoring\ Platform
./mvnw spring-boot:run
```

#### Terminal 2 - Frontend :
```bash
cd front-end
npm start
```

### Accéder à l'Application

1. **Frontend** : Ouvrir votre navigateur sur [http://localhost:4200](http://localhost:4200)
2. **API Backend** : [http://localhost:8080/api](http://localhost:8080/api)
3. **Documentation API** : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (si activé)




---

## 📚 Documentation API

### URL de Base
```
http://localhost:8080/webjars/swagger-ui/index.html#/
```
---

## 🐛 Dépannage

### Problèmes Backend

#### Problème : Le port 8080 est déjà utilisé
**Solution** :
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

Ou changer le port dans `application.yml` :
```yaml
server:
  port: 8081
```

#### Problème : Échec de connexion à la base de données
**Solutions** :
- Vérifier que PostgreSQL est en cours d'exécution
- Vérifier les identifiants de la base de données dans `application-dev.yml`
- S'assurer que la base de données `tutoring_db` existe
- Tester la connexion : `psql -U tutoring_user -d tutoring_db`

#### Problème : Version de Java incompatible
**Solution** :
```bash
java -version  # Devrait afficher Java 21
```
Mettre à jour la variable d'environnement `JAVA_HOME` si nécessaire.

### Problèmes Frontend

#### Problème : Le port 4200 est déjà utilisé
**Solution** :
```bash
# Utiliser un port différent
ng serve --port 4300

# Ou tuer le processus
# Windows
netstat -ano | findstr :4200
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:4200 | xargs kill -9
```

#### Problème : npm install échoue
**Solutions** :
```bash
# Vider le cache npm
npm cache clean --force

# Supprimer node_modules et réinstaller
rm -rf node_modules package-lock.json
npm install

# Utiliser legacy peer deps si nécessaire
npm install --legacy-peer-deps
```

#### Problème : Aucun style n'apparaît
**Solutions** :
- Vider le cache du navigateur (Ctrl + F5)
- Vérifier que `styles.scss` se charge
- Vérifier que Tailwind CSS est installé : `npm list tailwindcss`
- Redémarrer le serveur de développement

#### Problème : Erreurs CORS
**Solution** :
Le backend devrait avoir CORS configuré. Sinon, ajouter au backend :
```java
@CrossOrigin(origins = "http://localhost:4200")
```

### Problèmes Courants

#### Problème : Impossible de connecter le frontend au backend
**Liste de vérification** :
1. ✅ Le backend est en cours d'exécution sur le port 8080
2. ✅ Le BASE_URL du frontend est correct
3. ✅ Aucun pare-feu ne bloque les connexions
4. ✅ CORS est correctement configuré
5. ✅ Vérifier la console du navigateur pour les erreurs

#### Problème : La connexion ne fonctionne pas
**Solutions** :
- Vérifier l'onglet réseau dans les DevTools du navigateur
- Vérifier que les identifiants sont corrects
- Vérifier les logs du backend pour les erreurs
- S'assurer que le token JWT est stocké dans localStorage

---

## 📝 Flux de Développement

### Développement Backend
1. Apporter des modifications au code Java
2. Maven rechargera automatiquement (si Spring DevTools est utilisé)
3. Ou redémarrer l'application

### Développement Frontend
1. Apporter des modifications à TypeScript/HTML/CSS
2. Hot Module Replacement (HMR) rechargera automatiquement
3. Le navigateur se met à jour automatiquement

### Modifications de la Base de Données
1. Mettre à jour `schema.sql` si nécessaire
2. Redémarrer le backend pour appliquer les changements
3. Ou utiliser des outils de migration de base de données (Flyway/Liquibase)

---

## 🔧 Configuration Avancée

### Compilation pour la Production

#### Backend
```bash
mvn clean package -Pprod
java -jar target/tutoring-0.0.1-SNAPSHOT.jar
```

#### Frontend
```bash
ng build --configuration production
```

Déployer le dossier `dist/` sur votre serveur web.

### Déploiement Docker (Optionnel)

Créer `docker-compose.yml` à la racine du projet :

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: tutoring_db
      POSTGRES_USER: tutoring_user
      POSTGRES_PASSWORD: votre_mot_de_passe
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  backend:
    build: ./backend/Tutoring Platform
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    environment:
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/tutoring_db

  frontend:
    build: ./front-end
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  postgres_data:
```

Exécuter avec :
```bash
docker-compose up -d
```

---

## 📖 Ressources Supplémentaires

- **Documentation Angular** : https://angular.dev
- **Documentation Spring Boot** : https://spring.io/projects/spring-boot
- **Documentation PostgreSQL** : https://www.postgresql.org/docs/
- **Guide de Structure du Projet** : Voir `front-end/PROJECT_STRUCTURE.md`
- **Référence Rapide** : Voir `front-end/QUICK_REFERENCE.md`

---

## 🤝 Support

Pour les problèmes ou questions :
1. Consulter la section [Dépannage](#dépannage)
2. Examiner les fichiers de documentation du projet
3. Vérifier la console du navigateur et les logs du backend
4. Vérifier que tous les prérequis sont installés

---

---

## 👥 Équipe

Développé pour la gestion de plateforme de tutorat éducatif.

---

**Dernière Mise à Jour** : 24 octobre 2025

**Bon Codage ! 🚀**
