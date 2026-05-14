# KI-Nutzung

## Spring Security Setup + Tests
- **Datum:** 26.04.2026
- **Tool:** Claude Opus 4.7
- **Prompts:** 
  - Erstelle 3 Tests für Spring Security mit MockMvc
  - Erstelle eine simple login.html, home.html und dashboard.html
  - Vereinzelt für Code Erklärungen bzw. Hinweise genutzt

---

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

---

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
- **Verifikation:** Code wurde aktiv mitgestaltet. Verständnis durch Nachfragen und Erklärungen sichergestellt, alle Tests lokal mit `./mvnw test` werden akzeptiert.
- **!! Dieser Abschnitt wurde mit KI erstellt!!**

---

  ## Pet-Registrierung (Entity, Repository, Service, Controller, Tests)
  - **Datum:** 06.05 && 10.05.2026
  - **Tool:** Claude Opus 4.7
  - **Prompts:**
    - Architektur-Beratung zur Pet-Schichten (Entity 1:n zu OwnerProfile,
      Repository mit
      Owner-Filter, Service mit Zugehörigkeitsprüfung gegen URL-Manipulation)
    - PetForm als Java Record mit `@AssertTrue` für Chip-Konsistenz
      (chipped ↔ chipNumber)
    - PetController-Skizze für Liste/Anlegen/Bearbeiten/Löschen mit
      `mode`-basierter
      Form-View und `currentUserId(...)`-Helper
    - PetServiceTest mit Mockito (KI-generiert, manuell geprüft und
      dokumentiert)
    - PetControllerTest mit `@WebMvcTest` + `@WithMockUser` für Routing,
      Validation,
      Security-Redirect
    - Erklärung von SonarQube-/IntelliJ-Warnungen (DataFlowIssue bei
      `@NotNull`-null,
      SameParameterValue, S2637 duplicate-literal) und Suppress-Strategie
  - **Verifikation:** Alle Tests lokal mit `./mvnw test` grün, manuelle
    Code-Reviews,
    Verständnisprüfung durch Nachfragen.

---

  ## CareRequest (Entity, Repository, Service, Controller, Tests)
  - **Datum:** 11.05.2026
  - **Tool:** Claude Opus 4.7
  - **Prompts:**
    - Schichten-Plan für `CareRequest` analog zum Pet-Pattern: Entity 1:n zu Owner + 1:n zu Pet,
      Repository mit Owner-Filter, Service mit Re-Use der `petService.findByIdForOwner`-Security
    - `@AssertTrue isDateRangeValid()` als Cross-Field-Check für Enddatum nach Startdatum
      (sowohl in der Entity als auch im Form-Record)
    - `CareRequestForm` als Java Record mit `@NotNull` + `@Future` + Cross-Field-Validation
    - Bewusster Verzicht auf Edit/Delete-Routes — Status-Wechsel sind workflow-getrieben,
      nicht user-editierbar (vermeidet inkonsistente Zustände)
    - `CareRequestServiceTest` mit Mockito (auch Security-Edge-Case: fremdes Pet wirft
      `PetNotFoundException`)
    - `CareRequestControllerTest` mit `@WebMvcTest` für Routing, Validation
      (`@NotNull`, `@Future`, `@AssertTrue` Cross-Field), Auth-Redirect
    - Kritische Diskussion von Copilot-Review-Suggestions (Validation-Duplikation,
      Enterprise-Javadoc, Try-Catch in `@Transactional` → bewusst nicht übernommen)
  - **Verifikation:** Alle Tests lokal mit `./mvnw test` grün, manuelle Code-Reviews,
    Verständnisprüfung durch Nachfragen.

---

  ## Host-Profil (Entity, Repository, Service, Controller, Forms, Tests)
  - **Datum:** 12.05.2026
  - **Tool:** Claude Opus 4.7
  - **Prompts:**
    - Beratung: `HostProfile`-Entity mit `Set<PetSpecies>` als
      `@ElementCollection`,
      `@AssertTrue isAvailabilityRangeValid()` als Cross-Field-Check
      für Verfügbarkeitszeitraum,
      `@DecimalMin(inclusive=false)` für `pricePerWeek > 0`
    - `RegisterHostForm` (mit E-Mail/Passwort) vs. `UpdateHostForm`
      (ohne) als Java Records
    - `HostService` analog `OwnerService` — `existsByEmail` über alle
      Rollen,
      Passwort-Hashing vor DB-Schreibung, Schutzregel `UserRole.HOST`
      hardcoded
    - SecurityConfig-Erweiterung: `/host/register` zu `permitAll`,
      `/host/**` mit `ROLE_HOST`
    - `@WebMvcTest` Controller-Tests mit Multi-Select-Param-Binding für
      `acceptedSpecies`
    - Kritische Diskussion von IntelliJ-AI-Suggestions
      (Inline-Access-Control,
      Global-Exception-Handler, „E-Mail-Hint an Angreifer") — bewusst
      nicht übernommen,
      weil Registrierungs- vs. Login-Kontext unterschieden werden
      müssen.
    - Dieser Abschnitt
  - **Verifikation:** Alle Tests lokal mit `./mvnw test` grün (28
    Host-Tests), manuelle Code-Reviews, Verständnisprüfung durch Nachfragen.

  ---

## Offer / Matching-Logik (Entity, Repositories, Service, Controller, Tests)
- **Datum:** 13.05.2026
- **Tool:** Claude Opus 4.7
- **Prompts:**
    - Anforderungsanalyse Issue #7: Was bedeutet "passt zu Host"? Vier Filter
      abgeleitet (Status OPEN, Species ∈ acceptedSpecies, Datum in Verfügbarkeit,
      kein eigenes Offer schon vorhanden)
    - Architektur-Entscheidung: Ein `OfferController` für Host- und Owner-Routes
      (bewusster Pattern-Bruch, weil Offer per Definition zwei Parteien verbindet)
    - JPQL-`@Query` mit `NOT EXISTS`-Subquery für die Matching-Logik
      (Alternative Java-Filter besprochen, SQL-Variante gewählt für Skalierbarkeit
      und um genau diese Stelle gegen Bugs testbar zu machen)
    - Neuer Test-Typ `@DataJpaTest` eingeführt um die JPQL gegen H2 zu verifizieren
      — Mockito allein kann SQL-Bugs nicht fangen
    - Re-Verify-Pattern in `OfferService.createOffer`: Eligibility-Check geht
      über `findMatchingForHost(...).contains(cr)` statt Bedingungen einzeln zu
      prüfen → Single Source of Truth, Query-Änderungen propagieren automatisch
    - `OfferNotEligibleException` als URL-Manipulation-Guard
    - `@WebMvcTest` mit gemischten Rollen (HOST + OWNER) — per-Test
      `@WithMockUser` statt Klassen-Annotation, beide UserRepository-Mocks in
      `@BeforeEach`
- **Verifikation:** Alle Tests lokal mit `./mvnw test` grün (25 neue Offer-Tests),
  manuelle Code-Reviews, Verständnisprüfung durch Nachfragen.

## Offer-Accept + Auto-Reject-Kaskade (#8 + #9 + #10 Teil
1)
- **Datum:** 13.05.2026
- **Tool:** Claude Opus 4.7
- **Prompts:**
    - Scope-Klärung: #8 + #9 + #10 Teil 1 (OPEN→MATCHED)
      zusammen in einem PR,
      #10 Teil 2 (MATCHED→CLOSED via Scheduled-Job) als
      Folge-PR getrennt
    - Service-Methode `OfferService.accept(offerId,
  ownerUserId)` als atomare Operation:
      Offer→ACCEPTED, CareRequest→MATCHED, Kaskade aller
      anderen PENDING-Offers
      derselben CareRequest auf REJECTED
    - Security: gleiche `OfferNotFoundException` für
      nicht-existent UND fremd-besessen
      (Owner-Mismatch) — kein Info-Leak welche Offers
      existieren
    - State-Guards: `OfferNotPendingException` für
      nicht-PENDING Offers UND
      nicht-OPEN CareRequests (Race-Condition-Schutz)
    - Kaskaden-Pattern:
      `findByCareRequestIdAndStatus(...PENDING)` laden, Filter
      `!o.getId().equals(offerId)` damit das anzunehmende
      Offer nicht
      selbst-rejected wird
    - Owner-Scoped Test-Setup mit Mockito + zwei zusätzliche
      Host-Profile für die
      Kaskaden-Verifikation (3 Offers, 1 ACCEPTED + 2
      REJECTED)
- **Verifikation:** Alle 124 Tests grün, manuelle
  Code-Reviews,
  Verständnisprüfung durch Nachfragen.

  ## Global Exception Handler (#57)
    - **Datum:** 14.05.2026
    - **Tool:** Claude Opus 4.7
    - **Prompts:**
        - Begründung warum `@ControllerAdvice` jetzt sinnvoll
          ist (5+ Stellen mit
          custom Exceptions, Vergleichsgruppe
          `Software-Engineering-Projekt-WI24A3`
          hat ein zentrales `ApiExceptionHandler`)
        - Mapping-Strategie: `*NotFoundException` → 404,
          State-Exceptions
          (`OfferNotPending/NotEligible`) → 409, sonstige
          RuntimeExceptions bewusst
          ungefangen → Spring-Default-500 damit echte Bugs
          sichtbar bleiben
        - Debug: `@ResponseStatus` auf `@ExceptionHandler`
          triggert intern
          `sendError()` → BasicErrorController übernimmt das
          Rendering und unsere
          View wird verworfen. Fix: `ModelAndView` mit
          explizitem `setStatus(...)`
        - Debug: Inner `@Controller`-Klassen werden von
          `@WebMvcTest` Component-Scan
          nicht erfasst → 6 Tests hingen auf Spring's
          Default-404. Fix: Top-Level
          `ThrowingTestController` im Test-Source-Set
    - **Verifikation:** Alle 130 Tests grün, manuelle
      Code-Reviews,
      Verständnisprüfung durch Nachfragen.