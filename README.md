# agenda-rdv

Application de prise de rendez-vous : API Spring Boot sécurisée par JWT et front Angular.

![CI](https://github.com/momo97ndiaye/agenda-rdv/actions/workflows/ci.yml/badge.svg)

## Back-end

- Inscription et connexion, mots de passe hachés (BCrypt), jeton JWT signé HS256.
- CRUD des rendez-vous : chaque utilisateur ne voit et ne modifie que les siens.
- Refus des créneaux qui se chevauchent (409) et des créneaux dont la fin précède le début (400).
- PostgreSQL, migrations Flyway, erreurs au format Problem Details (RFC 7807).
- Tests d'intégration sur un vrai PostgreSQL (Testcontainers), image Docker construite en CI.

Java 17 · Spring Boot 3.3 · Spring Security (OAuth2 Resource Server) · Spring Data JPA · Flyway · JUnit 5 · Testcontainers · Docker

| Méthode | Route | |
|---|---|---|
| POST | `/api/auth/register` | inscription |
| POST | `/api/auth/login` | renvoie `{ "token": "..." }` |
| GET | `/api/appointments` | mes rendez-vous |
| POST | `/api/appointments` | créer |
| PUT | `/api/appointments/{id}` | modifier |
| DELETE | `/api/appointments/{id}` | supprimer |

## Front-end

- Angular 21 : composants standalone, signals, formulaires réactifs.
- Connexion et inscription, jeton ajouté par un intercepteur HTTP, retour à la connexion si le jeton expire.
- Liste, création, modification et suppression des rendez-vous ; messages d'erreur de l'API affichés (créneau déjà pris, dates incohérentes).
- Tests unitaires avec Vitest.

## Lancer en local

Tout avec Docker :

```bash
export JWT_SECRET=une-cle-d-au-moins-32-caracteres
docker compose up -d --build        # PostgreSQL + API sur http://localhost:8080
cd frontend && npm install && npm start
```

Ou en développement :

```bash
docker compose up -d postgres

cd backend
export JWT_SECRET=une-cle-d-au-moins-32-caracteres
mvn spring-boot:run

cd ../frontend
npm install
npm start          # http://localhost:4200, les appels /api sont redirigés vers le back-end
```

## Tests

```bash
cd backend && mvn verify          # Docker requis (Testcontainers)
cd frontend && npx ng test --watch=false
```
