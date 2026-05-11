# KI-Nutzung im Projekt

Diese Datei dokumentiert den Einsatz von KI-Tools im Pawsitters-Projekt – pflichtgemäß laut Projektbeschreibung.

## Verwendete Tools

| Tool | Modell / Version | Eingesetzt von |
|------|------------------|----------------|
| Claude Code | Sonnet 4.6 / Opus 4.7 | Vincent |
| _ggf. ergänzen_ |  |  |

## Vorgehen

Für jeden nennenswerten KI-Einsatz wird unten ein Eintrag erstellt mit:

- **Zweck** – was sollte erreicht werden
- **Prompt** – verwendeter Prompt (gekürzt möglich)
- **Ergebnis & Anpassung** – was wurde übernommen, was angepasst, was verworfen
- **Eigenanteil** – welche Teile wurden eigenständig erstellt oder nachbearbeitet

---

## Prompts

### 2026-05-10 – Initiales Issue- und Kanban-Setup

- **Zweck**: Aufgaben aus der Projektbeschreibung in GitHub-Issues überführen, bestehende Issues den Teammitgliedern zuweisen und ergänzende Issues erstellen.
- **Prompt** (sinngemäß): Aufgaben aus der Projektbeschreibungs-PDF extrahieren, in einem Kanban-Board darstellen und nach Backend (Sinan), Frontend (Kevin), Planung/Doku (Vincent) zuweisen. Bestehende Issues passend einsortieren, fehlende Issues neu anlegen.
- **Ergebnis & Anpassung**: 14 bestehende Issues mit Labels und Assignees versehen, 24 neue Issues erstellt (10 Frontend, 10 Planung/Doku, 4 Backend-Ergänzungen). Labels `frontend`, `backend`, `testing`, `planning`, `concept` neu eingeführt.
- **Eigenanteil**: Auswahl der Aufgabenverteilung, Review der erstellten Issues, finale Entscheidung über Scope.

### 2026-05-10 – Projekt-Regeln und CONTRIBUTING.md

- **Zweck**: Schlanken Regelkatalog für die Zusammenarbeit erstellen.
- **Prompt** (sinngemäß): Vorschlag für die wichtigsten Regeln im Projekt; anschließend auf 7 Kern-Regeln reduzieren und in `CONTRIBUTING.md` festhalten.
- **Ergebnis & Anpassung**: Erstentwurf hatte zu viele Regeln, wurde auf 7 gekürzt. Eine Regel manuell ersetzt.
- **Eigenanteil**: Auswahl der finalen Regeln, Anpassung an unsere Aufteilung.

---

## Reflexion (wird vor Abgabe ergänzt)

> Hier folgt eine Reflexion über den Einfluss von KI auf Softwareentwicklung, Testing und Security im Projekt sowie über Risiken (z. B. blindes Übernehmen, Halluzinationen, Sicherheitslücken in generiertem Code).
