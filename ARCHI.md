# ARCHITECTURE.md

> **Projet :** Authentication Gateway  
> **Architecture :** Hexagonale (Ports & Adapters) / DDD Light  
> **Objectif :** Présenter les choix de conception et l'architecture interne 

---

# 1. Vision d'ensemble

L'objectif principal de cette Gateway est de jouer le rôle de **point d'entrée sécurisé** devant une ou plusieurs APIs métier.

Elle remplit deux responsabilités principales :

- Authentifier et autoriser les requêtes entrantes.
- Rediriger les requêtes autorisées vers les APIs backend.

La Gateway constitue ainsi une couche de sécurité indépendante des applications métiers.

---

# 2. Pourquoi une Architecture Hexagonale ?

Ce projet adopte une **Architecture Hexagonale (Ports & Adapters)** afin de respecter plusieurs objectifs :

- séparation stricte entre métier et technique ;
- indépendance vis-à-vis de Spring Boot ;
- testabilité maximale ;
- faible couplage ;


L'objectif est que le métier puisse évoluer indépendamment :

- du framework,
- de la sécurité,
- du protocole HTTP,
- de Keycloak,
- ou de toute autre technologie externe.

---

# 3. Architecture Globale

```
                        Client

                           │

                     Bearer JWT

                           │

                           ▼

                Authentication Gateway

          ┌───────────────────────────────┐
          │                               │
          │  Spring Security              │
          │  JWT Validation               │
          │  Authorization                │
          │  Reverse Proxy                │
          │                               │
          └──────────────┬────────────────┘
                         │
                         ▼
                  Business Use Cases
                         │
                         ▼
                  Infrastructure Layer
                         │
                         ▼
                   Fake Books API
```

---

# 4. Architecture Hexagonale

```
                    REST API

                        │

                 Controllers

                        │

────────────────────────────────────────────

              Application Layer

────────────────────────────────────────────

                  Domain

────────────────────────────────────────────

            Ports (Interfaces)

────────────────────────────────────────────

            Infrastructure

      Spring Security
      RestClient
      Keycloak
```

Le **Domain** ne connaît jamais Spring.

L'Infrastructure dépend du Domain, mais jamais l'inverse.

Les dépendances sont toujours dirigées vers le centre de l'hexagone.

---

# 5. Organisation des modules

```
gateway

├── domain
│
├── application
│
├── infrastructure
│
├── api
│
└── launcher
```

## gateway-domain

Responsabilités :

- modèles métier
- objets valeur
- ports
- exceptions
- logique métier

Aucune dépendance Spring.

---

## gateway-application

Responsabilités :

- orchestration
- use cases
- services applicatifs
- DTO

Cette couche fait le lien entre les Controllers et le Domain.

Elle ne contient aucune logique liée à HTTP ou Spring Security.

---

## gateway-api

Responsabilités :

- Controllers REST
- mapping HTTP
- codes de retour
- gestion des erreurs

Aucune logique métier.

---

## gateway-infrastructure

Responsabilités :

- Spring Security
- RestClient
- Keycloak
- JWT
- Configuration
- Adapters

Toutes les dépendances techniques sont concentrées ici.

---

## gateway-launcher

Responsabilités :

- démarrage Spring Boot
- configuration des Beans
- bootstrap de l'application

---

# 6. Les Ports

Les Ports représentent les interfaces exposées par le Domain.

Ils définissent les besoins métier sans connaître leur implémentation.

## CurrentPrincipalProviderPort

Responsabilité :

Fournir l'utilisateur actuellement authentifié.

Implémentation :

```
SpringSecurityCurrentPrincipalProvider
```

---

## BackendApiPort

Responsabilité :

Communiquer avec les APIs backend.

Implémentation :

```
RestClientBackendApiAdapter
```

---

## OutgoingTokenProviderPort

Responsabilité :

Déterminer quel JWT transmettre au backend.

Implémentations :

```
OriginalTokenRelayProvider

InternalJwtOutgoingTokenProvider
```

---

# 7. Les Adapters

Les Adapters sont les implémentations concrètes des Ports.

Ils permettent de connecter le Domain au monde extérieur.

## SpringSecurityCurrentPrincipalProvider

Transforme le principal Spring Security en objet métier.

```
SecurityContext

↓

Authentication

↓

Jwt

↓

JwtPrincipal
```

---

## OriginalTokenRelayProvider

Réutilise le JWT reçu du client.

```
Client JWT

↓

Gateway

↓

Backend
```

---

## InternalJwtOutgoingTokenProvider

Prévu pour générer un JWT interne.

```
Client JWT

↓

Gateway

↓

Internal JWT

↓

Backend
```

Cette implémentation est volontairement laissée ouverte afin de démontrer l'extensibilité de la Gateway.

---

## RestClientBackendApiAdapter

Implémente le Reverse Proxy HTTP.

Il encapsule complètement RestClient.

Le Domain ne connaît donc jamais Spring RestClient.

---

# 8. Flux d'authentification

```
Client

↓

JWT

↓

SecurityFilterChain

↓

JwtDecoder

↓

JwtAuthenticationConverter

↓

JwtGrantedAuthoritiesConverter

↓

GrantedAuthorities

↓

SpringSecurityCurrentPrincipalProvider

↓

CurrentPrincipalService

↓

Controller
```

Chaque étape possède une responsabilité unique.

Cette séparation facilite :

- les tests ;
- la maintenance ;
- l'évolution de la sécurité.

---

# 9. Flux Reverse Proxy

```
Client

↓

Controller

↓

ProxyBackendService

↓

OutgoingTokenProvider

↓

BackendApiPort

↓

RestClientBackendApiAdapter

↓

Fake Books API
```

Le Controller ne connaît jamais RestClient.

Le Service ne connaît jamais HTTP.

Le Domain ne connaît jamais Spring.

---

# 10. Gestion de la sécurité

La Gateway repose sur plusieurs mécanismes complémentaires.

## Authentification

Validation du JWT :

- signature
- expiration
- issuer
- audience

---

## Autorisation

Les rôles sont convertis en :

```
ROLE_USER

ROLE_ADMIN
```

Spring Security applique ensuite automatiquement les règles d'accès.

---

## Configuration dynamique

Toutes les règles de sécurité sont externalisées dans :

```
application.yml
```

Les routes publiques.

Les routes protégées.

Les rôles.

Le mode Relay / Internal.

Tout est configurable.

---

# 11. Stratégie de tests

Le projet adopte une approche de tests en plusieurs niveaux.

| Niveau | Objectif |
|---------|----------|
| Domain | Validation du métier |
| Application | Validation des Use Cases |
| Infrastructure | JWT, Security, Adapters |
| API | Controllers REST |
| Security | Authentification / Autorisation |
| Integration | SpringBoot + WireMock |
| Testcontainers | Validation avec un vrai Keycloak |

Chaque couche est testée indépendamment afin de limiter les régressions.

---


# 13. Axes d'amélioration

Les évolutions suivantes pourraient être intégrées dans une version production :

- génération complète des Internal JWT
- support Multi-Realm
- support Multi-IdP
- Token Exchange OAuth2
- TLS/HTTPS entre Gateway et Backend
- Rate Limiting

---

# Conclusion

Cette Gateway met en œuvre une architecture volontairement modulaire et découplée afin d'illustrer les bonnes pratiques de conception d'une application Java moderne.

Le projet démontre :

- une architecture hexagonale stricte ;
- une séparation claire des responsabilités ;
- une intégration avec Keycloak (OAuth2 / OpenID Connect) ;
- une configuration dynamique des règles de sécurité ;
- une stratégie de tests couvrant l'ensemble de la chaîne applicative.

Cette conception facilite la maintenance, les tests et les évolutions futures tout en restant proche des architectures rencontrées dans des environnements d'entreprise.