# Synkro — API

Backend REST de l'application Synkro, développé dans le cadre d'un test technique.

## Présentation

Synkro est une application de gestion de projet. Ce dépôt contient uniquement l'API. Le client web est disponible dans un dépôt séparé et dépend de cette API pour fonctionner.

## Stack technique

- **Java 25**
- **Spring Boot 4.0.6**
  - Spring Web MVC
  - Spring Security
  - Spring Data JPA / Hibernate
  - Spring Validation
  - Spring Actuator
- **MariaDB** — base de données relationnelle
- **jjwt** — gestion des tokens JWT (access token + refresh token)
- **Maven** — gestion des dépendances et build

## Prérequis

- Java 25
- Une instance **MariaDB** accessible (locale ou distante)

## Installation

```bash
git clone https://github.com/ton-compte/synkro.git
cd synkro
```

## Configuration

Copie le fichier `example.env` et remplis les valeurs :

```bash
cp example.env local.env
```

Contenu à remplir dans `local.env` :

```env
# Connexion à la base de données
# Utiliser 127.0.0.1 plutôt que localhost si tu as des problèmes de connexion
DATABASE_HOST=127.0.0.1
DATABASE_NAME=synkro

# Identifiants MariaDB
DATABASE_USERNAME=
DATABASE_PASSWORD=

# Clé secrète JWT — chaîne aléatoire d'au moins 32 caractères
SYNKRO_SECRET=

# Durée de validité de l'access token (en millisecondes, ex: 600000 = 10 min)
SYNKRO_JWT_EXPIRATION=600000

# Durée de validité du refresh token (en jours, ex: 7)
SYNKRO_REFRESH_TOKEN_EXPIRATION_DAYS=7
```

La base de données est créée automatiquement au premier démarrage si elle n'existe pas (`createDatabaseIfNotExist=true`).

## Lancer le projet

Les variables d'environnement doivent être chargées avant de lancer l'application. Selon ton OS :

```bash
# Linux / macOS
export $(cat local.env | grep -v '#' | xargs) && ./mvnw spring-boot:run

# Ou directement si votre IDE charge le fichier .env
./mvnw spring-boot:run
```

L'API sera accessible sur `http://localhost:8080/api`.

## Endpoints principaux

| Méthode | Route | Description |
|---|---|---|
| POST | `/api/auth/register` | Créer un compte |
| POST | `/api/auth/login` | Se connecter |
| POST | `/api/auth/refresh` | Rafraîchir le token |
| POST | `/api/auth/logout` | Se déconnecter |
| GET | `/api/users/me` | Profil de l'utilisateur connecté |
| PATCH | `/api/users/me` | Modifier son profil (pseudo, email) |
| PATCH | `/api/users/me/password` | Changer son mot de passe |
| DELETE | `/api/users/me` | Supprimer son compte |
| GET | `/api/projects` | Lister ses projets |
| POST | `/api/projects` | Créer un projet |
| GET | `/api/projects/{id}` | Détail d'un projet |
| PUT | `/api/projects/{id}` | Modifier un projet |
| DELETE | `/api/projects/{id}` | Supprimer un projet |
| PATCH | `/api/projects/{id}/transfer` | Transférer la propriété |
| POST | `/api/projects/{id}/avatar` | Upload de l'avatar du projet |
| GET | `/api/projects/{id}/members` | Lister les membres |
| POST | `/api/projects/{id}/members` | Ajouter un membre |
| PATCH | `/api/projects/{id}/members` | Modifier le rôle d'un membre |
| DELETE | `/api/projects/{id}/members/{userId}` | Retirer un membre |
| GET | `/api/projects/{id}/tasks` | Lister les tâches d'un projet |
| POST | `/api/projects/{id}/tasks` | Créer une tâche |
| PUT | `/api/tasks/{id}` | Modifier une tâche |
| PATCH | `/api/tasks/{id}/status` | Changer le statut d'une tâche |
| DELETE | `/api/tasks/{id}` | Supprimer une tâche |
| POST | `/api/tasks/{id}/assign` | Affecter des participants à une tâche |
| GET | `/api/admin/users` | Lister tous les utilisateurs (admin) |
| PATCH | `/api/admin/users/{id}/role` | Modifier le rôle d'un utilisateur (admin) |
| DELETE | `/api/admin/users/{id}` | Supprimer un utilisateur (admin) |

## Structure du projet

```
src/main/java/fr/enzogiardinelli/synkro/
├── controllers/   # Endpoints REST
├── services/      # Logique métier
├── repositories/  # Accès base de données (Spring Data JPA)
├── entities/      # Entités JPA (User, Project, Task…)
├── dtos/          # Objets de requête et de réponse
├── security/      # Configuration JWT et Spring Security
├── exceptions/    # Gestion globale des erreurs
└── config/        # Configuration CORS et autres
```
