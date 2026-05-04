# KI-Nutzung

## Spring Security Setup + Tests
- **Datum:** 26.04.2026
- **Tool:** Claude Opus 4.7
- **Prompts:** 
  - Erstelle 3 Tests für Spring Security mit MockMvc
  - Erstelle eine simple login.html, home.html und dashboard.html
  - Vereinzelt für Code Erklärungen bzw. Hinweise genutzt

## Domain-Entities (User, UserRole, OwnerProfile) + Validation-Tests
- **Datum:** 26.04 && 03.05.2026
- **Tool:** Claude Opus 4.7
- **Prompts:**
  -  Beratung zur Aufteilung in `User` (Login-Daten) vs.
    `OwnerProfile`/`HostProfile` (Profildaten)
  - Beratung für sinnvolle `@Column(length = ...)` und `@Size(max = ...)` Werte (E-Mail
    RFC 5321 → 254, BCrypt-Hash → 60, Adressen → 255)
  - Empfehlungen für Unit-Tests mit `jakarta.validation.Validator`
- **Verifikation:** Alle Tests lokal mit `./mvnw test` ausgeführt und auf grün geprüft. 

  ## PetOwner-Registrierung (Repository, Service, Controller, Templates, Tests)
  - **Datum:** 04.05.2026
  - **Tool:** Claude Opus 4.7
  - **Prompts:**
    - Architektur-Beratung zur Schichtenaufteilung Repository / Service / Controller / Form / Template
    - Vorlagen für `JpaRepository`-Interfaces mit Query-Derivation (`findByEmail`, `findByUserId`,
      `existsByEmail`)
    - Controller-Skizze mit `@Valid`, `BindingResult`, `@AuthenticationPrincipal`,
      ModelAttribute-Naming
    - Form-DTOs als Java Records (`RegisterOwnerForm`, `UpdateOwnerForm`)
    - Mockito-basierte Service-Tests und `@WebMvcTest` Controller-Tests vollständig KI generiert. Mussten danach aber manuell debuggt werden
    - Erklärung von Spring oder SonarQube Warnungen und ggf. Ausbesserung dieser
    - `templates/owner/register.html, profile.html, profile-edit.html` vollständig von KI generiert. Kleine manuelle Anpassungen mussten gemacht werden
    - Testdokumentation von KI generiert
  - **Verifikation:** Code wurde aktiv mitgestaltet. Verständnis durch Nachfragen
    und Erklärungen sichergestellt, alle Tests lokal mit `./mvnw test` werden akzeptiert.
  - **!! Dieser Abschnitt wurde mit KI erstellt!!**