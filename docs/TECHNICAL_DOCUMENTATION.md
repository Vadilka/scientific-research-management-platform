# Technical Documentation / Dokumentacja techniczna

This file is a compact bilingual overview. Full language-specific versions are available in:

- [TECHNICAL_DOCUMENTATION_EN.md](TECHNICAL_DOCUMENTATION_EN.md)
- [DOKUMENTACJA_TECHNICZNA_PL.md](DOKUMENTACJA_TECHNICZNA_PL.md)

## EN Summary

The system is a full-stack web application for scientific article submissions. It includes:

- React + TypeScript frontend with Polish/English interface.
- Spring Boot backend with REST API and role-based access.
- PostgreSQL database with Flyway migrations.
- Article workflow from draft to publication.
- Manual administrator-controlled role management.
- Reviewer assignments, reviews and editorial decisions.
- In-app workflow notifications.
- CSV/PDF reporting.
- Docker Compose deployment.
- Actuator, Prometheus and Grafana monitoring.
- Backend integration tests, frontend lint/build and generated screenshots.
- GitHub Actions CI for backend tests and frontend lint/build on `main`.

The main architectural decision is that the backend owns business rules. The frontend shows or hides actions depending on the user role, but status transitions, reviewer assignment rules and administrator role changes are validated by backend services.

## PL Podsumowanie

System jest pełną aplikacją webową do obsługi zgłoszeń artykułów naukowych. Obejmuje:

- frontend React + TypeScript z interfejsem polskim i angielskim,
- backend Spring Boot z REST API i kontrolą dostępu,
- bazę PostgreSQL z migracjami Flyway,
- workflow artykułu od szkicu do publikacji,
- ręczne zarządzanie rolami przez administratora,
- przypisania recenzentów, recenzje i decyzje redakcyjne,
- powiadomienia wewnętrzne workflow,
- raporty CSV/PDF,
- wdrożenie Docker Compose,
- monitoring Actuator, Prometheus i Grafana,
- testy integracyjne backendu, lint/build frontendu oraz generowane screenshoty.
- GitHub Actions CI dla testów backendu oraz lint/build frontendu na gałęzi `main`.

Najważniejsza decyzja architektoniczna polega na tym, że backend jest właścicielem reguł biznesowych. Frontend pokazuje albo ukrywa akcje zależnie od roli, ale przejścia statusów, przypisania recenzentów i zmiany ról administratora są walidowane przez serwisy backendowe.

## Screenshot Set / Zestaw zrzutów

Current documentation uses 18 fresh screenshots generated from the running Docker environment:

- start page and registration,
- authenticated home with complete navigation,
- submissions, files, filters, details and draft editing,
- reviewer assignment, review form and review history,
- editorial decision form and publication queue,
- notifications, reports and admin panel,
- Prometheus targets and Grafana dashboard.

Full gallery: [SCREENSHOTS.md](SCREENSHOTS.md)
