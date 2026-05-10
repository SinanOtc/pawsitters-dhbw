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

## Verantwortlichkeiten

- **Sinan** – Backend
- **Kevin** – Frontend
- **Vincent** – Planung, Dokumentation, Konzepte

## Branch- und Commit-Konventionen

- Branch-Namen: `feature/issue-<nr>-kurzbeschreibung`, `fix/...`, `docs/...`
- Commit-Präfixe: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`
