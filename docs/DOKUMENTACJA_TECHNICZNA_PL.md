# Dokumentacja techniczna

## 1. Cel projektu

System zgłaszania artykułów naukowych jest aplikacją webową do obsługi procesu akademickiego zgłoszenia artykułu. System obejmuje zgłoszenie autora, zarządzanie metadanymi, przypisanie recenzenta, recenzję, decyzję redakcyjną, publikację, powiadomienia, raporty i monitoring działania aplikacji.

Projekt został przygotowany jako kompletna aplikacja na przedmiot JEE oraz jako repozytorium portfolio. Dlatego poza ekranami biznesowymi zawiera także Docker, testy, wygenerowane zrzuty ekranu, dokumentację dwujęzyczną oraz monitoring.

## 2. Stos technologiczny

| Warstwa | Technologia | Odpowiedzialność |
| --- | --- | --- |
| Frontend | React, TypeScript, Vite, React Router, i18next | Interfejs użytkownika, routing, dwujęzyczne etykiety, formularze |
| Backend | Java, Spring Boot, Spring Security, Spring Data JPA | REST API, logika biznesowa, autoryzacja, zapis danych |
| Baza danych | PostgreSQL, Flyway | Trwałe dane i migracje schematu |
| Pliki | Wolumen Docker | Przechowywanie przesłanych plików artykułów |
| Raporty | CSV, OpenPDF | Eksport danych operacyjnych |
| Monitoring | Actuator, Micrometer, Prometheus, Grafana | Health checki i metryki |
| Infrastruktura | Docker Compose | Uruchamianie całego środowiska lokalnie |
| Testy | JUnit, Spring Boot Test, frontend build/lint | Weryfikacja kluczowych scenariuszy |

## 3. Architektura uruchomieniowa

Aplikacja działa w architekturze warstwowej:

1. Przeglądarka pobiera frontend React z kontenera frontendowego.
2. Frontend wysyła uwierzytelnione żądania REST do backendu Spring Boot.
3. Backend sprawdza uprawnienia, waliduje przejścia statusów i komunikuje się z PostgreSQL.
4. Pliki artykułów są zapisywane w wolumenie Docker, a ich metadane w bazie danych.
5. Actuator udostępnia health check oraz metryki Prometheus.
6. Prometheus pobiera metryki z backendu, a Grafana prezentuje je na dashboardzie.

Taki podział pozwala utrzymać frontend jako warstwę interakcji, a backend jako miejsce odpowiedzialne za reguły, których nie można ufać klientowi: role, przejścia statusów, przypisania recenzentów i decyzje redakcyjne.

## 4. Role domenowe

| Rola | Główne zadania |
| --- | --- |
| `AUTHOR` | Tworzenie szkiców, zgłaszanie artykułów, upload plików, sprawdzanie statusu i powiadomień |
| `REVIEWER` | Obsługa przypisanych recenzji i składanie opinii |
| `EDITOR` | Przypisywanie recenzentów, obsługa kolejki redakcyjnej, akceptacja, odrzucenie i publikacja |
| `ADMIN` | Ręczne zarządzanie rolami i dostęp do modułów operacyjnych |

Nowy użytkownik zawsze otrzymuje rolę `AUTHOR`. Role podwyższone są nadawane ręcznie przez administratora. To świadoma decyzja bezpieczeństwa: użytkownik nie może samodzielnie wybrać roli uprzywilejowanej podczas logowania albo rejestracji.

## 5. Moduły backendu

- `user`: rejestracja, profil aktualnego użytkownika, zarządzanie rolami i konta demonstracyjne.
- `submission`: metadane artykułów, autorzy, kategorie, statusy, pliki, upload i download.
- `review`: przypisania recenzentów, recenzje, decyzje redakcyjne i przejścia statusów.
- `notification`: powiadomienia wewnętrzne oraz stan przeczytane/nieprzeczytane.
- `reporting`: podsumowanie operacyjne, eksport CSV i eksport PDF.
- `monitoring`: własne metryki Micrometer dla danych domenowych.
- `config`: konfiguracja bezpieczeństwa i inicjalizacja danych.
- `common`: wspólne wyjątki i pomocnicze elementy warstwy web.

Backend wykorzystuje Spring Security z HTTP Basic. Frontend przechowuje dane logowania w local storage na potrzeby sesji demonstracyjnej, ale właściwa autoryzacja jest egzekwowana po stronie backendu.

## 6. Workflow zgłoszenia

Cykl życia artykułu opisują statusy:

`DRAFT -> SUBMITTED -> IN_REVIEW -> REVIEW_COMPLETED -> ACCEPTED/REJECTED -> PUBLISHED`

Najważniejsze reguły:

- Szkic można edytować przed wysłaniem do recenzji.
- Recenzenta można przypisać tylko do zgłoszenia `SUBMITTED` lub `IN_REVIEW`.
- Przypisanie recenzenta zmienia status artykułu na `IN_REVIEW`.
- Złożenie recenzji aktualizuje przypisanie i może przenieść artykuł do `REVIEW_COMPLETED`.
- Decyzje `ACCEPT` i `REJECT` wymagają statusu `REVIEW_COMPLETED`.
- Decyzja `PUBLISH` wymaga statusu `ACCEPTED`.

Reguły są sprawdzane na backendzie, a nie wyłącznie w interfejsie.

## 7. Ekrany frontendu

Aplikacja React zawiera główne trasy:

- `/`: strona startowa, opis projektu, panel logowania i kontekst roli po zalogowaniu.
- `/submissions`: tworzenie i edycja zgłoszeń, upload plików, filtrowanie, lista i szczegóły.
- `/notifications`: powiadomienia workflow ze stanem przeczytane/nieprzeczytane.
- `/reviews`: przypisania recenzentów, formularz recenzji i historia recenzji.
- `/published`: formularz decyzji redakcyjnej, kolejka publikacji i historia decyzji.
- `/reports`: liczniki operacyjne oraz eksport CSV/PDF.
- `/admin`: ręczne zarządzanie rolami użytkowników.

Interfejs jest dwujęzyczny. Polski służy głównie dokumentacji uczelnianej, a angielski ułatwia publiczne wykorzystanie repozytorium.

## 8. Baza danych

Schemat bazy jest zarządzany migracjami Flyway:

- `V1__initial_schema.sql`: główne tabele domenowe.
- `V2__seed_initial_data.sql`: użytkownicy demo, role i kategorie.
- `V3__review_assignment_uniqueness.sql`: unikalność przypisań recenzentów.
- `V4__normalize_demo_passwords.sql`: ujednolicenie haseł demo.
- `V5__notifications.sql`: tabela powiadomień i obsługa zdarzeń workflow.

Główne encje to użytkownicy, zgłoszenia artykułów, autorzy, kategorie, pliki, przypisania recenzentów, recenzje, decyzje redakcyjne i powiadomienia.

## 9. Docker

`docker-compose.yml` uruchamia:

- `postgres`: baza PostgreSQL.
- `backend`: Spring Boot API.
- `frontend`: produkcyjny frontend serwowany przez Nginx.
- `prometheus`: pobieranie metryk.
- `grafana`: wizualizacja dashboardu.

Start całego środowiska:

```powershell
docker compose up -d --build
```

## 10. Monitoring

Monitoring działa na dwóch poziomach:

- technicznym: `/actuator/health` i status scrape w Prometheus,
- domenowym: liczba zgłoszeń, zgłoszenia według statusów, liczba przypisań recenzentów, przypisania według statusów i liczba plików.

Grafana jest provisionowana z plików w repozytorium, więc dashboard odtwarza się automatycznie po uruchomieniu Docker Compose.

## 11. Testy i weryfikacja

Testy integracyjne backendu sprawdzają kluczowe reguły biznesowe:

- poprawny start kontekstu Spring,
- edycję szkicu i wyszukiwanie zgłoszeń,
- administrację rolami, workflow recenzji, powiadomienia oraz eksporty raportów.

Frontend jest weryfikowany przez:

- `npm run lint`
- `npm run build`
- `npm run screenshots`

Komenda screenshot generuje obrazy dokumentacyjne z działającej aplikacji. Dzięki temu dokumentacja wizualna jest powiązana z aktualnym stanem systemu.

## 12. Zrzuty ekranu

Aktualna dokumentacja wykorzystuje 18 świeżych zrzutów:

- autoryzacja i strona główna,
- formularz zgłoszenia, upload plików, filtrowanie, szczegóły i edycja szkicu,
- przypisanie recenzenta, formularz recenzji i historia recenzji,
- decyzje redakcyjne i kolejka publikacji,
- powiadomienia, raporty i panel administratora,
- Prometheus targets oraz Grafana dashboard.

Pełna galeria: [SCREENSHOTS.md](SCREENSHOTS.md)
