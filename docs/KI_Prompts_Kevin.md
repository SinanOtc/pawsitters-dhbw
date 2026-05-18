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
- **Prompt** (sinngemäß): Issue #25 — der Tierhalter soll für ein bestimmtes Haustier einen Betreuungs-Zeitraum anfragen. Backend-Form `CareRequestForm` (Pet-ID + Start-/End-Datum) gibt es schon. Im Frontend brauche ich eine Haustier-Auswahl und zwei Datepicker. Bitte prüfe auch, ob eine Bestätigung nach erfolgreicher Erstellung robust ohne Controller-Änderung möglich ist.
- **Ergebnis & Anpassung**: Formular mit Haustierauswahl und Datepickern erstellt, Frontend-Validierung für den Zeitraum ergänzt und eine einfache Übersicht der Betreuungsanfragen eingebunden. Eine rein clientseitige Bestätigung wurde nach Review wieder entfernt, weil sie ohne Server-Feedback falsche Erfolgsmeldungen anzeigen kann.
- **Eigenanteil**: Bestehende Backend-Form geprüft, Scope auf Frontend gehalten und entschieden, dass eine robuste Bestätigung später mit Flash-Attribut oder Query-Param im Controller umgesetzt werden sollte.

### 2026-05-16 - Frontend: Host-Dashboard (passende Anfragen & Angebote)

- **Zweck**: Issue #26 umsetzen: Host-Seite mit Liste passender Anfragen, Form zum Angebot-Senden und Übersicht der eigenen gesendeten Angebote.
- **Prompt** (sinngemäß): Issue #26 — der Host soll alle Anfragen sehen, die zu seinem Profil passen, daraus eine auswählen, einen Wochenpreis eingeben und das Angebot abschicken. Außerdem soll er später sehen, was er bisher angeboten hat und in welchem Status. Backend ist schon da: `OfferController` liefert `matchingRequests`, `offerForm` und `offers` ins Model. Welche Felder zeige ich pro Anfrage sinnvoll (Tierart, Zeitraum, Tierhalter — siehe Akzeptanzkriterien) und wie verlinke ich zur Offer-Form? Im Issue steht auch „optionale Nachricht" — das `OfferForm` hat aber nur `weeklyPrice`, also lasse ich das raus, sonst müsste das Backend angepasst werden (nicht mein Scope).
- **Ergebnis & Anpassung**: Drei Templates ausgebaut: `host/care-requests/list.html` (Tabelle mit Haustier, Tierart, Zeitraum, Tierhalter + „Angebot senden"-Link), `host/care-requests/offer-form.html` (Preisfeld mit `btn-primary`-Submit) und `host/offers/list.html` (Tabelle mit Haustier, Zeitraum, Tierhalter, Preis, Status). Stil aus `pets/list.html` übernommen (Tabelle + `#lists.isEmpty`-Fallback).
- **Eigenanteil**: Akzeptanzkriterien gegen den Backend-Stand abgeglichen, „optionale Nachricht" bewusst rausgelassen weil Frontend-only Scope, Commit-Schritte pro Template festgelegt.

### 2026-05-17 - Frontend: PetOwner-Dashboard (Angebote annehmen)

- **Zweck**: Issue #27 umsetzen: Owner soll alle eingegangenen Angebote zu einer Anfrage sehen und annehmen können.
- **Prompt** (sinngemäß): Issue #27 — der Tierhalter braucht eine Übersicht, wer welches Angebot für seine Anfrage abgegeben hat. Akzeptanzkriterien: pro Angebot Host, Preis und Verfügbarkeit zeigen, Annehmen-/Ablehnen-Button und ein visuelles Feedback nach der Annahme. Backend hat schon `GET /owner/care-requests/{id}/offers` (Liste) und `POST /owner/offers/{offerId}/accept`. Reject-Endpoint gibt es nicht — Accept rejected die anderen Offers ja automatisch (siehe Sinans Auto-Reject-Kaskade). Ich würde den Reject-Button daher erstmal weglassen und ein Backend-Issue dafür anlegen, analog zur Nachricht in #26. Visuelles Feedback würde ich über Status-Badges pro Offer lösen (PENDING gelb, ACCEPTED grün, REJECTED grau), das geht ganz ohne Controller-Änderung.
- **Ergebnis & Anpassung**: `owner/care-requests/list.html` um „Angebote ansehen"-Spalte ergänzt, `owner/care-requests/offers.html` mit Tabelle (Host, Adresse, Verfügbarkeit, Preis, Status) gebaut, Annehmen-Form nur sichtbar bei PENDING-Offer + OPEN-CareRequest. Status-Badges `.status-pending/.status-accepted/.status-rejected` ins `style.css` ergänzt.
- **Eigenanteil**: Akzeptanzkriterien gegen Backend abgeglichen, fehlenden Reject-Endpoint als eigenes Backend-Issue ausgelagert, Badge-Farben passend zur bestehenden Farbpalette (`message-success`/`message-error`) gewählt, Commit-Schritte pro Datei festgelegt.

### 2026-05-17 - Frontend: Anfrage-Status-Anzeige

- **Zweck**: Issue #28 umsetzen: Status einer Betreuungsanfrage sichtbar machen — Badge pro Anfrage in der Übersicht und Detailseite mit Statusverlauf.
- **Prompt** (sinngemäß): Issue #28 — der Owner soll auf einen Blick sehen, wo seine Anfrage gerade steht. Akzeptanzkriterien: Status-Badge pro Anfrage in der Übersicht und eine Detailseite mit Statushistorie. Mein Gedanke: Eine echte Historie geht ohne Backend-Audit-Log und Timestamps gar nicht (`CareRequest` hat aktuell keine `createdAt`/`updatedAt`). Was Frontend-only realistisch ist: ein **visueller Stepper**, der die drei `RequestStatus`-Werte OPEN → MATCHED → CLOSED zeigt und den aktuellen Status hervorhebt. Für die Detailseite selbst fehlt aber ein `GET /owner/care-requests/{id}`-Endpoint — den lege ich als Backend-Issue für Sinan an. Das Template kann ich aber schon fertig bauen, weil es nur auf `${careRequest}` zugreift.
- **Ergebnis & Anpassung**: `owner/care-requests/list.html` zeigt den Status jetzt als farbiges Badge (gelb / grün / grau) statt als Plain-Text. Neue Detailseite `owner/care-requests/detail.html` mit Pet-Info, Datumstabelle, aktuellem Status-Badge und einem Stepper, der OPEN → Vermittelt → Geschlossen visualisiert. Erreichte Schritte werden grün, der aktuelle bekommt zusätzlich einen Glow. Haustier-Name in der Liste verlinkt jetzt auf die Detailseite. CSS-seitig wurden `.status-open/.status-matched/.status-closed` und der `.status-stepper`-Block ergänzt.
- **Eigenanteil**: Akzeptanzkriterien gegen Domain abgeglichen (keine echte Historie möglich → Stepper als Workaround), fehlenden Detail-Endpoint als Backend-Issue ausgelagert, Stepper-Logik (`step-active` vs. `step-current`) selbst durchgedacht, Farben wieder konsistent zur bestehenden Palette gewählt.

### 2026-05-17 - Frontend: Landing Page

- **Zweck**: Issue #30 umsetzen: Einstiegsseite mit kurzer Plattform-Erklärung und Call-to-Action zur Registrierung.
- **Prompt** (sinngemäß): Issue #30 — die aktuelle Startseite ist sehr karg (nur Headline + Login-Link + zwei Registrieren-Buttons). Das wirkt nicht wie eine richtige Landing Page. Mein Gedanke: Drei Sektionen wären sinnvoll — eine Hero mit klarer Headline und CTAs (Tierhalter / Gastgeber), zwei Feature-Kacheln nebeneinander (was kann ich als Tierhalter / als Gastgeber tun) und ein „So funktioniert's" mit drei Schritten (Registrieren → Anfragen oder Angebote → Vermittlung). Brauche kein Bildmaterial, nur Text + sauberes Layout. Farben sollen zur bestehenden Palette passen (helles Grau-Blau).
- **Ergebnis & Anpassung**: `home.html` komplett neu aufgebaut mit Hero-Sektion (Headline, Subtitle, zwei CTAs, Login-Hinweis darunter), zwei Feature-Cards für die Zielgruppen und einer nummerierten Step-Liste mit Kreis-Counter (`counter-reset` / `::before` mit `content: counter(...)`). CSS-seitig die Sektionen `.hero / .feature-grid / .landing-steps` ergänzt, auf Mobile-Breakpoint die Hero-Headline verkleinert.
- **Eigenanteil**: Inhaltliche Struktur (drei Sektionen) selbst entschieden, Texte für die Feature-Kacheln aus den bisherigen Issues abgeleitet (was der User auf der Plattform wirklich machen kann), Farbschema bewusst konsistent zum Rest der App gewählt.

### 2026-05-17 - Frontend: Datumsfelder gegen ungültige Bereiche absichern

- **Zweck**: Issue #96 (selbst angelegt) umsetzen: Im Host-Registrierungsformular und im Profil-Edit konnte man im Browser ein „Verfügbar ab" eingeben, das nach „Verfügbar bis" liegt. Backend-Validation fängt das zwar, aber UX-mäßig sollte der Browser das schon verhindern.
- **Prompt** (sinngemäß): Issue #96 — Browser soll bei den zwei Datumsfeldern (Verfügbar ab / bis bei Host, Start / End bei CareRequest) selbst verhindern, dass ein End-Datum vor dem Start-Datum gewählt wird. Es gibt schon ein `care-request.js`, das genau das für die CareRequest-Form macht. Da das gleiche Pattern jetzt an drei Stellen gebraucht wird (Host-Register, Host-Profil-Edit, CareRequest), wäre es sinnvoller, das Script zu generalisieren statt zu kopieren. Mein Gedanke: Container mit `data-date-range` markieren, Inputs mit `data-date-range-start` / `data-date-range-end`, plus ein optionales `data-date-range-min="today|tomorrow"` für den Unterschied zwischen @FutureOrPresent (Host) und @Future (CareRequest). So muss man pro Form nur drei Attribute setzen und das Script bedient sie automatisch.
- **Ergebnis & Anpassung**: `care-request.js` zu `date-range.js` generalisiert: Script findet alle `[data-date-range]`-Container, zieht das `min` des Endfelds dynamisch nach (Startwert + 1 Tag), zeigt Custom-Validity-Messages und unterscheidet via `data-date-range-min` ob heute oder morgen als frühestmögliches Datum gilt. CareRequest-Form, Host-Register und Host-Profil-Edit auf das neue Pattern umgestellt.
- **Eigenanteil**: Refactor-Entscheidung (generalisieren statt kopieren) selbst getroffen, Attribute-Naming durchdacht (`data-date-range-*` als sprechendes Pattern), Today-vs-Tomorrow-Unterscheidung als Konfigurationsoption durchgezogen, damit das eine Script beide Backend-Constraints abdeckt.

### 2026-05-18 - Frontend: Design-System (cream + terracotta)

- **Zweck**: Pawsitters-Design aus Claude Design (claude.ai/design) im Frontend umsetzen — kompletter Visual-Wechsel von der bisherigen blau-grauen Palette zu einem warmen Cream-Hintergrund mit Terracotta-Akzent, plus neue Schriftarten (Manrope/Fraunces).
- **Prompt** (sinngemäß): Ich habe ein Design-System aus Claude Design exportiert — kannst du das nach und nach im Projekt umsetzen? Bitte mit kleinen Commits, weil das ein größeres Refactor ist, und in einer logischen Reihenfolge (Foundations → Building Blocks → zusammengesetzte Komponenten → Brand). Bestehende Templates möglichst nicht anfassen, nur die CSS umbauen — sonst bricht zu viel auf einmal.
- **Ergebnis & Anpassung**: 10 kleine Commits auf `feature/design-system`: Tokens als `:root`-Block (Schritt 1), Google Fonts ins Layout (Schritt 2), Body + Headings auf Manrope/Fraunces (Schritt 3), Buttons zu Pill mit Terracotta (Schritt 4), Forms mit dickerem Border + Akzent-Fokus (Schritt 5), Status-Pills uppercase + Messages mit Border-Left (Schritt 6), Tabellen mit uppercase Headern (Schritt 7), Feature-Cards und Landing-Steps in größerer Radius mit Akzent-Counter (Schritt 8), Stepper auf Terracotta + Soft-Halo (Schritt 9), Brand-Wordmark zu Fraunces Italic (Schritt 10). Alle bestehenden Templates funktionieren ohne HTML-Änderung weiter.
- **Eigenanteil**: Commit-Reihenfolge bewusst nach Abhängigkeit sortiert (Pills vor Tables, weil Tables die Pills nutzen). Px-Werte aus dem Kit teilweise auf rem umgerechnet, damit die Codebase einheitlich bleibt. Brand-Wordmark moderater dimensioniert (1.75rem statt 38px), damit der Header nicht zu hoch wird. Optionale Komponenten aus dem Kit (Search-Row, Trustpilot, Stats-Bar) bewusst weggelassen, weil sie keinem aktuellen Akzeptanzkriterium dienen.

---

## Reflexion

Die KI hat vor allem bei wiederholenden Template-Anpassungen, beim Abgleich mit den Akzeptanzkriterien und beim Testen geholfen. Wichtig war, die Änderungen selbst zu prüfen, die Commits klein zu halten und die Anwendung am Ende lokal zu starten.
