# 🐾 Pawsitters — Pet Holiday Platform

Plattform zur Vermittlung von Tierbetreuung während Urlauben — Tierhalter publizieren Betreuungsanfragen, Hosts geben Angebote ab.

**DHBW Heilbronn** · Software-Engineering-Projekt · 3er-Team

> 🔥 **Aktuelle Phase:** [Engineering Excellence](https://github.com/SinanOtc/pawsitters-dhbw/issues/80) — Production-grade Polish über die bestehende MVC-Architektur. Live-Deployment, PostgreSQL, ArchUnit, HTMX, Observability. Tracking-Issue mit allen Tasks: [#80](https://github.com/SinanOtc/pawsitters-dhbw/issues/80).

---

## 👥 Team & Rollen

| Mitglied | Bereich |
|---|---|
| **Sinan Oturucu** | Backend (Entities, Services, Controller, Tests, Security) |
| **Kevin Christian Albrandt** | Frontend (Thymeleaf-Templates, Styling, UX) |
| **Vincent Roland Scheibe** | Doku, Planung, Architektur |

---

## 🚀 Quick Start

### Voraussetzungen
- Java 21 (Temurin empfohlen)
- Maven Wrapper liegt bei (`./mvnw`)

### App starten
```bash
./mvnw spring-boot:run
```
App läuft auf [http://localhost:8080](http://localhost:8080).

### Tests laufen lassen
```bash
./mvnw test         # Alle Tests
./mvnw verify       # Build + Tests
```

---

## 🔐 Demo-Logins

Beim Start im `dev`-Profil werden automatisch Demo-Daten angelegt (1 Owner mit Pet + CareRequest, 1 Host mit Profil + Offer auf die Request). Direkt einloggbar:

| Rolle | E-Mail | Passwort |
|---|---|---|
| **Owner** | `demo-owner@test.de` | `demo12345` |
| **Host** | `demo-host@test.de` | `demo12345` |

---

## 🧰 Tech-Stack

| Schicht | Technologie |
|---|---|
| **Language** | Java 21 |
| **Framework** | Spring Boot 4 |
| **Build** | Maven |
| **Web** | Spring MVC + Thymeleaf (server-side rendering) |
| **Persistence** | Spring Data JPA + Hibernate |
| **DB** | H2 In-Memory (dev/test), PostgreSQL (prod) |
| **Security** | Spring Security · Session-Cookie · BCrypt · CSRF · Role-Based-Access |
| **Validation** | Jakarta Bean Validation |
| **Testing** | JUnit 5 · Mockito · `@WebMvcTest` · `@DataJpaTest` · 133+ Tests |
| **Tooling** | Lombok · DevTools |

---

## 🏛️ Architektur (kurz)

Klassische **MVC + Layered Architecture**. Begründung & Details: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

```
┌──────────────────────────────────────────────────┐
│ Web-Layer        @Controller + Form-Records      │
│                  Templates (Thymeleaf)           │
├──────────────────────────────────────────────────┤
│ Service-Layer    @Service + Domain Exceptions    │
│                  Owner-Scoping als Security      │
├──────────────────────────────────────────────────┤
│ Persistence      JpaRepository + @Query (JPQL)   │
├──────────────────────────────────────────────────┤
│ Domain           JPA Entities + Bean Validation  │
└──────────────────────────────────────────────────┘
```

**Pattern-Highlights:**
- `GlobalExceptionHandler` mit `ModelAndView` für 404/409-Mappings
- `CurrentUserResolver` als zentraler Helper (eliminiert 5fache DRY-Verletzung)
- Cross-Field-Validation per `@AssertTrue` in Entities UND Forms
- Service-to-Service-Composition für Security (z. B. `OfferService.createOffer` re-verifiziert über `findMatchingForHost`)

---

## 📚 Doku

| Dokument | Inhalt |
|---|---|
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Architektur-Entscheidungen, Schichten, Tech-Stack-Begründung |
| [`docs/SECURITY.md`](docs/SECURITY.md) | Sicherheitskonzept, Threats + Mitigations, Shift-Security-Left |
| [`docs/TEST_DOCUMENTATION.md`](docs/TEST_DOCUMENTATION.md) | Tabellarische Test-Übersicht aller Test-Klassen |
| [`docs/ENTWICKLUNGSPROZESS.md`](docs/ENTWICKLUNGSPROZESS.md) | Aufgabenteilung, Git-Workflow, Sprint-Reflexion |
| [`docs/KI_PROMPTS.md`](docs/KI_PROMPTS.md) | Konsolidierte KI-Nutzungs-Doku aller Teammitglieder |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Projekt-Regeln & Workflow |

---

## 🧪 Tests

Aktuell **133 Tests, 100 % grün**. Verteilung:

| Schicht | Tests | Stil |
|---|---|---|
| Domain | 30+ | Jakarta Validator direkt, keine Spring-Last |
| Service | 41+ | Mockito-Unit-Tests |
| Web (Controller) | 45+ | `@WebMvcTest` mit MockMvc + Spring Security Test |
| Repository | 5 | `@DataJpaTest` gegen In-Memory-H2 |
| Security/Config | 7 | Integration-Tests gegen die echte SecurityConfig |

`./mvnw test` reicht.

---

## 🛠️ Setup-Profile

| Profil | Zweck | DB | Demo-Seed |
|---|---|---|---|
| `dev` (Default) | Lokale Entwicklung | H2 In-Memory | ✅ |
| `test` | Test-Runs | isolierte H2 | ❌ |
| `prod` | Production | PostgreSQL (Env-Vars) | ❌ |

Profil wechseln via `SPRING_PROFILES_ACTIVE=prod` oder in `application.yaml`.
