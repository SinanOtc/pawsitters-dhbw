# Contributing – Pawsitters

Diese Regeln gelten für alle Beiträge zum Projekt. Sie sind bewusst kurz gehalten.

## Kern-Regeln

1. **Kein direkter Push auf `main`** – alle Änderungen laufen über Pull Requests mit mindestens einem Review.
2. **Jeder Branch und PR referenziert ein Issue** (`Closes #X`). Keine Arbeit ohne zugehöriges Issue im Kanban-Board.
3. **CI muss grün sein, bevor gemerged wird.** Keine kaputten Tests auf `main`.
4. **Jedes neue Feature bekommt mindestens einen Unit-Test.** Testing zählt 20 % der Note.
5. **Keine Secrets im Repo.** Passwörter, Tokens und `.env`-Dateien gehören in `.gitignore`.
6. **Klein und früh committen.** Lieber mehrere kleine, sprechende Commits als ein großer Sammel-Commit – das macht die Beiträge aller Mitglieder nachvollziehbar (Bewertungskriterium).
7. **KI-Einsatz sofort in `KI_PROMPTS.md` festhalten.** Rückwirkend lässt sich das nicht mehr rekonstruieren.

## Definition of Done

Ein Issue gilt erst als „Done", wenn alle folgenden Punkte erfüllt sind:

- [ ] Code ist auf einem Feature-Branch implementiert
- [ ] Tests sind ergänzt, alle laufen lokal grün (`./mvnw test`)
- [ ] CI ist auf dem PR grün (Tests + Coverage-Gate + ArchUnit + OWASP)
- [ ] PR wurde von mindestens einer Person aus dem Team reviewt
- [ ] PR wurde via Squash-Merge in `main` integriert
- [ ] Zugehöriges Issue ist im Kanban-Board automatisch auf „Done" verschoben

Für reine Doku-Änderungen entfällt der Test-Punkt.

## Review-Etikette

- Reviews bewusst zeitnah (idealerweise innerhalb von 24 h), damit niemand blockiert ist.
- Reviewer kommentiert konstruktiv und konkret — Vorschläge mit Begründung, nicht nur „bitte ändern".
- Autor:in darf widersprechen und diskutieren — eine Review ist ein Gespräch, kein Befehl.
- Nach Merge: kurzer Blick auf den `main`-CI-Run, um sicherzustellen, dass nichts kaputt ist.

## Verantwortlichkeiten

- **Sinan Oturucu** – Backend (Entities, Services, Controller, Tests, Security)
- **Kevin Christian Albrandt** – Frontend (Thymeleaf-Templates, Styling, Design-System)
- **Vincent Roland Scheibe** – Planung, Dokumentation, Konzepte

## Branch- und Commit-Konventionen

- Branch-Namen: `feature/<kurzbeschreibung>`, `fix/<kurzbeschreibung>`, `docs/<kurzbeschreibung>`
- Commit-Präfixe: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `chore:`
- Commit-Messages auf Englisch oder Deutsch — Hauptsache, sprechend
