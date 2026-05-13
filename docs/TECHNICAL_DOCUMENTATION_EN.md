# Technical Documentation

## 1. Project Purpose

The Scientific Article Submission System is a web application for managing the lifecycle of academic article submissions. It covers author submission, metadata management, reviewer assignment, peer review, editorial decisions, publication, notifications, reporting and operational monitoring.

The project was designed as a complete JEE university project and as a portfolio-ready open-source repository. For that reason, the implementation includes not only business screens, but also Docker deployment, tests, generated screenshots, bilingual documentation and monitoring.

## 2. Technology Stack

| Layer | Technology | Responsibility |
| --- | --- | --- |
| Frontend | React, TypeScript, Vite, React Router, i18next | User interface, routing, bilingual labels, form handling |
| Backend | Java, Spring Boot, Spring Security, Spring Data JPA | REST API, business rules, authorization, persistence |
| Database | PostgreSQL, Flyway | Durable storage and schema migrations |
| Files | Docker volume | Uploaded article files |
| Reports | CSV generation, OpenPDF | Operational exports |
| Monitoring | Actuator, Micrometer, Prometheus, Grafana | Health checks and metrics |
| Infrastructure | Docker Compose | Local deployment of the full system |
| Tests | JUnit, Spring Boot Test, frontend build/lint | Verification of critical workflows |

## 3. Runtime Architecture

The application follows a layered architecture:

1. The browser loads the React frontend from the frontend container.
2. The frontend sends authenticated REST requests to the Spring Boot backend.
3. The backend checks security rules, validates business transitions and uses repositories to access PostgreSQL.
4. Uploaded files are stored in a Docker volume, while file metadata is stored in PostgreSQL.
5. Actuator exposes health and Prometheus metrics.
6. Prometheus scrapes the backend and Grafana visualizes technical and domain metrics.

This separation keeps the frontend focused on interaction and the backend responsible for rules that cannot be trusted to the client, such as role checks, status transitions and reviewer assignments.

## 4. Domain Roles

| Role | Main responsibilities |
| --- | --- |
| `AUTHOR` | Create drafts, submit articles, upload files, check status and notifications |
| `REVIEWER` | View assigned submissions and submit reviews |
| `EDITOR` | Assign reviewers, review editorial queue, accept/reject/publish submissions |
| `ADMIN` | Manage roles and access all operational modules |

New users are always registered as `AUTHOR`. Higher roles are assigned manually by an administrator. This was an intentional security decision: users must not be able to choose privileged roles during login or registration.

## 5. Backend Modules

- `user`: registration, current user profile, role management and demo bootstrap users.
- `submission`: article metadata, authors, categories, statuses, file metadata, upload and download.
- `review`: reviewer assignments, review submission, editorial decisions and status transitions.
- `notification`: in-app workflow notifications and read/unread state.
- `reporting`: operational summary, CSV export and PDF export.
- `monitoring`: custom Micrometer gauges for domain metrics.
- `config`: security, data initialization and application configuration.
- `common`: shared exceptions and web helpers.

The backend uses Spring Security with stateless JWT Bearer authentication. Login and registration return a signed token, and the frontend stores only that token for the current browser session. Passwords are never stored in the frontend state or local storage. Authorization is still enforced by backend controllers and services, so hiding UI actions is only a convenience layer, not the security boundary.

## 6. Submission Workflow

The article lifecycle is represented by statuses:

`DRAFT -> SUBMITTED -> IN_REVIEW -> REVIEW_COMPLETED -> ACCEPTED/REJECTED -> PUBLISHED`

Important rules:

- Drafts can be edited before review.
- Only submitted or already in-review submissions can receive reviewer assignments.
- A reviewer assignment changes the submission status to `IN_REVIEW`.
- A submitted review changes the assignment status and may move the submission to `REVIEW_COMPLETED`.
- Editorial `ACCEPT` and `REJECT` decisions require a `REVIEW_COMPLETED` submission.
- `PUBLISH` requires an `ACCEPTED` submission.

These rules are validated on the backend, not only in the UI.

## 7. Frontend Screens

The React application contains these main routes:

- `/`: start page, project overview, authentication panel and full role context after login.
- `/submissions`: create/edit submissions, upload files, filter, list and inspect details.
- `/notifications`: workflow event notifications with read/unread state.
- `/reviews`: reviewer assignments, review form and review history.
- `/published`: editorial decision form, publication queue and decision history.
- `/reports`: operational counters and CSV/PDF export actions.
- `/admin`: manual role management.

The UI is bilingual. Polish is used for the university-facing screenshots and English is available for public repository usage.

## 8. Frontend Architecture, API and State

The frontend is organized around a small application shell and feature pages:

- `App.tsx`: route definitions for home, submissions, notifications, reviews, publication queue, reports and administration.
- `components/AppLayout.tsx`: shared navigation, language switcher and role-aware menu visibility.
- `pages/*`: page-level state for forms, filters, selected rows and refresh actions.
- `api/client.ts`: typed API access layer based on axios.
- `api/types.ts`: shared TypeScript contracts that mirror backend request and response DTOs.
- `auth/AuthProvider.tsx`: authentication state, login, registration, logout and current-user loading.

The frontend does not use a global state library. State is intentionally kept simple: authentication is stored in React Context, while page-specific data is loaded and managed inside the relevant page component with `useState` and `useEffect`. Submission lists, selected records, review forms, notification state, report downloads and loading flags are owned by the page that renders them. After mutations, pages reload the affected data from the API so the UI stays close to backend state.

JWT handling is centralized in `AuthProvider` and `api/client.ts`. Login and registration return a token and current user profile. The browser stores only `{ token }` in local storage under `article-submission-auth`; email and password are not persisted. Authenticated API calls create an axios client with the `Authorization: Bearer <token>` header. On application startup, the stored token is validated by calling `/api/auth/me`; if the request fails, the token is removed and the user is logged out locally.

The API base URL is read from `VITE_API_BASE_URL` and falls back to `http://localhost:8080`. Login and registration are unauthenticated axios calls, while protected endpoints are called through the token-aware client. The API layer returns typed response data and keeps endpoint usage grouped by domain, which makes pages less dependent on raw HTTP details.

Network and validation errors are handled at two levels. Authentication extracts backend error messages from axios responses and displays login or registration feedback. Feature pages catch API errors around mutations such as submission creation, reviewer assignment, review submission, role updates and exports, then show a user-facing fallback message. A global axios error interceptor is intentionally not used because each workflow needs a different message. The backend remains the source of truth for authorization and business-rule validation, so frontend checks are only used to improve the user experience.

Role-aware navigation in `AppLayout.tsx` hides unavailable tabs, but it is not treated as a security mechanism. Every privileged action is also protected by Spring Security and service-level business rules.

The main API groups used by the frontend are:

- `/api/auth`: login, registration and current-user profile.
- `/api/users`: reviewer/editor lookup and administrator role management.
- `/api/categories`: scientific categories.
- `/api/submissions`: submission list, details, draft editing and creation.
- `/api/submissions/{id}/files`: file upload and download.
- `/api/review-assignments`, `/api/reviews`, `/api/editorial-decisions`: peer-review workflow.
- `/api/notifications`: workflow notifications and read/unread state.
- `/api/reports`: operational counters and CSV/PDF exports.

## 9. Database

The database schema is managed by Flyway migrations:

- `V1__initial_schema.sql`: core domain tables.
- `V2__seed_initial_data.sql`: demo users, roles and categories.
- `V3__review_assignment_uniqueness.sql`: reviewer assignment uniqueness.
- `V4__normalize_demo_passwords.sql`: demo password normalization.
- `V5__notifications.sql`: notification table and workflow event support.

Main persisted entities include users, article submissions, authors, categories, files, review assignments, reviews, editorial decisions and notifications.

## 10. Docker Deployment

`docker-compose.yml` starts:

- `postgres`: PostgreSQL database.
- `backend`: Spring Boot API.
- `frontend`: production frontend served by Nginx.
- `prometheus`: metrics scraping.
- `grafana`: dashboard visualization.

The full stack can be started with:

```bash
docker compose up -d --build
```

Host ports are bound to `127.0.0.1` and can be changed through `.env` variables with the `_HOST_PORT` suffix. This is used when local ports such as `5432`, `3000` or `9090` are already occupied.

Before the first run, create `.env` from `.env.example`. On Windows PowerShell use `Copy-Item .env.example .env`; on macOS and Linux use `cp .env.example .env`.

## 11. Monitoring

Monitoring has two levels:

- Technical health: `/actuator/health` and Prometheus scrape status.
- Domain metrics: total submissions, submissions by status, total review assignments, review assignments by status and uploaded files.

Grafana is provisioned from repository files, so the dashboard is recreated automatically when the Docker stack starts.

## 12. Testing and Verification

Backend integration tests verify the most important business rules:

- Spring application context starts correctly.
- Draft editing and submission search work.
- Two different authors can submit articles with the same title while ownership remains separated.
- A non-owner author cannot edit another user's draft.
- Role administration, review workflow, notifications and report exports work together.
- Duplicate reviewer assignment for the same submission is rejected.
- JWT login, current-user lookup, Bearer-token protection, invalid token rejection, administrator access and role-change authorization are covered by security tests.

Frontend verification includes:

- `npm run lint`
- `npm run build`
- `npm run screenshots`

Backend tests use `.\mvnw.cmd test` on Windows and `./mvnw test` on macOS/Linux.

The screenshot command generates documentation images from the running application. This is useful because it proves that the documented UI still matches the current product.

GitHub Actions runs the backend Maven tests and frontend lint/build jobs on pushes and pull requests to `main`.

## 13. Screenshots

The current documentation uses 18 fresh screenshots:

- Authentication and home page.
- Submission form, file upload, filtering, details and draft editing.
- Reviewer assignment, review form and review history.
- Editorial decisions and publication queue.
- Notifications, reports and admin role management.
- Prometheus targets and Grafana dashboard.

Full gallery: [SCREENSHOTS.md](SCREENSHOTS.md)
