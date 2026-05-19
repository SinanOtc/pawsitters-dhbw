# KI-Nutzung im Projekt

Pflicht-Dokumentation laut Projektbeschreibung (DHBW Heilbronn, Pawsitters).

## Verwendete Tools

| Tool | Modell / Version | Eingesetzt von |
|---|---|---|
| Claude Code | Sonnet 4.6 / Opus 4.7 | Sinan, Vincent, Kevin |
| OpenAI Codex | GPT-5 | Kevin |
| GitHub Copilot | (PR-Review-Suggestions) | Sinan |

## Format pro Einsatz

**Zweck → Prompt → Ergebnis & Anpassung → Eigenanteil**

Kevin pflegt seine Prompts in einer eigenen Datei: [`KI_Prompts_Kevin.md`](KI_Prompts_Kevin.md).

---

## Sinan — Backend (Entities, Services, Controller, Tests)

### Spring Security Setup + Tests

- **Datum:** 26.04.2026
- **Tool:** Claude Opus 4.7
- **Prompts:**
  - Erstelle 3 Tests für Spring Security mit MockMvc
  - Erstelle eine simple `login.html`, `home.html` und `dashboard.html`
  - Vereinzelt für Code-Erklärungen bzw. Hinweise genutzt

### Domain-Entities (User, UserRole, OwnerProfile) + Validation-Tests

- **Datum:** 26.04. + 03.05.2026
- **Tool:** Claude Opus 4.7
- **Prompts:**
  - Beratung zur Aufteilung in `User` (Login-Daten) vs. `OwnerProfile` / `HostProfile` (Profildaten)
  - Beratung für sinnvolle `@Column(length = ...)` und `@Size(max = ...)` Werte (E-Mail RFC 5321 → 254, BCrypt-Hash → 60, Adressen → 255)
  - Empfehlungen für Unit-Tests mit `jakarta.validation.Validator`
- **Verifikation:** Alle Tests lokal mit `./mvnw test` ausgeführt und auf grün geprüft.

### PetOwner-Registrierung (Repository, Service, Controller, Templates, Tests)

- **Datum:** 04.05.2026
- **Tool:** Claude Opus 4.7
- **Prompts:**
  - Architektur-Beratung zur Schichtenaufteilung Repository / Service / Controller / Form / Template
  - Vorlagen für `JpaRepository`-Interfaces mit Query-Derivation (`findByEmail`, `findByUserId`, `existsByEmail`)
  - Controller-Skizze mit `@Valid`, `BindingResult`, `@AuthenticationPrincipal`, ModelAttribute-Naming
  - Form-DTOs als Java Records (`RegisterOwnerForm`, `UpdateOwnerForm`)
  - Mockito-basierte Service-Tests und `@WebMvcTest` Controller-Tests vollständig KI-generiert, danach aber manuell debuggt
  - Erklärung von Spring- oder SonarQube-Warnungen und ggf. Ausbesserung dieser
  - `templates/owner/register.html`, `profile.html`, `profile-edit.html` vollständig von KI generiert. Kleine manuelle Anpassungen
  - Testdokumentation von KI generiert
- **Verifikation:** Code wurde aktiv mitgestaltet. Verständnis durch Nachfragen und Erklärungen sichergestellt, alle Tests lokal mit `./mvnw test` grün.
- **!! Dieser Abschnitt wurde mit KI erstellt !!**

### Pet-Registrierung (Entity, Repository, Service, Controller, Tests)

- **Datum:** 06.05. + 10.05.2026
- **Tool:** Claude Opus 4.7
- **Prompts:**
  - Architektur-Beratung zur Pet-Schichten (Entity 1:n zu `OwnerProfile`, Repository mit Owner-Filter, Service mit Zugehörigkeitsprüfung gegen URL-Manipulation)
  - `PetForm` als Java Record mit `@AssertTrue` für Chip-Konsistenz (`chipped` ↔ `chipNumber`)
  - `PetController`-Skizze für Liste / Anlegen / Bearbeiten / Löschen mit `mode`-basierter Form-View und `currentUserId(...)`-Helper
  - `PetServiceTest` mit Mockito (KI-generiert, manuell geprüft und dokumentiert)
  - `PetControllerTest` mit `@WebMvcTest` + `@WithMockUser` für Routing, Validation, Security-Redirect
  - Erklärung von SonarQube- / IntelliJ-Warnungen (`DataFlowIssue` bei `@NotNull`-null, `SameParameterValue`, S2637 duplicate-literal) und Suppress-Strategie
- **Verifikation:** Alle Tests lokal mit `./mvnw test` grün, manuelle Code-Reviews, Verständnisprüfung durch Nachfragen.

### CareRequest (Entity, Repository, Service, Controller, Tests)

- **Datum:** 11.05.2026
- **Tool:** Claude Opus 4.7
- **Prompts:**
  - Schichten-Plan für `CareRequest` analog zum Pet-Pattern: Entity 1:n zu Owner + 1:n zu Pet, Repository mit Owner-Filter, Service mit Re-Use der `petService.findByIdForOwner`-Security
  - `@AssertTrue isDateRangeValid()` als Cross-Field-Check für Enddatum nach Startdatum (sowohl in der Entity als auch im Form-Record)
  - `CareRequestForm` als Java Record mit `@NotNull` + `@Future` + Cross-Field-Validation
  - Bewusster Verzicht auf Edit / Delete-Routes — Status-Wechsel sind workflow-getrieben, nicht user-editierbar (vermeidet inkonsistente Zustände)
  - `CareRequestServiceTest` mit Mockito (auch Security-Edge-Case: fremdes Pet wirft `PetNotFoundException`)
  - `CareRequestControllerTest` mit `@WebMvcTest` für Routing, Validation, Auth-Redirect
  - Kritische Diskussion von Copilot-Review-Suggestions (Validation-Duplikation, Enterprise-Javadoc, Try-Catch in `@Transactional` → bewusst nicht übernommen)
- **Verifikation:** Alle Tests lokal mit `./mvnw test` grün, manuelle Code-Reviews, Verständnisprüfung durch Nachfragen.

### Host-Profil (Entity, Repository, Service, Controller, Forms, Tests)

- **Datum:** 12.05.2026
- **Tool:** Claude Opus 4.7
- **Prompts:**
  - Beratung: `HostProfile`-Entity mit `Set<PetSpecies>` als `@ElementCollection`, `@AssertTrue isAvailabilityRangeValid()` als Cross-Field-Check für Verfügbarkeitszeitraum, `@DecimalMin(inclusive=false)` für `pricePerWeek > 0`
  - `RegisterHostForm` (mit E-Mail / Passwort) vs. `UpdateHostForm` (ohne) als Java Records
  - `HostService` analog `OwnerService` — `existsByEmail` über alle Rollen, Passwort-Hashing vor DB-Schreibung, Schutzregel `UserRole.HOST` hardcoded
  - SecurityConfig-Erweiterung: `/host/register` zu `permitAll`, `/host/**` mit `ROLE_HOST`
  - `@WebMvcTest` Controller-Tests mit Multi-Select-Param-Binding für `acceptedSpecies`
  - Kritische Diskussion von IntelliJ-AI-Suggestions (Inline-Access-Control, Global-Exception-Handler, „E-Mail-Hint an Angreifer") — bewusst nicht übernommen, weil Registrierungs- vs. Login-Kontext unterschieden werden müssen
- **Verifikation:** Alle Tests lokal mit `./mvnw test` grün (28 Host-Tests), manuelle Code-Reviews, Verständnisprüfung durch Nachfragen.

### Offer / Matching-Logik (Entity, Repositories, Service, Controller, Tests)

- **Datum:** 13.05.2026
- **Tool:** Claude Opus 4.7
- **Prompts:**
  - Anforderungsanalyse Issue #7: Was bedeutet „passt zu Host"? Vier Filter abgeleitet (Status OPEN, Species ∈ `acceptedSpecies`, Datum in Verfügbarkeit, kein eigenes Offer schon vorhanden)
  - Architektur-Entscheidung: Ein `OfferController` für Host- und Owner-Routes (bewusster Pattern-Bruch, weil Offer per Definition zwei Parteien verbindet)
  - JPQL-`@Query` mit `NOT EXISTS`-Subquery für die Matching-Logik (Alternative Java-Filter besprochen, SQL-Variante gewählt für Skalierbarkeit und um genau diese Stelle gegen Bugs testbar zu machen)
  - Neuer Test-Typ `@DataJpaTest` eingeführt, um die JPQL gegen H2 zu verifizieren — Mockito allein kann SQL-Bugs nicht fangen
  - Re-Verify-Pattern in `OfferService.createOffer`: Eligibility-Check geht über `findMatchingForHost(...).contains(cr)` statt Bedingungen einzeln zu prüfen → Single Source of Truth, Query-Änderungen propagieren automatisch
  - `OfferNotEligibleException` als URL-Manipulation-Guard
  - `@WebMvcTest` mit gemischten Rollen (HOST + OWNER) — per-Test `@WithMockUser` statt Klassen-Annotation, beide UserRepository-Mocks in `@BeforeEach`
- **Verifikation:** Alle Tests lokal mit `./mvnw test` grün (25 neue Offer-Tests), manuelle Code-Reviews, Verständnisprüfung durch Nachfragen.

### Offer-Accept + Auto-Reject-Kaskade (#8 + #9 + #10 Teil 1)

- **Datum:** 13.05.2026
- **Tool:** Claude Opus 4.7
- **Prompts:**
  - Scope-Klärung: #8 + #9 + #10 Teil 1 (OPEN → MATCHED) zusammen in einem PR, #10 Teil 2 (MATCHED → CLOSED via Scheduled-Job) als Folge-PR getrennt
  - Service-Methode `OfferService.accept(offerId, ownerUserId)` als atomare Operation: Offer → ACCEPTED, CareRequest → MATCHED, Kaskade aller anderen PENDING-Offers derselben CareRequest auf REJECTED
  - Security: gleiche `OfferNotFoundException` für nicht-existent UND fremd-besessen (Owner-Mismatch) — kein Info-Leak welche Offers existieren
  - State-Guards: `OfferNotPendingException` für nicht-PENDING Offers UND nicht-OPEN CareRequests (Race-Condition-Schutz)
  - Kaskaden-Pattern: `findByCareRequestIdAndStatus(...PENDING)` laden, Filter `!o.getId().equals(offerId)` damit das anzunehmende Offer nicht selbst-rejected wird
  - Owner-Scoped Test-Setup mit Mockito + zwei zusätzliche Host-Profile für die Kaskaden-Verifikation (3 Offers, 1 ACCEPTED + 2 REJECTED)
- **Verifikation:** Alle 124 Tests grün, manuelle Code-Reviews, Verständnisprüfung durch Nachfragen.

### Global Exception Handler (#57)

- **Datum:** 14.05.2026
- **Tool:** Claude Opus 4.7
- **Prompts:**
  - Begründung warum `@ControllerAdvice` jetzt sinnvoll ist (5+ Stellen mit custom Exceptions, Vergleichsgruppe `Software-Engineering-Projekt-WI24A3` hat ein zentrales `ApiExceptionHandler`)
  - Mapping-Strategie: `*NotFoundException` → 404, State-Exceptions (`OfferNotPending`/`NotEligible`) → 409, sonstige RuntimeExceptions bewusst ungefangen → Spring-Default-500 damit echte Bugs sichtbar bleiben
  - Debug: `@ResponseStatus` auf `@ExceptionHandler` triggert intern `sendError()` → BasicErrorController übernimmt das Rendering und unsere View wird verworfen. Fix: `ModelAndView` mit explizitem `setStatus(...)`
  - Debug: Inner `@Controller`-Klassen werden von `@WebMvcTest` Component-Scan nicht erfasst → 6 Tests hingen auf Spring's Default-404. Fix: Top-Level `ThrowingTestController` im Test-Source-Set
- **Verifikation:** Alle 130 Tests grün, manuelle Code-Reviews, Verständnisprüfung durch Nachfragen.

### CurrentUserResolver Refactor (#58)

- **Datum:** 15.05.2026
- **Tool:** Claude Opus 4.7
- **Prompts:**
  - DRY-Analyse der `currentUserId(UserDetails)`-Helper-Methode die in 5 Controllern wortwörtlich kopiert war (Pet, CareRequest, Host, Offer, Owner — letzterer mit Variante `loadProfileByEmail(String)`)
  - Entscheidung für `@Component CurrentUserResolver` statt `HandlerMethodArgumentResolver` (zweiteres wäre Spring-idiomatischer, aber für ein Schulprojekt unnötiger Setup-Aufwand und schlechter zu erklären in der Verteidigung)
  - Ablage in `security/`-Package (neben `CustomUserDetailsService`) statt `web/controller/` — die Klasse löst Auth- / Principal-Belange auf, ist kein Web-Controller
  - Test-Strategie: 2 Unit-Tests (happy path + missing-user edge case) statt Integration. Mock `UserDetails` statt echtem Principal.
  - Test-Refactor: 5 Controller-Tests umgestellt von `@MockitoBean UserRepository` auf `@MockitoBean CurrentUserResolver`. Bei `OfferControllerTest` mit zwei Rollen mussten `argThat`-Matcher verwendet werden, um anhand des Principal-Usernames auf die jeweilige UserID zu mappen.
- **Verifikation:** Alle 132 Tests grün (130 alte + 2 neue für `CurrentUserResolverTest`), `mvn test` lokal durchgelaufen und selbstständige Recherche zu dem Thema.

---

## Kevin — Frontend (Templates, Styling)

Siehe [`KI_Prompts_Kevin.md`](KI_Prompts_Kevin.md). Inhalte umfassen u. a. Basis-Layout & Navigation (#21), Profil-Seiten (#22, #23), Pet- und CareRequest-Formulare (#24, #25), Host- und PetOwner-Dashboards (#26, #27), Status-Anzeige (#28), Landing Page (#30), Design-System-Refactor (cream + terracotta), zweispaltiger Landing-Hero, Host-Map mit Leaflet/OSM und Host-Dashboard-Statistiken.

---

## Vincent — Planung & Doku

### 2026-05-10 — Initiales Issue- und Kanban-Setup

- **Zweck:** Aufgaben aus der Projektbeschreibung in GitHub-Issues überführen, bestehende Issues den Teammitgliedern zuweisen und ergänzende Issues erstellen.
- **Prompt (sinngemäß):** Aufgaben aus der Projektbeschreibungs-PDF extrahieren, in einem Kanban-Board darstellen und nach Backend (Sinan), Frontend (Kevin), Planung / Doku (Vincent) zuweisen. Bestehende Issues passend einsortieren, fehlende Issues neu anlegen.
- **Ergebnis & Anpassung:** 14 bestehende Issues mit Labels und Assignees versehen, 24 neue Issues erstellt (10 Frontend, 10 Planung / Doku, 4 Backend-Ergänzungen). Labels `frontend`, `backend`, `testing`, `planning`, `concept` neu eingeführt.
- **Eigenanteil:** Auswahl der Aufgabenverteilung, Review der erstellten Issues, finale Entscheidung über Scope.

### 2026-05-10 — Projekt-Regeln und CONTRIBUTING.md

- **Zweck:** Schlanken Regelkatalog für die Zusammenarbeit erstellen.
- **Prompt (sinngemäß):** Vorschlag für die wichtigsten Regeln im Projekt; anschließend auf 7 Kern-Regeln reduzieren und in `CONTRIBUTING.md` festhalten.
- **Ergebnis & Anpassung:** Erstentwurf hatte zu viele Regeln, wurde auf 7 gekürzt. Eine Regel (ursprünglich „Reviewer rotieren") manuell durch „Klein und früh committen" ersetzt, weil das direkt zum Bewertungskriterium „sichtbare Beiträge aller Mitglieder" passt.
- **Eigenanteil:** Auswahl der finalen Regeln, Anpassung an unsere Aufteilung.

### 2026-05-11 — Use-Case-Diagramm

- **Zweck:** Issue #37 umsetzen — Use-Case-Diagramm mit Akteuren und fachlichen Use Cases als Mermaid.
- **Prompt (sinngemäß):** Use-Case-Diagramm in Mermaid für Pawsitters bauen, gruppiert nach Profil-Management, Pet & Anfrage, Matching & Angebote. „include"-Beziehungen modellieren (Annehmen triggert Auto-Ablehnung + Status-Update).
- **Ergebnis & Anpassung:** `docs/use-case-diagram.md` mit Mermaid-Flowchart, Akteur-Tabelle, Use-Case-Beschreibungs-Tabelle und Issue-Verknüpfungen. Bei der ersten Version waren UC-Nummern (UC1-13) und Issue-Refs noch nicht abgeglichen — manuell mit aktueller Issue-Liste verifiziert.
- **Eigenanteil:** Akteur-Identifikation (PetOwner, Host, System), Use-Case-Schnitt entschieden, Verknüpfung zu Issues manuell geprüft.

### 2026-05-11 — Anforderungsdokument

- **Zweck:** Issue #36 umsetzen — `docs/requirements.md` mit funktionalen / nicht-funktionalen Anforderungen und User Stories.
- **Prompt (sinngemäß):** Anforderungsdokument schreiben auf Basis der Projektbeschreibungs-PDF mit FR-1 bis FR-10, NFR-1 bis NFR-11, User Stories, Out-of-Scope und Glossar.
- **Ergebnis & Anpassung:** Initialer PR offen auf Branch `docs/requirements`. Tabellen-Format mit Issue-Verknüpfungen pro FR. Aufgrund späterer Iteration der Doku-Struktur in den Verbund-Refactor verschoben (siehe Eintrag „Doku-Komplett-Überarbeitung").
- **Eigenanteil:** Auswahl der NFR-Bereiche, Abgleich mit echtem Umsetzungsstand, Scope-Abgrenzung (Out-of-Scope).

### 2026-05-11 — 25 Erweiterungs-Issues (Bonus-Features)

- **Zweck:** Erweiterungs-Ideen-Liste in saubere, durchgeplante GitHub-Issues umsetzen, mit Labels und Priorisierung im Projekt-Board.
- **Prompt (sinngemäß):** Bestehende Issues analysieren, dann 25 Erweiterungs-Issues anlegen mit ausformulierten Akzeptanzkriterien, technischen Hinweisen, Cross-References. Labels (frontend / backend / security / nice-to-have) und Prioritäten (P1 für Bewertungs-Relevant, P2 für Wow-Features) im Project Board setzen.
- **Ergebnis & Anpassung:** 25 Issues (#117 – #141) angelegt mit thematischer Gruppierung: Kerngeschäft (Suchfilter, Mehrere Tiere, Tierarten-Felder, Notfallkontakt), Kommunikation (Chat, Status-Updates, Mail), Buchung (Verfügbarkeitskalender, Preisberechnung, Mock-Payment, Storno), Architektur & Tech (Microservices, Swagger, PostgreSQL, Docker, JaCoCo / Sonar), Security (Rollen, Bean Validation, OWASP, HTTPS), Kreative Wow-Features (Karten, Tier-Tagebuch, Trial Meeting, Badges, Dashboard). 4 Ideen wurden als bereits vorhanden identifiziert und übersprungen (#116 Bewertung, #43 Spring Security, #106 Rate-Limiting, #77 i18n).
- **Eigenanteil:** Priorisierung in P1 / P2 selbst entschieden (Kriterium: zählt für Bewertungs-Kategorie ja / nein), Cross-References zu existierenden Issues manuell gesetzt.

### 2026-05-19 — Doku-Komplett-Überarbeitung

- **Zweck:** Alle `.md`-Dateien als zusammenhängendes Doku-Paket überarbeiten, Inkonsistenzen entfernen, fehlende Inhalte ergänzen.
- **Prompt (sinngemäß):** Gesamtes Projekt + Code + Issues analysieren, dann alle `.md`-Dateien überarbeiten. Keine Erfindungen, alles aus Code / Git / Issue-Tracker belegbar. Klein und sauber committen.
- **Ergebnis & Anpassung:** Branch `docs/full-md-overhaul` mit einem Commit pro Datei:
  - `CONTRIBUTING.md` um „Definition of Done" und Review-Etikette erweitert
  - `README.md` Test-Zahlen vereinheitlicht (155 Tests, vorher 133+ / 142 inkonsistent)
  - `docs/ARCHITECTURE.md` Test-Zahl synchronisiert, unsichtbares Unicode-Artefakt (U+2060 Word Joiner) am Dateiende entfernt
  - `docs/use-case-diagram.md` Issue-Referenzen mit echtem Stand abgeglichen, Umsetzungs-Status pro Use Case ergänzt
  - `docs/SECURITY.md` Markdown-Formatierung repariert (führende Leerzeichen, fehlende Code-Block-Auszeichnungen, Heading-Hierarchie). Inhalt unverändert.
  - `docs/TEST_DOCUMENTATION.md` neu strukturiert: Test-Zahl auf 155 (vorher 142), fehlende Tests dokumentiert (`PawsittersApplicationTests.contextLoads`, `closeExpiredRequests_*`, `createOffer_withMessage_*`, gesamte `reject_*`-Familie, `getDetail_*`, `ownerProfileNotFound`, `emailAlreadyTaken`, `postRejectOffer_*`), Tabellen-Format normalisiert
  - `docs/KI_PROMPTS.md` Vincent-Sektion ausgebaut, Reflexion geschrieben, Sinan-Sektion nur Heading-Hierarchie geglättet
  - `docs/ENTWICKLUNGSPROZESS.md` von leerer Skelett-Datei zu vollständigem Dokument ausgebaut (Aufgabenverteilung mit echten Commit-Zahlen, Workflow, Git-Nutzung, Phasen-Reflexion)
- **Eigenanteil:** Vor der Überarbeitung Test-Lauf mit `./mvnw test` zur Verifikation der 155-Test-Zahl. Plan pro Datei vor der Umsetzung mit Team abgestimmt. Bei `ENTWICKLUNGSPROZESS.md` ausschließlich Inhalte verwendet, die sich aus Git-Log, Issue-Tracker oder echtem Code belegen lassen — keine erfundenen Fakten.

---

## Reflexion: KI in unserem Projekt

### Was die KI gut konnte

**Boilerplate beschleunigen.** Repository-Interfaces, DTO-Records, Service-Skelette und `@WebMvcTest`-Setup-Code waren in unter einer Minute fertig. Den Code-Anteil, der eigentlich Tippen statt Denken ist, hat die KI fast vollständig übernommen — und dabei konsistenter geschrieben als wir das per Hand getan hätten (gleiche Annotation-Reihenfolge, gleiche Test-Naming-Konventionen). Für ein 3er-Team mit Studienalltag ist das ein realer Geschwindigkeits-Multiplikator.

**Architektur-Sparring.** Die wertvollsten KI-Sessions waren keine Code-Generierung, sondern Diskussionen über Trade-offs: Microservices vs. Monolith, `HandlerMethodArgumentResolver` vs. einfacher `@Component`, JPQL `NOT EXISTS` vs. Java-Filter. Die KI hat Argumente strukturiert geliefert, wir haben entschieden. Das hat in der Architekturdokumentation die Begründungs-Tiefe ermöglicht, die per reiner Google-Recherche schwer erreichbar gewesen wäre.

**Dokumentation auf Niveau ziehen.** Tabellen-Formatierung, konsistente Heading-Struktur, Mermaid-Diagramme — alles Sachen, bei denen Menschen unter Zeitdruck schlampig werden. KI macht das geduldig und gleichbleibend.

### Was wir bewusst nicht übernommen haben

**Copilot-Review-Suggestions zur „Enterprise-Validierung".** Vorschläge wie „validiere noch mal im Service" oder „pack einen Try-Catch um den `@Transactional`-Aufruf" wurden bewusst abgelehnt. Erstens duplizieren sie Bean-Validation, die bereits am Form und an der Entity steht. Zweitens verschleiert ein Try-Catch in `@Transactional` das Spring-Rollback-Verhalten. Die KI optimiert manchmal in Richtung „mehr Defensiv-Code" — das ist nicht immer besseres Design.

**IntelliJ-AI-Hinweise zur „angreifer-freundlichen E-Mail-Hint".** Beim Login darf eine generische Fehlermeldung sinnvoll sein („E-Mail oder Passwort falsch"), beim Registrieren ist eine konkrete Meldung („E-Mail bereits vergeben") ein UX-Muss. Die KI hat das in einen Topf geworfen — wir haben Login- und Registrierungs-Kontext explizit unterschieden.

**Tests, die nichts testen.** Manche KI-generierten Tests asserten nur, dass Mocks aufgerufen wurden, ohne zu prüfen, was der Code mit dem Ergebnis macht. Diese haben wir entweder durch echte Verhaltens-Asserts ersetzt oder gelöscht.

### Risiken, die wir beobachtet haben

**Halluzinationen bei Spring-Boot-Spezifika.** „Verwende `@RequestMapping(produces = ...)` für CSRF-Tokens" — Vorschlag, der so nicht funktioniert. Wir mussten jeden API-Vorschlag der KI gegen die echte Spring-Doku oder einen Lauf der Tests prüfen, weil sich Spring-APIs zwischen Major-Versionen ändern und die KI nicht immer weiß, welche Version wir nutzen.

**Convergence zu „durchschnittlichem" Code.** Wenn man eine KI auf „mach das idiomatischer" loslässt, glättet sie alles auf den Stack-Overflow-Mainstream. Eigenwillige, aber gut begründete Entscheidungen (z. B. Form-Records direkt im Service statt Mapper) werden weg-vorgeschlagen. Wir haben gelernt, diese KI-Glättung zu erkennen und punktuell zu widerstehen.

**Plausible Sicherheits-Lücken.** Eine KI hat einmal vorgeschlagen, in `findByIdForOwner` direkt `findById(petId)` aufzurufen und „dann ja den Owner-Check in der View" zu machen. Das wäre eine horizontale Privilege Escalation gewesen. Solche Vorschläge sehen syntaktisch okay aus, lassen sich aber nur erkennen, wenn der Mensch das Owner-Scoping-Pattern verinnerlicht hat. Die KI kann nicht ersetzen, was der Entwickler verstehen muss.

### Einfluss auf Softwareentwicklung, Testing, Security

**Entwicklung:** Die Geschwindigkeit der ersten 80 % eines Features ist mit KI deutlich höher. Die letzten 20 % — Edge Cases, integriertes Verhalten, Code-Review — bleiben menschliche Arbeit. Das verschiebt das Verhältnis von „Tippen" zu „Denken": man tippt weniger, denkt mehr.

**Testing:** KI-generierte Tests waren eine gute Startbasis, aber wir mussten sie konsequent prüfen — nicht nur, ob sie grün laufen, sondern ob sie das Richtige verifizieren. „Tests sind grün" ist keine Sicherheit. Beispiel: Tests gegen Owner-Scoping mussten wir manuell entwerfen, weil die KI sie zu einfach gemacht hat.

**Security:** Bean-Validation, Owner-Scoping, CSRF-Schutz — die Pattern verstehen wir aus dem Code, nicht aus dem Generieren. Die KI hat geholfen, sie konsistent umzusetzen, aber das Verständnis kam aus Diskussion und Eigenrecherche. Wir hätten ein gleichwertiges Sicherheits-Niveau auch ohne KI erreicht — die KI hat das Niveau nicht ersetzt, sondern beschleunigt.

### Fazit

KI war für uns ein Werkzeug, kein Autor. Sie hat Boilerplate beschleunigt, Diskussionen strukturiert und Doku-Konsistenz verbessert. Aber alle Architektur-, Sicherheits- und Domänen-Entscheidungen sind im Team gefallen, und jeder Code-Block auf `main` ist durchgegangen, weil mindestens eine Person ihn verstanden hat. Genau das verlangt die Aufgabenstellung — und genau das ist die Grenze, an der KI heute steht.
