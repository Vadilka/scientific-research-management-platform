# Frontend

React + TypeScript frontend for the Scientific Research Management Platform.

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
