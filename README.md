# agenda-rdv

Application de prise de rendez-vous : API Spring Boot sécurisée par JWT, front Angular (en cours).

![CI](https://github.com/momo97ndiaye/agenda-rdv/actions/workflows/ci.yml/badge.svg)

## Back-end

- Inscription et connexion, mots de passe hachés (BCrypt), jeton JWT signé HS256.
- CRUD des rendez-vous : chaque utilisateur ne voit et ne modifie que les siens.
- Refus des créneaux qui se chevauchent (409) et des créneaux dont la fin précède le début (400).
- PostgreSQL, migrations Flyway, erreurs au format Problem Details (RFC 7807).

Java 17 · Spring Boot 3.3 · Spring Security (OAuth2 Resource Server) · Spring Data JPA · Flyway · JUnit 5

| Méthode | Route | |
|---|---|---|
| POST | `/api/auth/register` | inscription |
| POST | `/api/auth/login` | renvoie `{ "token": "..." }` |
| GET | `/api/appointments` | mes rendez-vous |
| POST | `/api/appointments` | créer |
| PUT | `/api/appointments/{id}` | modifier |
| DELETE | `/api/appointments/{id}` | supprimer |

## Lancer en local

```bash
docker compose up -d
cd backend
export JWT_SECRET=une-cle-d-au-moins-32-caracteres
mvn spring-boot:run
```

## Tests

```bash
cd backend && mvn verify
```
