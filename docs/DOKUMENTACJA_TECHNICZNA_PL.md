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

Backend wykorzystuje Spring Security oraz bezstanowe uwierzytelnianie JWT Bearer. Logowanie i rejestracja zwracają podpisany token, a frontend przechowuje wyłącznie ten token dla bieżącej sesji przeglądarki. Hasła nie są przechowywane w stanie frontendu ani w local storage. Właściwa autoryzacja nadal jest egzekwowana przez kontrolery i serwisy backendu, więc ukrywanie akcji w interfejsie jest tylko warstwą wygody, a nie granicą bezpieczeństwa.

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

## 8. Architektura frontendu, API i stan

Frontend jest zorganizowany wokół niewielkiej powłoki aplikacji oraz stron funkcjonalnych:

- `App.tsx`: definicje tras dla strony głównej, zgłoszeń, powiadomień, recenzji, publikacji, raportów i administracji.
- `components/AppLayout.tsx`: wspólna nawigacja, przełącznik języka i widoczność menu zależna od roli.
- `pages/*`: stan ekranów, formularzy, filtrów, zaznaczonych rekordów i odświeżania danych.
- `api/client.ts`: typowana warstwa komunikacji z API oparta na axios.
- `api/types.ts`: kontrakty TypeScript odpowiadające requestom i response DTO backendu.
- `auth/AuthProvider.tsx`: stan uwierzytelnienia, logowanie, rejestracja, wylogowanie i ładowanie bieżącego użytkownika.

Frontend nie używa zewnętrznej biblioteki globalnego stanu. Stan jest celowo prosty: uwierzytelnienie znajduje się w React Context, a dane konkretnych ekranów są pobierane i obsługiwane w komponentach stron przez `useState` i `useEffect`. Listy zgłoszeń, wybrane rekordy, formularze recenzji, stan powiadomień, eksporty raportów i flagi ładowania należą do strony, która je renderuje. Po mutacjach strony ponownie pobierają zmienione dane z API, aby interfejs pozostawał blisko stanu backendu.

Obsługa JWT jest scentralizowana w `AuthProvider` oraz `api/client.ts`. Logowanie i rejestracja zwracają token oraz profil użytkownika. Przeglądarka zapisuje wyłącznie `{ token }` w local storage pod kluczem `article-submission-auth`; e-mail i hasło nie są utrwalane. Uwierzytelnione wywołania API tworzą klienta axios z nagłówkiem `Authorization: Bearer <token>`. Po starcie aplikacji zapisany token jest sprawdzany przez `/api/auth/me`; jeśli żądanie się nie powiedzie, token jest usuwany, a użytkownik zostaje lokalnie wylogowany.

Bazowy adres API jest odczytywany z `VITE_API_BASE_URL`, a domyślnie wskazuje `http://localhost:8080`. Logowanie i rejestracja są wykonywane jako nieuwierzytelnione żądania axios, natomiast endpointy chronione są wywoływane przez klienta z tokenem JWT. Warstwa API zwraca typowane dane i grupuje endpointy domenowo, dzięki czemu strony nie zależą bezpośrednio od szczegółów HTTP.

Błędy sieciowe i walidacyjne są obsługiwane na dwóch poziomach. Uwierzytelnianie wyciąga komunikaty błędów z odpowiedzi axios i pokazuje feedback logowania albo rejestracji. Strony funkcjonalne przechwytują błędy przy operacjach takich jak tworzenie zgłoszenia, przypisanie recenzenta, wysłanie recenzji, zmiana roli i eksporty, a następnie pokazują komunikat zastępczy. Globalny interceptor błędów axios nie jest używany celowo, ponieważ każdy workflow wymaga innego komunikatu dla użytkownika. Backend pozostaje źródłem prawdy dla autoryzacji i reguł biznesowych, więc walidacje frontendu służą głównie wygodzie użytkownika.

Nawigacja zależna od roli w `AppLayout.tsx` ukrywa niedostępne zakładki, ale nie jest traktowana jako mechanizm bezpieczeństwa. Każda operacja uprzywilejowana jest dodatkowo chroniona przez Spring Security oraz reguły biznesowe usług.

Główne grupy API używane przez frontend:

- `/api/auth`: logowanie, rejestracja i profil bieżącego użytkownika.
- `/api/users`: wyszukiwanie recenzentów/redaktorów i administracja rolami.
- `/api/categories`: kategorie naukowe.
- `/api/submissions`: lista, szczegóły, edycja szkicu i tworzenie zgłoszeń.
- `/api/submissions/{id}/files`: upload i download plików.
- `/api/review-assignments`, `/api/reviews`, `/api/editorial-decisions`: workflow recenzji.
- `/api/notifications`: powiadomienia i stan przeczytane/nieprzeczytane.
- `/api/reports`: liczniki operacyjne oraz eksport CSV/PDF.

## 9. Baza danych

Schemat bazy jest zarządzany migracjami Flyway:

- `V1__initial_schema.sql`: główne tabele domenowe.
- `V2__seed_initial_data.sql`: użytkownicy demo, role i kategorie.
- `V3__review_assignment_uniqueness.sql`: unikalność przypisań recenzentów.
- `V4__normalize_demo_passwords.sql`: ujednolicenie haseł demo.
- `V5__notifications.sql`: tabela powiadomień i obsługa zdarzeń workflow.

Główne encje to użytkownicy, zgłoszenia artykułów, autorzy, kategorie, pliki, przypisania recenzentów, recenzje, decyzje redakcyjne i powiadomienia.

## 10. Docker

`docker-compose.yml` uruchamia:

- `postgres`: baza PostgreSQL.
- `backend`: Spring Boot API.
- `frontend`: produkcyjny frontend serwowany przez Nginx.
- `prometheus`: pobieranie metryk.
- `grafana`: wizualizacja dashboardu.

Start całego środowiska:

```bash
docker compose up -d --build
```

Porty hosta są przypięte do `127.0.0.1` i można je zmienić w `.env` przez zmienne z sufiksem `_HOST_PORT`. Jest to używane wtedy, gdy lokalne porty takie jak `5432`, `3000` albo `9090` są już zajęte.

Przed pierwszym uruchomieniem należy utworzyć `.env` na podstawie `.env.example`. W Windows PowerShell służy do tego `Copy-Item .env.example .env`, a w macOS i Linux `cp .env.example .env`.

## 11. Monitoring

Monitoring działa na dwóch poziomach:

- technicznym: `/actuator/health` i status scrape w Prometheus,
- domenowym: liczba zgłoszeń, zgłoszenia według statusów, liczba przypisań recenzentów, przypisania według statusów i liczba plików.

Grafana jest provisionowana z plików w repozytorium, więc dashboard odtwarza się automatycznie po uruchomieniu Docker Compose.

## 12. Testy i weryfikacja

Testy integracyjne backendu sprawdzają kluczowe reguły biznesowe:

- poprawny start kontekstu Spring,
- edycję szkicu i wyszukiwanie zgłoszeń,
- możliwość zgłoszenia artykułów o tym samym tytule przez dwóch różnych autorów przy zachowaniu oddzielnego właściciela,
- blokadę edycji szkicu przez innego autora,
- administrację rolami, workflow recenzji, powiadomienia oraz eksporty raportów.
- odrzucenie ponownego przypisania tego samego recenzenta do tego samego zgłoszenia.
- logowanie JWT, endpoint bieżącego użytkownika, ochronę Bearer tokenem, odrzucenie niepoprawnego tokena, dostęp administratora oraz autoryzację zmiany ról.

Frontend jest weryfikowany przez:

- `npm run lint`
- `npm run build`
- `npm run screenshots`

Testy backendu uruchamia się przez `.\mvnw.cmd test` w Windows oraz `./mvnw test` w macOS/Linux.

Komenda screenshot generuje obrazy dokumentacyjne z działającej aplikacji. Dzięki temu dokumentacja wizualna jest powiązana z aktualnym stanem systemu.

GitHub Actions uruchamia testy Maven backendu oraz lint/build frontendu przy pushach i pull requestach do `main`.

## 13. Zrzuty ekranu

Aktualna dokumentacja wykorzystuje 18 świeżych zrzutów:

- autoryzacja i strona główna,
- formularz zgłoszenia, upload plików, filtrowanie, szczegóły i edycja szkicu,
- przypisanie recenzenta, formularz recenzji i historia recenzji,
- decyzje redakcyjne i kolejka publikacji,
- powiadomienia, raporty i panel administratora,
- Prometheus targets oraz Grafana dashboard.

Pełna galeria: [SCREENSHOTS.md](SCREENSHOTS.md)
