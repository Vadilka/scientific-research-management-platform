# University Login Research / Analiza logowania uczelnianego

## EN Purpose

The project currently supports local account registration and JWT Bearer authentication for development and demonstration. A production university deployment should integrate with the institution's central identity provider.

## Recommended Direction

The preferred integration path is OpenID Connect or SAML 2.0, depending on what the university already provides.

- Use OpenID Connect when the university exposes an OAuth2/OIDC identity provider.
- Use SAML 2.0 when the university uses a SAML identity provider such as Shibboleth, ADFS, Okta, or another academic federation tool.
- Use CAS only if the university explicitly operates a CAS server and expects applications to integrate with the CAS protocol.

## Options

### OpenID Connect / OAuth2 Login

Spring Security provides OAuth2 support through OAuth2 Client, OAuth2 Resource Server, and Authorization Server areas. For application login, OAuth2 Login is part of OAuth2 Client support and is configured with `spring-boot-starter-oauth2-client`.

Best fit:

- modern university identity provider,
- OAuth2/OIDC metadata available,
- future frontend token-based architecture.

### SAML 2.0 Login

Spring Security SAML 2.0 Login allows the application to act as a relying party and authenticate users through an external SAML asserting party. It uses the Web Browser SSO Profile.

Best fit:

- university uses SAML/Shibboleth federation,
- identity provider metadata is available,
- application should map SAML attributes such as email, name, and affiliation into local user records.

### CAS

CAS is a single sign-on and single sign-off protocol. The CAS server authenticates the user once and issues service tickets that client applications validate.

Best fit:

- university explicitly uses Apereo CAS,
- application receives a service ticket and validates it against `/serviceValidate` or CAS 3.0 endpoints,
- local system maps the returned username and attributes to application roles.

## Proposed Implementation Plan

1. Keep local accounts for development and fallback.
2. Add a separate university login button.
3. Add `externalProvider` and `externalSubject` fields to users.
4. On first SSO login, create a local user with role `AUTHOR`.
5. Keep `REVIEWER`, `EDITOR`, and `ADMIN` assignments manual.
6. Map university attributes to `fullName`, `email`, and optional affiliation.
7. Store no university password in the application.

## PL Cel

Projekt obsługuje obecnie lokalne konta oraz uwierzytelnianie JWT Bearer na potrzeby developmentu i demonstracji. W wersji produkcyjnej dla uczelni aplikacja powinna zostać zintegrowana z centralnym dostawcą tożsamości.

## Rekomendacja

Najlepszą ścieżką jest OpenID Connect albo SAML 2.0, zależnie od infrastruktury uczelni.

- OpenID Connect, jeżeli uczelnia udostępnia OAuth2/OIDC.
- SAML 2.0, jeżeli uczelnia korzysta z SAML, Shibboleth, ADFS, Okta albo federacji akademickiej.
- CAS, jeżeli uczelnia faktycznie używa Apereo CAS i wymaga integracji przez protokół CAS.

## Plan wdrożenia

1. Zostawić konta lokalne do developmentu i trybu awaryjnego.
2. Dodać oddzielny przycisk logowania uczelnianego.
3. Rozszerzyć `users` o `externalProvider` i `externalSubject`.
4. Przy pierwszym logowaniu SSO tworzyć konto lokalne z rolą `AUTHOR`.
5. Role `REVIEWER`, `EDITOR` i `ADMIN` dalej nadawać ręcznie.
6. Mapować atrybuty uczelniane na `fullName`, `email` i afiliację.
7. Nie przechowywać hasła uczelnianego w aplikacji.

## Sources

- [Spring Security SAML 2.0 Login](https://docs.spring.io/spring-security/reference/servlet/saml2/login/index.html)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [Apereo CAS Protocol Specification](https://apereo.github.io/cas/7.2.x/protocol/CAS-Protocol-Specification.html)
