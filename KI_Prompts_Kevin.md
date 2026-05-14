# KI-Nutzung im Projekt - Kevin

Diese Datei dokumentiert meinen KI-Einsatz im Pawsitters-Projekt ab 2026-05-12.

## Verwendete Tools

| Tool | Modell / Version | Eingesetzt von |
|------|------------------|----------------|
| OpenAI Codex | GPT-5 | Kevin |
| Claude Code | Sonnet / Opus | Kevin |

## Vorgehen

Für jeden nennenswerten KI-Einsatz wird unten ein Eintrag erstellt mit:

- **Zweck** - was sollte erreicht werden
- **Prompt** - verwendeter Prompt (gekürzt möglich)
- **Ergebnis & Anpassung** - was wurde übernommen, was angepasst, was verworfen
- **Eigenanteil** - welche Teile wurden eigenständig erstellt oder nachbearbeitet

---

## Prompts

### 2026-05-12 - Frontend: Basis-Layout & Navigation

- **Zweck**: Issue #21 umsetzen: gemeinsames Layout-Template mit Header, Footer, Navbar und schlichtem Styling.
- **Prompt** (sinngemäß): Frontend-Issue #21 schrittweise umsetzen, damit zwischen den Änderungen kurze Commits möglich sind.
- **Ergebnis & Anpassung**: `layout.html` als Base-Layout mit Thymeleaf-Fragments erstellt, Navbar mit Links zu Login, Profil, Anfragen und Angeboten ergänzt, `style.css` für ein einfaches Grundstyling angelegt.
- **Eigenanteil**: Anforderungen aus dem Issue geprüft, Commit-Schritte entschieden und Commit-Messages selbst formuliert.

### 2026-05-12 - Seiten an gemeinsames Layout anbinden

- **Zweck**: Bestehende Seiten auf das gemeinsame Layout umstellen.
- **Prompt** (sinngemäß): Prüfen, ob das Issue bereits erfüllt ist, und falls nötig weitere Templates an das Layout anbinden.
- **Ergebnis & Anpassung**: `home.html`, `login.html`, `dashboard.html` sowie die vorhandenen Owner-Seiten auf das Layout umgestellt. Inline-Styles für Fehlermeldungen durch `.field-error` ersetzt.
- **Eigenanteil**: Akzeptanzkriterien abgeglichen und entschieden, alle vorhandenen Templates einzubeziehen.

### 2026-05-12 - Tests und lokaler Start

- **Zweck**: Änderungen prüfen und Anwendung lokal starten.
- **Prompt** (sinngemäß): Tests ausführen und die Seite starten.
- **Ergebnis & Anpassung**: `mvn test` lief nach erneuter Ausführung erfolgreich durch (64 Tests). Anwendung mit `mvn spring-boot:run` gestartet.
- **Eigenanteil**: Testergebnis geprüft und Seite lokal unter `http://localhost:8080` kontrolliert.

### 2026-05-12 - Frontend: PetOwner-Profil-Seite

- **Zweck**: Issue #22 umsetzen: Formular und Anzeige für das PetOwner-Profil verbessern.
- **Prompt** (sinngemäß): Mit Issue #22 weitermachen und die PetOwner-Profilseite im Frontend überarbeiten.
- **Ergebnis & Anpassung**: Registrierungs- und Bearbeitungsformular sowie die Profilanzeige im Frontend überarbeitet. Vorschläge mit Backend-/Controller-Änderungen wurden wieder verworfen, weil ich nur am Frontend arbeiten sollte; übernommen wurden nur die Änderungen, die zum tatsächlichen PR-Stand passen.
- **Eigenanteil**: Frontend-only Scope klargestellt, Änderungen geprüft und Testergebnis (`mvn test`, 92 Tests) kontrolliert.

### 2026-05-12 - Dokumentation der KI-Nutzung

- **Zweck**: Eigene KI-Nutzung für den Frontend-Teil dokumentieren.
- **Prompt** (sinngemäß): `KI_Prompts_Kevin.md` im Stil der vorhandenen KI-Dokumentation erstellen, aber mit eigenen Inhalten.
- **Ergebnis & Anpassung**: Datei erstellt und auf die heutigen KI-Einsätze reduziert.
- **Eigenanteil**: Inhalt geprüft und gekürzt.

### 2026-05-14 - Frontend: Host-Profil-Seite

- **Zweck**: Issue #23 umsetzen: Formular und Anzeige für das Gastgeber-Profil im Frontend erstellen.
- **Prompt** (sinngemäß): Mit dem Host-Profil-Issue weitermachen und die Änderungen in kleinen Commits umsetzen.
- **Ergebnis & Anpassung**: Host-Registrierungsformular, Profilanzeige und Bearbeitungsformular mit Thymeleaf umgesetzt. Mehrfachauswahl für Tierarten, Datepicker für Verfügbarkeit und Preisfeld wurden an die bestehenden Backend-Forms angebunden.
- **Eigenanteil**: Scope auf Frontend begrenzt, Commit-Schritte festgelegt und Akzeptanzkriterien gegen die vorhandenen Backend-Endpunkte geprüft.

---

## Reflexion

Die KI hat vor allem bei wiederholenden Template-Anpassungen, beim Abgleich mit den Akzeptanzkriterien und beim Testen geholfen. Wichtig war, die Änderungen selbst zu prüfen, die Commits klein zu halten und die Anwendung am Ende lokal zu starten.
