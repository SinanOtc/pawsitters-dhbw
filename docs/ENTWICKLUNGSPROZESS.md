# Entwicklungsprozess

Dieses Dokument beschreibt, wie wir Pawsitters als 3er-Team organisiert und entwickelt haben — von der Aufgabenverteilung über den Git-Workflow bis zur Reflexion der einzelnen Projektphasen.

## 1. Team & Aufgabenverteilung

Drei Studierende mit klar getrennten Schwerpunkten, aber durchlässigen Grenzen.

| Mitglied | Schwerpunkt | Verantwortung |
|---|---|---|
| **Sinan Oturucu** | Backend | Domain-Entities, Services, Controller, Persistence, Spring Security, Tests, Engineering-Excellence-Tooling (ArchUnit, JaCoCo, OWASP) |
| **Kevin Christian Albrandt** | Frontend | Thymeleaf-Templates, CSS / Design-System, UX, Host-Map (Leaflet), Dashboard-UI |
| **Vincent Roland Scheibe** | Planung, Doku, Konzepte | Issues / Kanban, Architektur-, Test-, Security-, KI-Doku, Anforderungs- und Use-Case-Spezifikation, Doku-Konsistenz |

**Sichtbarkeit der Beiträge (Stand 19.05.2026, `git shortlog -sne`):**

| Mitglied | Commits | Bemerkung |
|---|---|---|
| Sinan | 208 | Größtenteils Backend-Features + Engineering-Excellence-Refactors |
| Kevin | 115 | Frontend-Features + Design-System-Refactor (10 separate Commits) |
| Vincent | 25 | Doku, Issue-Management, Konzept-Dokumente |

Die deutlichen Unterschiede in den Commit-Zahlen spiegeln die Rollenaufteilung wider: Code-tragende Rollen (Backend / Frontend) produzieren naturgemäß mehr Commits als die Planungs- und Doku-Rolle, die zusätzlich über Issue-Tracker, Project-Board und Diagramme arbeitet — Beiträge, die nicht alle als Commits sichtbar sind.

**Cross-Reviews als Verbindlichkeit.** Trotz der Spezialisierung hat jedes Teammitglied Code aller anderen mindestens einmal reviewt, weil die Aufgabenstellung verlangt, dass jeder den gesamten Code erklären können muss. Reviews liefen über GitHub-PRs (siehe Abschnitt 4).

## 2. Entwicklungsansatz: Kanban über GitHub Projects

Wir haben einen **Kanban-Ansatz** gewählt, kein klassisches Sprint-Modell. Begründung:

- Drei Studierende mit eigenen Stundenplänen und unregelmäßigen Verfügbarkeiten — fester Sprint-Rhythmus hätte mehr Reibung als Nutzen gebracht.
- Aufgaben sind unterschiedlich groß (UI-Feature vs. Architektur-Refactor) — ein Pull-Modell mit klaren WIP-Limits funktioniert besser als „alles in zwei Wochen committen".
- GitHub-Project-Board direkt ans Repo gekoppelt, keine separate Tooling-Burg.

**Board-Struktur:**

| Spalte | Bedeutung |
|---|---|
| Backlog | Issue ist erfasst, aber noch nicht eingeplant |
| In progress | Aktuell von genau einer Person bearbeitet |
| In review | Code fertig, PR offen, wartet auf Review |
| Done | Issue geschlossen (i. d. R. via PR-Merge) |

**Felder pro Issue:** Status, Assignees, Labels (z. B. `frontend`, `backend`, `security`), Priority (P0 / P1 / P2), Size (XS – XL). Priorität und Size werden bei Issues mit > XS-Aufwand gepflegt.

**Informelles WIP-Limit:** maximal ein bis zwei „In progress"-Issues pro Person, damit kein Issue lange parallel liegen bleibt.

## 3. Workflow pro Aufgabe

Jede Änderung — Code oder Doku — durchläuft den gleichen Pfad:

```
Issue → Branch → Commits → PR → Review → CI → Squash-Merge → Done
```

**Schritte im Detail:**

1. **Issue auswählen** aus dem Backlog. Falls noch kein Issue existiert, wird eines angelegt — Arbeit ohne zugehöriges Issue gibt es nicht (CONTRIBUTING-Regel #2).
2. **Branch erstellen** mit sprechendem Namen nach Schema `feature/<kurzbeschreibung>`, `fix/<kurzbeschreibung>` oder `docs/<kurzbeschreibung>`.
3. **Kleine Commits** mit sprechenden Messages und Präfix (`feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `chore:`). Bei zusammenhängenden Schritten ein Commit pro Schritt, nicht ein Sammel-Commit am Ende (CONTRIBUTING-Regel #6).
4. **PR öffnen** mit Verweis auf das Issue (`Closes #X`).
5. **Review abwarten** — mindestens eine Person aus dem Team muss zustimmen. Branch-Protection auf `main` erzwingt das auf Repo-Ebene.
6. **CI muss grün sein** — Tests, ArchUnit, Coverage-Gate (siehe Abschnitt 6).
7. **Squash-Merge nach `main`** — die Historie auf `main` bekommt einen sauberen Commit pro PR, der den Issue-Titel widerspiegelt.
8. **Issue wird automatisch auf „Done"** verschoben (Project-Board-Automation reagiert auf den PR-Merge).

Die vollständige **Definition of Done** ist in [`CONTRIBUTING.md`](../CONTRIBUTING.md) dokumentiert.

## 4. Git-Nutzung

### Branch-Strategie

Trunk-basiert: `main` ist immer deploy-fähig. Feature-Branches sind kurzlebig (üblicherweise < 1 Tag), werden nach Merge gelöscht.

### Branch-Schutz

Auf `main` sind folgende Regeln aktiv:

- Direkter Push ist nicht erlaubt (auch nicht für Admins, außer manuell deaktiviert).
- Pull Request mit mindestens einem zustimmenden Review wird verlangt.
- CI-Checks müssen passieren, bevor gemerged werden kann.

In einem Fall hat Branch-Protection einen Doku-Merge blockiert, obwohl alle Beteiligten zustimmten — die Regel hat genau das verhindert, wofür sie da ist (siehe `docs/use-case-diagram` PR).

### Commit-Konventionen

Format: `<typ>: <kurze beschreibung in der gegenwart>` — z. B. `feat: add reject endpoint for pending offers`. Body optional für Kontext.

Verwendete Präfixe:

- `feat:` — neue Funktionalität (Code oder UI)
- `fix:` — Bug-Fix
- `docs:` — reine Doku-Änderung
- `test:` — neue oder geänderte Tests
- `refactor:` — Umstrukturierung ohne Verhaltensänderung
- `chore:` — Build-Config, Dependencies, Tooling

### Squash-Merges

Jeder PR wird per Squash-Merge in `main` integriert. Vorteile:

- Die History auf `main` ist eine lineare Folge von „eine Feature pro Commit", nicht ein Wust aus Zwischen-Commits.
- `git blame` zeigt immer den fachlich relevanten Commit, nicht „fix typo".
- Reverten eines Features ist trivial (`git revert` auf einen Commit).

Auf den Feature-Branches selbst committen wir hingegen klein und früh — die Squash-Verdichtung passiert erst beim Merge.

## 5. Code-Reviews

**Bedeutung im Schul-Kontext:** Die Aufgabenstellung verlangt, dass jedes Teammitglied den gesamten Code erklären können muss. Reviews sind unser Mechanismus, der diese Anforderung absichert. Eine Review ist erst dann sinnvoll, wenn der Reviewer den Code wirklich versteht, nicht nur „LGTM" klickt.

**Praxis:**

- Reviewer wechseln, niemand reviewt ausschließlich die eigene Domäne.
- Konstruktive Kommentare mit Begründung — Vorschläge statt Befehlen.
- Diskussionen finden im PR statt und bleiben dort dokumentiert.
- Bei Backend-PRs hat Kevin oft Frontend-Implikationen kommentiert (z. B. „der neue Endpoint braucht noch eine Anzeige, ich mache ein Folge-Issue auf").
- Bei Doku-PRs hat das Team auf inhaltliche Genauigkeit geprüft, nicht nur auf Format.

**Konkrete Beispiele:**

- Kritische Diskussion von Copilot-Review-Suggestions in Sinans Service-PRs (Validation-Duplikation, Enterprise-Javadoc, Try-Catch in `@Transactional`) — bewusst nicht übernommen, weil sie das Design verschlechtert hätten.
- Bei Kevins Design-System-PR mit 10 Einzel-Commits konnten andere Teammitglieder pro Commit nachvollziehen, welche CSS-Tokens warum geändert wurden — saubere Commit-Trennung als Review-Hilfe.
- Sinans Refactor `CurrentUserResolver` (#58) wurde als „könnte auch ein `HandlerMethodArgumentResolver` sein" reviewt. Die Entscheidung gegen die Spring-idiomatische Variante ist im PR-Kommentar dokumentiert.

## 6. CI / CD-Pipeline

Wir nutzen **GitHub Actions** als CI. Jeder Push (auf einen Branch mit aktivem PR oder auf `main`) löst die Pipeline aus.

**CI-Job: Build & Test**

```
1. Checkout
2. Java 21 (Temurin) installieren
3. Maven-Cache restaurieren
4. ./mvnw verify
   ├── Compile
   ├── Unit + Integration Tests (155 Tests)
   ├── ArchUnit-Regeln (9 Architektur-Tests)
   └── JaCoCo Coverage Report + Quality Gate
       (LINE ≥ 80 %, BRANCH ≥ 65 %)
5. Build-Info schreiben (Git-Hash, Build-Time)
```

Build-Gate scheitert, sobald ein Test rot ist, eine ArchUnit-Regel verletzt wird, oder die Coverage unter die Schwelle fällt. Damit ist die Architektur durchgesetzt (ArchUnit) UND die Test-Abdeckung gegen Regression abgesichert (JaCoCo).

**Manuell triggerbar:**

- `./mvnw dependency-check:check` — OWASP-CVE-Scan gegen die NVD. Wegen der Dauer (erster NVD-Download 30+ Minuten ohne API-Key) nicht in jedem Build, aber vor Releases.

**Deployment:**

Nach erfolgreichem Merge auf `main` triggert Railway automatisch einen Re-Deploy. Ablauf:

```
Push to main → Railway-Webhook → Multi-stage Docker-Build
              → Image-Push → Container-Restart → Live in ~3 Min
```

Health-Check über `/actuator/health` — Railway behält den alten Container, wenn der neue beim Start fehlschlägt (Zero-Downtime).

Live unter: <https://pawcation.up.railway.app>

## 7. Projektphasen

### Phase 1 — Fundament (Kalenderwoche 17 – 18 / April 2026)

- Repo angelegt, Maven + Spring Boot 4 aufgesetzt.
- CI-Pipeline mit GitHub Actions (#11).
- Spring Security mit Login, BCrypt, Session-Cookie.
- Erste Domain-Entities (`User`, `UserRole`, `OwnerProfile`).

### Phase 2 — Kern-Features (Kalenderwoche 18 – 19 / Mai 2026)

- PetOwner-Registrierung (#2): Repository, Service, Controller, Templates, Tests.
- Pet-Anlage (#4): mit `@AssertTrue isChipDataConsistent` für Cross-Field-Validation.
- CareRequest (#5): mit Datums-Range-Validation.
- Host-Profile (#3): mit Multi-Select für `acceptedSpecies`, Verfügbarkeitsfenster, Preis pro Woche.
- Offer / Matching (#7, #6): JPQL-Query mit `NOT EXISTS` für „passt zu Host", `@DataJpaTest` zur Verifikation.
- Offer-Accept mit Kaskaden-Reject (#8, #9, #10 Teil 1): atomare Service-Methode, andere PENDING-Offers werden REJECTED.

### Phase 3 — Engineering Excellence (Kalenderwoche 19 / Mai 2026)

Eigene „Engineering-Excellence-Phase" mit Tracking-Issue #80. Ziel: Production-Niveau ohne neue Features.

- `GlobalExceptionHandler` mit `@ControllerAdvice` (#57): saubere 404 / 409-Mappings statt nackten Stack-Traces.
- `CurrentUserResolver` Refactor (#58): DRY-Violation aufgelöst, die in 5 Controllern wortwörtlich kopiert war.
- ArchUnit (9 Regeln): Schicht-Trennung, Naming-Konventionen, Zyklenfreiheit als CI-erzwungene Tests. Hat einen versteckten `service ↔ web`-Zyklus aufgedeckt → Form-Records ins neutrale `dto`-Paket verschoben.
- JaCoCo Build-Gate: Coverage als Build-Schwelle, nicht nur als Bericht.
- OWASP Dependency-Check: 9 CVE-Findings (4 CRITICAL), durch Versions-Upgrades behoben, Suppressions mit Datum und Re-Review-Frist.
- Multi-stage Dockerfile, Non-root User, ~250 MB Final-Image.
- PostgreSQL-Prod-Profil + Flyway-Migrationen.

### Phase 4 — Design-System (Kalenderwoche 19 / Mai 2026)

10 Commits auf `feature/design-system` mit klarer Reihenfolge (Tokens → Fonts → Body → Buttons → Forms → Status-Pills → Tables → Feature-Cards → Stepper → Brand-Wordmark). Komplette Visual-Umstellung von blau-grau auf cream + terracotta, ohne ein einziges Template anzufassen — nur CSS.

### Phase 5 — Demo-Polish (Kalenderwoche 20 / Mai 2026)

- Reject-Endpoint für PENDING-Offers (#104) als getrennter Use Case neben Auto-Reject.
- CareRequest-Detail-Seite mit Status-Stepper (#105).
- Auto-Close-Scheduler: abgelaufene OPEN-Anfragen werden auf CLOSED gesetzt (#147).
- Confirm-Dialoge für destruktive Aktionen (Accept, Reject, Pet-Delete).
- Empty States mit prominenten CTAs.
- Host-Map auf der Owner-Offer-Seite mit Leaflet + OpenStreetMap.
- Host-Dashboard-Statistik-UI (Frontend-Vorbereitung, Backend-Aggregation als Follow-Up #144).

### Phase 6 — Doku-Finalisierung (Kalenderwoche 20 / Mai 2026)

- Architekturdokument (#13).
- Security-Konzept (#12) inkl. Shift-Security-Left (#35).
- Test-Dokumentation (#32).
- KI-Nutzungs-Dokumentation (#33).
- Use-Case-Diagramm (#37).
- Anforderungsdokument (#36).
- 25 Erweiterungs-Issues (#117 – #141) mit Priorisierung im Board.
- Doku-Komplett-Überarbeitung (`docs/full-md-overhaul`).
- Entwicklungsprozess-Dokument (dieses Dokument, #14).

## 8. Reflexion

### Was gut funktioniert hat

**Klare Rollen, durchlässige Grenzen.** Die Aufteilung Backend / Frontend / Doku hat Reibung minimiert — jeder wusste, was er primär baut. Gleichzeitig durfte jeder Kommentare zu fremden Bereichen geben, und das Team hat oft genug die Domäne gewechselt (Kevin hat im Frontend Backend-Issues für Sinan abgeleitet; Sinan hat in der Doku Korrekturen vorgeschlagen).

**Issues als Single Source of Truth.** Wir haben gelernt, dass „Arbeit ohne Issue" am Ende verlorenes Tracking ist. Die strikte Regel „kein Branch ohne Issue" hat den Project-Board-Status mit der Realität synchron gehalten.

**Branch-Protection als Sicherheitsnetz.** Mindestens zwei Mal hat die Regel „kein direkter Push auf main" einen schnellen, aber unreviewten Merge verhindert — beide Male war der zweite Blick wertvoll.

**ArchUnit als Architektur-Wächter.** Einen impliziten Paket-Zyklus zu finden, der bei reinem Code-Reading unsichtbar geblieben wäre, ist eine Fähigkeit, die wir uns nicht zugetraut hätten ohne das Tool. Architektur als Code statt als PDF.

**Kleines, häufiges Committen.** Die meisten PRs hatten 1 – 10 Commits, jeder mit klarer Verantwortung. Reviews waren dadurch leichter und Reverts wären notfalls chirurgisch möglich.

### Was wir anders machen würden

**Früher Branch-Protection einrichten.** In den ersten Tagen haben wir noch direkt auf `main` gepushed. Bei einem Hot-Fix gab das später ein paar inkonsistente Commits, die wir aufräumen mussten. Branch-Protection sollte ab Tag 1 stehen.

**Doku nicht ans Ende packen.** Zwar haben wir Architektur- und KI-Doku begleitend mitgeführt, aber die Test-Dokumentation und das Security-Konzept sind erst in Phase 6 entstanden. Bei einem realen Produkt-Projekt wären sie früher nötig gewesen. Lesson: Doku ist ein Artefakt jeder Phase, nicht eine Abschluss-Aufgabe.

**Vincents Commit-Anteil ist niedrig.** Mit 25 Commits gegenüber 208 / 115 ist das Verhältnis stark verzerrt, auch wenn die Rollenaufteilung das erklärt. In einer Wiederholung würden wir Doku-Arbeit häufiger als formale Commits markieren (z. B. „chore: pflege Kanban-Board" oder „docs: protokolliere Sprint-Status"), um die Beiträge sichtbarer zu machen. Aktuell sind viele Planungs-Aktivitäten im Project-Board sichtbar, aber nicht in Git.

**Mehr Integrationstests.** Wir haben 146 funktionale Tests, aber davon sind viele Mockito-basierte Unit-Tests. Ein paar mehr End-to-End-Tests (z. B. „Owner registriert → Pet anlegen → CareRequest → Host gibt Offer → Owner akzeptiert") wären Bonus gewesen — sind als Issue #42 im Backlog.

### Lessons Learned

**Kanban funktioniert für kleine Teams.** Mit drei Personen und durchlässigen Rollen ist ein Pull-Modell mit WIP-Limits präziser als ein Sprint-Modell mit Story-Points. Größere Teams würden mehr Struktur brauchen.

**Architektur in Code zwingt zur Disziplin.** ArchUnit-Regeln zu schreiben hat unsere Architektur-Doku-Aussagen sofort prüfbar gemacht. Eine Aussage, die nicht als Test formulierbar ist, ist häufig auch nicht präzise genug.

**KI ist Werkzeug, nicht Autor.** Wir haben KI-Tools (Claude Code, Copilot, Codex) intensiv genutzt, aber jeder Code-Block auf `main` ist durchgegangen, weil mindestens eine Person ihn verstanden hat. Die Aufgabenstellung verlangt das explizit — und es war der größte Schutz vor „plausibel aussehenden, aber falschen" KI-Vorschlägen (siehe Reflexion in [`KI_PROMPTS.md`](KI_PROMPTS.md)).

**Reviews schaffen geteilte Verantwortung.** Wer reviewt, übernimmt mit. Das verändert die Haltung gegenüber dem Code — niemand sieht den eigenen Beitrag isoliert, jeder weiß, dass auch jemand anderes ihn beim nächsten Bug-Report mit-debuggen muss.

**Live-Deploy stärkt das Team.** Die Demo unter <https://pawcation.up.railway.app> war für uns mehr als ein Bonus: jede Person konnte den eigenen Beitrag in Aktion sehen, ohne lokal alles aufzusetzen. Das hat die Motivation in den späten Phasen spürbar erhöht.
