# CyberShield Spring Boot API

Java 21 · Spring Boot 3.5.6 · Spring Data JPA · BCrypt · H2 (local) / PostgreSQL (deployment)

## Local setup

Run the backend in one terminal:

```bash
cd backend
mvn spring-boot:run
```

In another terminal, run the React app from the repository root:

```bash
npm ci
npm run dev
```

The frontend proxies `/api` to `http://localhost:8080` in development.

The default H2 database persists under `backend/data/`. This directory is ignored by Git. **Do not use the default configuration as a public production deployment.**

## API endpoints

- `POST /api/auth/signup` — `{displayName,email,password}`, 12-character minimum password.
- `POST /api/auth/login` — `{email,password}`.
- `GET /api/auth/me` — requires `Authorization: Bearer <token>`.
- `POST /api/auth/logout` — revokes the session token.
- `GET /api/progress` — returns `{progress,updatedAt}` for the current account.
- `PUT /api/progress` — accepts `{progress:{completedLessons:[],...}}`.
- `GET /api/health` — basic health response.

Passwords are BCrypt-hashed. Random session tokens are stored hashed in the database and expire after seven days. Tokens are held in browser sessionStorage, so closing the browser may require signing in again. Browser localStorage retains a guest cache. **Do not store real passwords or private data in educational exercises.**

## Deployment configuration

Set `DATABASE_URL` to a JDBC PostgreSQL URL, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `FRONTEND_ORIGIN` to the exact HTTPS frontend origin. Use a managed PostgreSQL database with backups and HTTPS termination. `spring.jpa.hibernate.ddl-auto=update` is a prototype convenience; use versioned migrations before production.

Set the frontend build variable `VITE_API_URL=https://api.example.org/api` when hosting separately.

**Production work remaining:** login rate limiting and abuse controls, email verification/password reset, security monitoring, versioned database migrations, account deletion/export policies, CSRF/cookie strategy if moving away from bearer headers, and robust conflict resolution for simultaneous devices. The current backend is a functional academic prototype, not a production-hardened identity service.
