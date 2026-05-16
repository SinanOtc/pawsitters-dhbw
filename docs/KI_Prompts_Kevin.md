# Kevin — Frontend (Templates, Styling)

KI-Nutzungsprotokoll für den Frontend-Teil des Pawsitters-Projekts.
Format: **Zweck → Prompt → Ergebnis & Anpassung → Eigenanteil**.

## Verwendete Tools

| Tool | Modell / Version |
|------|------------------|
| OpenAI Codex | GPT-5 |
| Claude Code | Sonnet 4.6 / Opus 4.7 |

---

## Prompts

### 2026-05-12 - Frontend: Basis-Layout & Navigation

- **Zweck**: Issue #21 umsetzen: gemeinsames Layout-Template mit Header, Footer, Navbar und schlichtem Styling.
- **Prompt** (sinngemäß): Bei unseren Seiten (Login, Home, Dashboard) wiederholen sich Header und Footer, das will ich nicht in jedem Template doppelt pflegen. Kannst du mir helfen, mit Thymeleaf-Fragments ein gemeinsames Layout zu bauen, das ich überall einbinden kann? In die Navbar gehören für den Anfang Login, Profil, Anfragen und Angebote. Bitte schrittweise, damit ich zwischendurch kleine Commits machen kann.
- **Ergebnis & Anpassung**: `layout.html` als Base-Layout mit Thymeleaf-Fragments erstellt, Navbar mit Links zu Login, Profil, Anfragen und Angeboten ergänzt, `style.css` für ein einfaches Grundstyling angelegt.
- **Eigenanteil**: Anforderungen aus dem Issue geprüft, Commit-Schritte entschieden und Commit-Messages selbst formuliert.

### 2026-05-12 - Seiten an gemeinsames Layout anbinden

- **Zweck**: Bestehende Seiten auf das gemeinsame Layout umstellen.
- **Prompt** (sinngemäß): Das Layout steht jetzt, aber damit der Aufwand was bringt, müssen die bestehenden Templates es auch wirklich verwenden. Schau bitte alle Templates durch und sag mir, welche noch einen eigenen Header/Footer haben oder gar nicht eingebunden sind. Außerdem habe ich im Login-Template Inline-CSS für Fehlermeldungen gesehen — das gehört doch besser in eine Klasse wie `.field-error` im CSS, oder?
- **Ergebnis & Anpassung**: `home.html`, `login.html`, `dashboard.html` sowie die vorhandenen Owner-Seiten auf das Layout umgestellt. Inline-Styles für Fehlermeldungen durch `.field-error` ersetzt.
- **Eigenanteil**: Akzeptanzkriterien abgeglichen und entschieden, alle vorhandenen Templates einzubeziehen.

### 2026-05-12 - Tests und lokaler Start

- **Zweck**: Änderungen prüfen und Anwendung lokal starten.
- **Prompt** (sinngemäß): Bevor ich das pushe, möchte ich sichergehen, dass ich mit meinen Template-Änderungen die Backend-Tests nicht aus Versehen zerschossen habe. Welcher Maven-Befehl führt die Tests aus, und wie starte ich die App lokal, damit ich die Seiten auch im Browser kontrollieren kann?
- **Ergebnis & Anpassung**: `mvn test` lief nach erneuter Ausführung erfolgreich durch (64 Tests). Anwendung mit `mvn spring-boot:run` gestartet.
- **Eigenanteil**: Testergebnis geprüft und Seite lokal unter `http://localhost:8080` kontrolliert.

### 2026-05-12 - Frontend: PetOwner-Profil-Seite

- **Zweck**: Issue #22 umsetzen: Formular und Anzeige für das PetOwner-Profil verbessern.
- **Prompt** (sinngemäß): Mein nächstes Issue ist #22 — der PetOwner soll sein Profil anzeigen und bearbeiten können. Mein Verständnis: Wir brauchen eine Anzeige-Seite (Name, E-Mail, Adresse) und eine Bearbeitungs-Seite. Macht es mehr Sinn, das in zwei getrennte Templates zu trennen, oder ein Template mit Modus-Wechsel zu bauen? Wichtig: Ich bin im Team nur fürs Frontend zuständig, falls dein Vorschlag etwas am Controller oder Service ändern würde, bitte raus damit.
- **Ergebnis & Anpassung**: Registrierungs- und Bearbeitungsformular sowie die Profilanzeige im Frontend überarbeitet. Vorschläge mit Backend-/Controller-Änderungen wurden wieder verworfen, weil ich nur am Frontend arbeiten sollte; übernommen wurden nur die Änderungen, die zum tatsächlichen PR-Stand passen.
- **Eigenanteil**: Frontend-only Scope klargestellt, Änderungen geprüft und Testergebnis (`mvn test`, 92 Tests) kontrolliert.

### 2026-05-12 - Dokumentation der KI-Nutzung

- **Zweck**: Eigene KI-Nutzung für den Frontend-Teil dokumentieren.
- **Prompt** (sinngemäß): Die Dozentin verlangt eine Datei `KI_PROMPTS.md`, in der wir unsere KI-Nutzung dokumentieren. Sinan hat schon einen Backend-Abschnitt angelegt — kannst du mir einen passenden Frontend-Abschnitt im gleichen Stil aufsetzen, in den ich meine heutigen Einsätze eintragen kann? Bitte kurz halten, ich will keinen Roman.
- **Ergebnis & Anpassung**: Datei erstellt und auf die heutigen KI-Einsätze reduziert.
- **Eigenanteil**: Inhalt geprüft und gekürzt.

### 2026-05-14 - Frontend: Host-Profil-Seite

- **Zweck**: Issue #23 umsetzen: Formular und Anzeige für das Gastgeber-Profil im Frontend erstellen.
- **Prompt** (sinngemäß): Das Host-Profil ist ein bisschen mehr als das Owner-Profil: Der Host muss angeben, welche Tierarten er aufnimmt (das können mehrere sein), in welchem Zeitraum er verfügbar ist und einen Wochenpreis. Wie baue ich in Thymeleaf eine Mehrfachauswahl mit Checkboxen, sodass Spring die Werte sauber an `acceptedSpecies` bindet? Und für die zwei Datumsfelder: Gibt es eine einfache Möglichkeit, dass das Bis-Datum nicht vor dem Ab-Datum liegen darf?
- **Ergebnis & Anpassung**: Host-Registrierungsformular, Profilanzeige und Bearbeitungsformular mit Thymeleaf umgesetzt. Mehrfachauswahl für Tierarten, Datepicker für Verfügbarkeit und Preisfeld wurden an die bestehenden Backend-Forms angebunden.
- **Eigenanteil**: Scope auf Frontend begrenzt, Commit-Schritte festgelegt und Akzeptanzkriterien gegen die vorhandenen Backend-Endpunkte geprüft.

### 2026-05-14 - Frontend: Pet-Registrierungs-Formular

- **Zweck**: Issue #24 umsetzen: Formular zur Registrierung eines Haustiers durch den Tierhalter erstellen.
- **Prompt** (sinngemäß): Issue #24 — Tierhalter sollen Haustiere anlegen können. Im Backend gibt es schon `PetForm` mit Name, Tierart, Geburtsjahr, Chip-Feldern und Impf-/Kastrationsstatus. Welche davon sollte ich im Formular zur Pflicht machen, welche dürfen optional bleiben? Tierart als Dropdown ist klar. Bei der Chip-Nummer könnte ich das Feld dynamisch ein-/ausblenden, je nachdem ob „gechippt" angehakt ist — oder reicht für den Anfang die Backend-Validierung?
- **Ergebnis & Anpassung**: Formular für Haustiere mit Name, Tierart-Dropdown, Geburtsjahr, Besonderheiten und den vorhandenen Backend-Feldern erstellt. Zusätzlich wurde eine einfache Haustier-Übersicht mit Link zum Registrierungsformular ergänzt.
- **Eigenanteil**: Bestehende Backend-Form geprüft, Pflichtfelder übernommen und die zusätzlichen Felder gegen den Issue-Scope abgewogen.

### 2026-05-15 - Frontend: Login- und Registrierungsseiten

- **Zweck**: Issue #29 umsetzen: Login-UI und Registrierung mit Rollenauswahl verbessern.
- **Prompt** (sinngemäß): Mir ist beim Durchklicken aufgefallen, dass der Einstieg für neue User noch unklar ist: Von der Login-Seite gibt es keinen sichtbaren Weg zur Registrierung, und neue User müssen ja erstmal entscheiden, ob sie als PetOwner oder Host anlegen. Wo macht die Rollenauswahl aus UX-Sicht mehr Sinn — direkt auf der Login-Seite oder schon auf der Startseite? Außerdem soll die Fehlermeldung bei falschem Passwort deutlicher hervorgehoben werden.
- **Ergebnis & Anpassung**: Login-Formular um klarere Fehlermeldung und Formularattribute ergänzt. Rollenauswahl für PetOwner und Host über die vorhandenen Registrierungsseiten eingebunden.
- **Eigenanteil**: Frontend-only Scope beachtet und keine neuen Backend-Routen erstellt.

### 2026-05-16 - Frontend: Betreuungsanfrage erstellen

- **Zweck**: Issue #25 umsetzen: Formular zur Erstellung einer Betreuungsanfrage durch den Tierhalter.
- **Prompt** (sinngemäß): Issue #25 — der Tierhalter soll für ein bestimmtes Haustier einen Betreuungs-Zeitraum anfragen. Backend-Form `CareRequestForm` (Pet-ID + Start-/End-Datum) gibt es schon. Im Frontend brauche ich eine Haustier-Auswahl (alle Haustiere des eingeloggten Owners als Dropdown) und zwei Datepicker. Nach dem erfolgreichen Absenden würde ich gerne eine Bestätigung anzeigen — geht das ohne Controller-Änderung, z.B. über ein Flash-Attribute oder einen Query-Param?
- **Ergebnis & Anpassung**: Formular mit Haustierauswahl und Datepickern erstellt, Frontend-Validierung für den Zeitraum ergänzt und eine Bestätigungsanzeige nach erfolgreicher Erstellung eingebunden.
- **Eigenanteil**: Bestehende Backend-Form geprüft, Scope auf Frontend gehalten und die Bestätigung ohne Controller-Änderung umgesetzt.

---

## Reflexion

Die KI hat vor allem bei wiederholenden Template-Anpassungen, beim Abgleich mit den Akzeptanzkriterien und beim Testen geholfen. Wichtig war, die Änderungen selbst zu prüfen, die Commits klein zu halten und die Anwendung am Ende lokal zu starten.
