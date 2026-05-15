# Customer Support Hub

Small Spring Boot API for a support ticket system.

## What it does

Three kinds of users:

- **ADMIN** — sees everything.
- **AGENT** — owns a list of customers.
- **CUSTOMER** — opens support tickets.

Login returns a JWT. Send it on every other request as `Authorization: Bearer <token>`.

## Tech stack

- Java 21, Spring Boot 3.3, Maven
- Spring Web, Spring Data JPA, Spring Security (OAuth2 Resource Server + JWT)
- MySQL 8
- Lombok, JUnit 5, Mockito

## Data model

Two tables:

- `users` — one row per person. Column `role` says ADMIN / AGENT / CUSTOMER. Column `agent_id` points to the agent who owns this customer (only set for customers).
- `tickets` — one row per ticket. Column `user_id` points to the customer who opened it.

## Run locally

Needs MySQL on `localhost:3306`, user `root`, password `root`.

```bash
./mvnw spring-boot:run
```

Database `supporthub` is created automatically. Two seed users are inserted on first start:

| Username | Password   | Role  |
|----------|------------|-------|
| `admin`  | `password` | ADMIN |
| `agent`  | `password` | AGENT |

## Run with Docker

```bash
docker-compose up --build
```

Starts MySQL + the app. API on `http://localhost:8080`.

## Endpoints

All paths return JSON. All except `/auth/login` need a Bearer token.

| # | Method | Path             | Who can call         | What it does |
|---|--------|------------------|----------------------|---|
| 1 | POST   | `/auth/login`    | anyone               | Send `{username, password}`. Get back `{accessToken, tokenType}`. |
| 2 | POST   | `/customers`     | AGENT, ADMIN         | Create a new customer. If AGENT calls, the new customer is linked to that agent. If ADMIN calls, must include `agentId` in the body. |
| 3 | GET    | `/customers`     | AGENT, ADMIN         | AGENT sees only their own customers. ADMIN sees every customer. |
| 4 | GET    | `/customers/me`  | CUSTOMER             | Returns the calling customer's own profile. |
| 5 | PUT    | `/customers/me`  | CUSTOMER             | Update own `email` and `fullName`. Username and role can't change. |
| 6 | PUT    | `/agents/me`     | AGENT                | Same idea — agent updates own `email` and `fullName`. |
| 7 | POST   | `/tickets`       | CUSTOMER             | Create a ticket. `user_id` is set to the calling customer. Status starts as `OPEN`. |
| 8 | GET    | `/tickets`       | CUSTOMER, AGENT, ADMIN | CUSTOMER sees own tickets. AGENT sees tickets opened by their customers. ADMIN sees all. |

## How it works

1. Client calls `POST /auth/login` with username + password.
2. Server checks the BCrypt-hashed password. If valid, signs a JWT (HMAC-SHA256) containing the username, user id, and a `roles` claim like `["ROLE_AGENT"]`.
3. Client sends the JWT on every other request.
4. Spring Security:
   - Verifies the JWT signature and expiry.
   - Reads the `roles` claim and attaches it as the caller's authorities.
5. Each controller method has a `@PreAuthorize` rule that checks the caller's role.
6. The service layer adds ownership filtering — e.g. an AGENT listing tickets only gets tickets where `ticket.user.agent.id = caller.id`. This stops one agent from seeing another agent's data.

## Quick example

```bash
# 1. Login
TOKEN=$(curl -s -X POST localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"agent","password":"password"}' | jq -r .accessToken)

# 2. Create a customer
curl -X POST localhost:8080/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password","email":"alice@x.com","fullName":"Alice"}'

# 3. Alice logs in and opens a ticket
CTOKEN=$(curl -s -X POST localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password"}' | jq -r .accessToken)

curl -X POST localhost:8080/tickets \
  -H "Authorization: Bearer $CTOKEN" \
  -H "Content-Type: application/json" \
  -d '{"subject":"Printer broken","description":"Smoke."}'

# 4. Agent lists tickets — sees alice's
curl -H "Authorization: Bearer $TOKEN" localhost:8080/tickets
```

## Error responses

All errors return JSON: `{status, message, timestamp}`.

| Status | When |
|--------|------|
| 400    | Validation failed (missing field, bad email, etc.) |
| 401    | No token, bad token, or wrong username/password |
| 403    | Role not allowed for this endpoint |
| 404    | Resource not found |
| 409    | Conflict (e.g. duplicate username) |
| 500    | Unexpected server error |

## Tests

```bash
./mvnw test
```

Unit tests for `UserService` and `TicketService` using Mockito. No database, no web layer.

## Notes

- Schema is created automatically by Hibernate (`ddl-auto=update`). Fine for this task; use Flyway for production.
- Seed users are inserted by a `CommandLineRunner` so the BCrypt hash is generated at startup (always correct).
- No refresh tokens — access token is valid 60 minutes, then re-login.
