# Frontend

React + TypeScript frontend for the Scientific Research Management Platform.

## Architecture

- `src/App.tsx` defines the route tree.
- `src/components/AppLayout.tsx` provides navigation, language switching and role-aware menu visibility.
- `src/pages/*` owns page-level loading, form and refresh state.
- `src/api/client.ts` contains typed axios calls to the Spring Boot API.
- `src/api/types.ts` contains TypeScript contracts shared across pages.
- `src/auth/AuthProvider.tsx` manages JWT session state with React Context.

## State Management

The application deliberately avoids Redux or another global state library. The only global state is authentication, exposed through `AuthProvider` and `useAuth`. Workflow data such as submissions, reviews, notifications, filters, selected records and loading flags belongs to the page that renders it. After a mutation, the page reloads the affected data from the API so the UI stays close to backend state.

Role-based menu visibility in `AppLayout` is a UX feature only. Real authorization is enforced by Spring Security on the backend.

## Authentication and API Calls

Login and registration return a JWT token and the current user profile. The frontend stores only `{ token }` in local storage under `article-submission-auth`; it does not persist the user's email or password.

Authenticated API calls use:

```http
Authorization: Bearer <token>
```

On startup, the stored token is checked with `/api/auth/me`. If that request fails, the token is removed and the local session is cleared.

The API base URL comes from `VITE_API_BASE_URL` and falls back to `http://localhost:8080`. Login and registration are unauthenticated axios requests, while all protected calls use an axios client created with the current JWT token. The API layer returns typed response data and keeps endpoints grouped by domain: auth, users, categories, submissions, files, reviews, editorial decisions, notifications and reports.

## Error Handling

Authentication errors read the backend `message` field from axios responses and display login or registration feedback. Feature pages catch API failures around mutations and exports, then show a user-facing fallback message. There is no global axios error interceptor because the visible message depends on the current workflow step. Backend authorization and business-rule validation remain the security boundary.

## Local Development

Install dependencies:

```bash
npm ci
```

Start the Vite development server:

```bash
npm run dev
```

The frontend expects the backend at `http://localhost:8080` by default. To use a different API address, create a local environment file and set:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## Verification

```bash
npm run lint
npm run build
```

## Screenshots

Screenshots are generated from a running Docker environment. Start the full stack from the repository root first:

```bash
docker compose up -d --build
```

Then run:

```bash
npm run screenshots
```

The script uses these default service URLs:

- Frontend: `http://localhost:4173`
- Backend API: `http://localhost:8080`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

They can be overridden with `APP_URL`, `API_URL`, `PROMETHEUS_URL` and `GRAFANA_URL`.
