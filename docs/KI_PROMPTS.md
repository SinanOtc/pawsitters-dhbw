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
