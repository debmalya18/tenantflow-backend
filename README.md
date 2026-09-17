![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-brightgreen) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-blue)

# TenantFlow

**A multi-tenant task management SaaS backend** — built with Java, Spring Boot, and PostgreSQL, featuring JWT authentication, tenant-isolated data access, role-based permissions, and real-time updates over WebSockets.

## Why this exists

Most task-management demos stop at basic CRUD. TenantFlow goes further: it's built the way a real multi-customer SaaS product is — one shared backend serving many independent teams, where each team's data is structurally isolated from every other team's, not just filtered on the frontend.

## How it works

1. A user signs up and logs in, receiving a signed JWT.
2. A custom JwtAuthFilter runs on every request, verifies the token, and resolves the authenticated user before any Controller logic executes.
3. Every data-access query is scoped to the authenticated user's own team, never to a client-supplied ID, so cross-tenant access isn't just disallowed, it's structurally impossible from the request layer.
4. Team leaders (auto-assigned on team creation, promotable afterward) get exclusive access to member management endpoints, enforced server-side on every call.
5. Task changes are broadcast in real time over STOMP/WebSocket to a per-team topic, so connected clients see updates instantly without polling.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1 (Web, Security, Data JPA, WebSocket) |
| Database | PostgreSQL (hosted on Neon) |
| Auth | JWT (jjwt), BCrypt password hashing |
| Real-time | STOMP over WebSocket |
| Build | Maven |

## Core features

- JWT authentication — stateless login/signup, tokens verified on every request via a custom security filter
- Multi-tenancy — every task/user record is team-scoped; queries can't cross tenant boundaries
- Role-based access control — LEADER vs MEMBER, enforced per-endpoint, tested against both positive and negative cases
- Full task CRUD — create, list, update, delete, each verifying team ownership before mutation
- Real-time notifications — task changes pushed live to all connected team members over WebSocket

## API overview

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | /api/auth/signup | Register a new user | Public |
| POST | /api/auth/login | Authenticate, receive JWT | Public |
| POST | /api/teams | Create a team (creator becomes LEADER) | Required |
| POST | /api/teams/members/{userId} | Add a member | LEADER only |
| DELETE | /api/teams/members/{userId} | Remove a member | LEADER only |
| POST | /api/teams/members/{userId}/promote | Promote to LEADER | LEADER only |
| POST | /api/tasks | Create a task | Required |
| GET | /api/tasks | List your team's tasks | Required |
| PUT | /api/tasks/{id} | Update a task | Required, same team |
| DELETE | /api/tasks/{id} | Delete a task | Required, same team |
| WS | /ws, subscribe /topic/team/{id} | Live task update stream | - |

## Running locally

git clone https://github.com/debmalya18/tenantflow-backend.git
cd tenantflow-backend
cp src/main/resources/application.properties.example src/main/resources/application.properties
./mvnw spring-boot:run

## What I would improve next

- Move the JWT signing secret to an environment variable instead of a hardcoded value
- Add pagination on the task-listing endpoint
- Refresh tokens instead of a flat 24-hour expiry
- Integration tests (currently verified manually via PowerShell and Node scripts during development)
- Deploy the backend alongside the Neon-hosted database

## Security highlights

- Passwords are never stored or returned in plaintext — hashed with BCrypt on signup, verified (not decrypted) on login
- API responses use dedicated response DTOs (e.g. TaskResponse) instead of raw entities, preventing internal fields like password hashes from leaking through JSON serialization — caught and fixed during development after inspecting an actual response payload
- Every mutating endpoint re-verifies resource ownership server-side (task belongs to caller's team, target user belongs to caller's team) rather than trusting client-supplied IDs
- Role checks (LEADER-only actions) are enforced in the service/controller layer on every request, not just hidden in a UI

## Testing approach

No frontend exists yet, so every endpoint was verified manually end-to-end using PowerShell (Invoke-RestMethod) and a small Node.js WebSocket client, including negative-path tests — e.g. confirming a MEMBER genuinely receives a 403 when attempting a LEADER-only action, not just that a LEADER succeeds.

