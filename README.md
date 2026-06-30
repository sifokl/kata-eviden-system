# Authentication Gateway – Architecture & Technical Overview

> **Projet :** Authentication Gateway  
> **Architecture :** Hexagonale (Ports & Adapters) / DDD Light  
> **Stack :** Java 21 • Spring Boot 3 • Spring Security • OAuth2 Resource Server • OpenID Connect • Keycloak • Maven

---

# 1. Présentation du projet

## 1.1 Objectif

Ce projet implémente une **Gateway d'authentification** dont le rôle est de **sécuriser** et **rediriger** les requêtes HTTP vers des APIs backend.

La Gateway agit comme un **Reverse Proxy sécurisé** placé devant les services métiers.

Pour chaque requête reçue, elle est capable de :

- Valider le JWT émis par un Identity Provider (Keycloak)
- Vérifier les droits d'accès de l'utilisateur
- Extraire l'identité et les rôles
- Propager (ou régénérer) le jeton vers les APIs internes
- Rediriger la requête vers le backend approprié

### Exemple

Requête entrante :

```http
GET http://localhost:8080/members/books
Authorization: Bearer eyJhbGciOi...
```

La Gateway :

1. valide le JWT auprès de Keycloak ;
2. vérifie que l'utilisateur possède les droits suffisants ;
3. propage (ou traduit) l'identité ;
4. redirige la requête vers :

```http
GET http://localhost:8081/api/library/members/books
```

Le backend n'est jamais directement exposé aux clients.

---

## 1.2 Stack technique

Le projet utilise les technologies suivantes :

| Technologie | Utilisation |
|-------------|-------------|
| Java 21 | Langage |
| Spring Boot 3 | Framework |
| Spring Security | Authentification / Autorisation |
| OAuth2 Resource Server | Validation JWT |
| OpenID Connect | Authentification |
| Keycloak | Identity Provider |
| RestClient | Reverse Proxy HTTP |
| Maven | Build |
| Docker | Exécution de l'environnement |
| JUnit 5 | Tests unitaires |
| Mockito | Mocking |
| MockMvc | Tests REST |
| WireMock | Backend simulé |
| Testcontainers | Tests d'intégration |

---

## 1.3 Périmètre de l'évaluation technique

Le périmètre de l'exercice porte **uniquement sur la Gateway d'authentification**, développée selon une **architecture hexagonale stricte (Ports & Adapters)**.

L'objectif est d'évaluer :

- la qualité de l'architecture ;
- la séparation des responsabilités ;
- l'implémentation de Spring Security ;
- la gestion des JWT ;
- la conception orientée domaine ;
- la qualité des tests.

Les autres composants servent uniquement de support à la démonstration.

### Fake Books API

Cette API représente un backend métier fictif.

Elle permet de simuler une ressource sécurisée vers laquelle la Gateway redirige les requêtes.

Elle ne constitue **pas** le sujet principal de l'évaluation.

### Keycloak

Keycloak est utilisé comme **Identity Provider (IdP)**.

Il fournit un environnement OAuth2 / OpenID Connect complet permettant de :

- authentifier les utilisateurs ;
- générer des JWT réels ;
- gérer les rôles ;
- publier les clés publiques (JWKS).

Son objectif est uniquement de fournir un environnement IAM réaliste et standardisé.

---

## 1.4 Les composants de l'écosystème

| Composant | Rôle | Port |
|-----------|------|------|
| Authentication Gateway | Validation JWT, autorisation, Reverse Proxy | **8080** |
| Fake Books API | Backend REST protégé | **8081** |
| Keycloak | Identity Provider (OIDC) | **8082** |

---

### Vue d'ensemble

```text
                              Authentication Platform

                 +--------------------------------------------+
                 |              Keycloak (8082)               |
                 |       Identity Provider (OIDC / JWT)       |
                 +----------------------+---------------------+
                                        ^
                                        |
                              Validation du JWT
                                        |
                                        |
+-------------+               JWT Externe                +----------------------+
|   Client    | ---------------------------------------> | Authentication       |
| Postman/Web |                                         | Gateway (8080)       |
+-------------+                                         +----------+-----------+
                                                                   |
                                                                   |
                                                       JWT Relay / Internal JWT
                                                                   |
                                                                   |
                                                                   v
                                                     +---------------------------+
                                                     | Fake Books API (8081)     |
                                                     | Protected Resources       |
                                                     +---------------------------+
```

---

## 1.5 Démarrage rapide

L'environnement complet peut être démarré en une seule commande grâce aux scripts fournis.

### Windows

```text
/docker/run.bat
```

### Linux / macOS

```text
/docker/run.sh
```

Ces scripts démarrent automatiquement :

- Keycloak
- Authentication Gateway
- Fake Books API

Aucune configuration manuelle n'est nécessaire.

---

## Validation de l'environnement

Une fois les applications démarrées, plusieurs moyens sont disponibles pour vérifier le bon fonctionnement du projet.

### Scripts Curl

Des scripts Curl prêts à l'emploi sont disponibles dans :

```text
/curl
```

Ils permettent de tester rapidement :

- les endpoints publics ;
- les endpoints protégés ;
- les différents rôles ;
- les erreurs d'authentification.

---

### Collection Postman

Une collection Postman complète est fournie dans :

```text
/postman
```

Elle contient l'ensemble des scénarios de démonstration.

Les tests Postman comportent des assertions lisibles permettant notamment de vérifier :

- Status HTTP 200
- Status HTTP 401
- Status HTTP 403
- Présence des données attendues
- Gestion des rôles
- Validation des erreurs

Cette collection permet de démontrer rapidement le fonctionnement de la Gateway sans écrire une seule ligne de code.

---

# 2. Guide des URLs

## Authentication Gateway

### Endpoints publics

| Méthode | URL | Description |
|---------|-----|-------------|
| GET | /public/books | Ressource publique |

---

### Endpoints protégés

| Méthode | URL | Rôle |
|---------|-----|------|
| GET | /members/books | USER / ADMIN |
| GET | /admin/books | ADMIN |
| GET | /api/me | Utilisateur courant |

---

### Documentation OpenAPI

| Ressource | URL |
|------------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI | http://localhost:8080/v3/api-docs |

---

### Spring Boot Actuator

| Endpoint |
|-----------|
| /actuator |
| /actuator/health |
| /actuator/info |

---

## Fake Books API

| Méthode | URL |
|---------|-----|
| GET | /api/library/public/books |
| GET | /api/library/members/books |
| GET | /api/library/admin/books |

Documentation :

```text
http://localhost:8081/swagger-ui.html
```

---

## Keycloak

### Console

```text
http://localhost:8082
```

### Realm

```text
eviden-kata
```

### OpenID Configuration

```text
http://localhost:8082/realms/eviden-kata/.well-known/openid-configuration
```

### JWKS

```text
http://localhost:8082/realms/eviden-kata/protocol/openid-connect/certs
```

### Token Endpoint

```text
http://localhost:8082/realms/eviden-kata/protocol/openid-connect/token
```

---

# 3. Architecture & Structure du projet

## 3.1 Structure de la Gateway

La Gateway est développée selon une **architecture hexagonale (Ports & Adapters)**.

```text
gateway

├── domain
│     ├── model
│     ├── exception
│     └── port
│
├── application
│     ├── service
│     └── dto
│
├── infrastructure
│     ├── security
│     ├── config
│     ├── adapter
│     └── proxy
│
├── api
│     ├── controller
│     └── handler
│
└── launcher
```

Le **Domain** est volontairement **indépendant de Spring**.

Il est constitué uniquement de Java standard afin de :

- préserver l'indépendance du métier ;
- faciliter les tests unitaires ;
- limiter le couplage avec les frameworks ;
- permettre une évolution de l'infrastructure sans impacter le domaine.

---

## 3.2 Composants d'accompagnement

### Fake Books API

La Fake Books API est un backend volontairement simple.

Son unique objectif est de représenter une ressource métier protégée derrière la Gateway.

Elle permet de démontrer :

- le Reverse Proxy ;
- la propagation du JWT ;
- les règles d'autorisation.

---

### Configuration Keycloak

Le Realm contient deux utilisateurs de démonstration.

| Utilisateur | Mot de passe | Rôles |
|-------------|--------------|--------|
| user | user | USER |
| admin | admin | USER, ADMIN |

Les JWT contiennent notamment les claims suivants :

- preferred_username
- email
- given_name
- family_name
- roles
- iss
- aud
- sub
- exp

---

# 4. Fonctionnalités avancées

## 4.1 Gateway hautement configurable

La Gateway ne contient **aucune règle métier codée en dur** concernant les routes ou les autorisations.

L'ensemble de la configuration est externalisé dans le fichier :

```text
application.yml
```

Exemple :

```yaml
gateway:

  security:

    public-endpoints:
      - /public/**

    route-authorizations:

      - pattern: /members/**
        access: HAS_ANY_ROLE
        roles:
          - USER
          - ADMIN

      - pattern: /admin/**
        access: HAS_ROLE
        roles:
          - ADMIN
```

Ajouter une nouvelle route sécurisée consiste uniquement à modifier la configuration YAML.

Aucune modification du code Java n'est nécessaire.

---

## 4.2 Stratégie de propagation des jetons

La Gateway implémente deux stratégies de propagation.

### Relay

Le JWT reçu du client est simplement relayé au backend.

```text
Client JWT
      │
      ▼
Gateway
      │
      ▼
Backend
```

---

### Internal Token

La Gateway peut également générer un JWT interne contenant uniquement les informations nécessaires au backend.

```text
External JWT

↓

Gateway

↓

Internal JWT

↓

Backend
```

Cette stratégie répond à un besoin fréquent dans les architectures d'entreprise où les APIs internes ne doivent pas recevoir directement les jetons issus de l'Identity Provider externe.

---

## 4.3 Stratégie de tests

Le projet met en œuvre plusieurs niveaux de tests complémentaires.

| Niveau | Objectif |
|----------|----------|
| Tests unitaires | Validation du domaine métier |
| Tests applicatifs | Validation des cas d'usage |
| Tests infrastructure | JWT, Spring Security, Adapters |
| Tests API | Contrats REST avec MockMvc |
| Tests d'intégration | SpringBootTest + WireMock |
| Tests Testcontainers | Validation avec Keycloak réel |

Cette approche permet de couvrir l'ensemble de la chaîne applicative, depuis le domaine jusqu'à l'intégration avec un véritable Identity Provider.