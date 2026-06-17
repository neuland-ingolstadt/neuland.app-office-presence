# Office Presence

Quarkus backend that tracks which Neuland members are currently in the office. Members check in via the app; entries expire after a configurable timeout (default: 5 hours). Presence is stored in memory and expired entries are purged every 5 minutes.

## Prerequisites

- Java 21

## Setup

Copy the example environment file and set the Authentik client ID:

```bash
cp .env.example .env
```

Quarkus reads `.env` automatically in dev and test mode.

## Configuration

| Variable | Required | Description |
|----------|----------|-------------|
| `NEULAND_AUTHENTIK_CLIENT_ID` | Yes | Same as `EXPO_PUBLIC_NEULAND_AUTHENTIK_CLIENT_ID` in the app (JWT `aud`) |
| `NEULAND_AUTHENTIK_ISSUER` | No | JWT `iss` claim (default: `https://auth.neuland.ing/application/o/next/`) |
| `OFFICE_PRESENCE_TIMEOUT_HOURS` | No | Auto check-out timeout in hours (default: `5`) |

JWKS URL is derived from the issuer automatically (`<issuer>jwks/`).

## Run

```bash
./mvnw quarkus:dev
```

Dev mode listens on `http://0.0.0.0:8080`.

## Test

```bash
./mvnw test
```

## API

All endpoints require `Authorization: Bearer <idToken>` with the `mitglieder` group.

| Method | Path | Response |
|--------|------|----------|
| `GET` | `/api/v1/office/presence` | `{ count, registered, expiresAt? }` |
| `POST` | `/api/v1/office/presence` | `{ registered: true, expiresAt }` |
| `DELETE` | `/api/v1/office/presence` | `{ registered: false, expiresAt: null }` |
