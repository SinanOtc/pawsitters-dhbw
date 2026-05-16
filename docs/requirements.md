# Anforderungsdokument – Pawsitters

Dieses Dokument beschreibt die Anforderungen an die Pawsitters-Plattform. Es ist die fachliche Grundlage für Architektur, Implementierung und Tests.

## 1. Projektziel

Pawsitters ist eine Web-Plattform, die **Tierhalter**, die während einer Abwesenheit eine Betreuung für ihr Haustier suchen, mit **Gastgebern** zusammenbringt, die Tiere gegen Bezahlung aufnehmen.

Die Plattform muss eine einfache, funktionale Bedienung über eine Web-Oberfläche ermöglichen und den vollständigen Lebenszyklus einer Betreuungsanfrage abbilden – von der Anfrage über das Matching bis zur Annahme eines Angebots.

## 2. Stakeholder & Akteure

| Akteur | Rolle |
|--------|-------|
| **PetOwner (Tierhalter)** | Sucht Betreuung für sein Haustier während Abwesenheit. |
| **Host (Gastgeber)** | Bietet Betreuung gegen Bezahlung an. |
| **System** | Führt automatische Aktionen aus (Status-Updates, Auto-Ablehnung konkurrierender Angebote). |
| **Betreuer\:in / Dozentin** | Bewertet das Projektergebnis (kein technischer Nutzer der Plattform). |

Eine detaillierte Darstellung der Akteure und ihrer Interaktionen findet sich im [Use-Case-Diagramm](./use-case-diagram.md).

## 3. Funktionale Anforderungen

Die folgenden Anforderungen sind **Pflichtanforderungen** laut Projektbeschreibung.

| ID | Anforderung | Beschreibung | Issue |
|----|-------------|--------------|-------|
| FR-1 | PetOwner-Profil erstellen | Ein Tierhalter kann ein Profil mit seinen Stammdaten anlegen. | #2 (closed), #22 |
| FR-2 | Host-Profil erstellen | Ein Gastgeber kann ein Profil mit akzeptierten Tierarten, Verfügbarkeit und Preis pro Woche anlegen. | #3, #23 |
| FR-3 | Haustier registrieren | Ein PetOwner kann ein oder mehrere Haustiere mit Tierart und Details registrieren. | #4 (closed), #24 |
| FR-4 | Betreuungsanfrage erstellen | Ein PetOwner kann eine Anfrage für einen Zeitraum (Start/Ende) erstellen. | #5 (closed), #25 |
| FR-5 | Passende Angebote anzeigen | Hosts sehen Anfragen, die zu ihrem Profil passen; PetOwner sehen eingegangene Angebote zur Anfrage. | #6, #26, #27 |
| FR-6 | Angebot versenden | Ein Host kann ein Angebot zu einer passenden Anfrage versenden. | #7, #26 |
| FR-7 | Angebot annehmen | Ein PetOwner kann ein Angebot annehmen. Nur ein Angebot pro Anfrage darf angenommen werden. | #8, #27 |
| FR-8 | Andere Angebote automatisch ablehnen | Beim Annehmen eines Angebots werden alle anderen Angebote zur selben Anfrage automatisch abgelehnt. | #9 |
| FR-9 | Anfrage-Status aktualisieren | Der Status einer Anfrage (z. B. *offen*, *angeboten*, *angenommen*, *abgeschlossen*, *abgelehnt*) wird abhängig von Aktionen aktualisiert. | #10, #28 |
| FR-10 | Authentifizierung | Nutzer können sich am System anmelden, um Aktionen unter ihrer Rolle auszuführen. | #15, #29 |

## 4. Nicht-funktionale Anforderungen

| ID | Bereich | Anforderung |
|----|---------|-------------|
| NFR-1 | Technologie | Backend in **Java 21** mit **Spring Boot**, gebaut mit **Maven**. |
| NFR-2 | Persistenz | **H2** als eingebettete Datenbank für Entwicklung und Tests. |
| NFR-3 | UI | Einfache funktionale Web-Oberfläche mit **Thymeleaf** und HTML. Kein komplexes Design erforderlich. |
| NFR-4 | Architektur | Klar strukturiert nach **MVC- / Schichten-Architektur**. Entscheidung wird im [Architekturdokument](./architecture.md) begründet. |
| NFR-5 | Testing | **Mindestens 10 Unit Tests** mit **JUnit**. Dokumentation in [TEST_DOCUMENTATION.md](../TEST_DOCUMENTATION.md). Edge Cases sind zu berücksichtigen. |
| NFR-6 | CI | Eine **CI-Pipeline** führt Tests automatisch bei Commits/Pushes aus. |
| NFR-7 | Codequalität | Lesbarer, strukturierter Code; JavaDoc für wichtige Klassen und Methoden; Erklärung der Geschäftslogik und Edge Cases. |
| NFR-8 | Security-Konzept | Pflicht-Dokument zu sensiblen Daten, Risiken, Maßnahmen und „Shift Security Left". Siehe [Security-Konzept](./security.md). |
| NFR-9 | Versionskontrolle | Gemeinsames Git-Repository mit regelmäßigen Commits, Branches, nachvollziehbaren Beiträgen aller Teammitglieder. Regeln in [CONTRIBUTING.md](../CONTRIBUTING.md). |
| NFR-10 | KI-Transparenz | Jeder relevante KI-Einsatz wird in [KI_PROMPTS.md](../KI_PROMPTS.md) dokumentiert. |
| NFR-11 | Verständlichkeit | Jedes Teammitglied muss den gesamten Code erklären können (Prüfungsrelevant). |

## 5. User Stories

### PetOwner

- **US-1**: *Als Tierhalter möchte ich ein Profil anlegen, damit ich Anfragen erstellen kann.*
- **US-2**: *Als Tierhalter möchte ich mein Haustier registrieren, damit Gastgeber wissen, welches Tier sie betreuen.*
- **US-3**: *Als Tierhalter möchte ich eine Betreuungsanfrage für einen Zeitraum erstellen, damit Gastgeber mir Angebote machen können.*
- **US-4**: *Als Tierhalter möchte ich alle Angebote zu meiner Anfrage einsehen, damit ich vergleichen kann.*
- **US-5**: *Als Tierhalter möchte ich ein Angebot annehmen, damit die Betreuung verbindlich vereinbart ist.*
- **US-6**: *Als Tierhalter möchte ich, dass die übrigen Angebote automatisch abgelehnt werden, sobald ich eines angenommen habe, damit kein Missverständnis entsteht.*
- **US-7**: *Als Tierhalter möchte ich den aktuellen Status meiner Anfragen sehen, damit ich den Überblick behalte.*

### Host

- **US-8**: *Als Gastgeber möchte ich ein Profil mit Tierarten, Verfügbarkeit und Preis anlegen, damit passende Anfragen mir zugeordnet werden.*
- **US-9**: *Als Gastgeber möchte ich Anfragen sehen, die zu meinem Profil passen, damit ich gezielt Angebote machen kann.*
- **US-10**: *Als Gastgeber möchte ich ein Angebot zu einer Anfrage versenden, damit der Tierhalter mich auswählen kann.*

### Beide

- **US-11**: *Als Nutzer möchte ich mich am System anmelden, damit meine Daten geschützt sind und Aktionen mir zugeordnet werden.*

## 6. Out of Scope

Folgende Punkte sind im Rahmen der Projektarbeit **bewusst nicht enthalten**:

- Echte Zahlungsabwicklung (Stripe, PayPal, etc.)
- Mobile App
- Bewertungs- / Rating-System (optionale Erweiterung, kein Pflichtteil)
- Mehrsprachigkeit (i18n)
- Push-Benachrichtigungen / E-Mail-Versand
- Produktionsbetrieb (Hosting, Deployment, Skalierung)

## 7. Bonus-Anforderungen (optional)

Diese Anforderungen sind nicht verpflichtend, werden aber laut Aufgabenstellung mit Bonuspunkten bewertet.

| ID | Anforderung | Issue |
|----|-------------|-------|
| BR-1 | Integrationstests ergänzend zu Unit Tests. | #42 |
| BR-2 | Implementierte Security-Mechanismen (z. B. Spring Security, Passwort-Hashing, CSRF-Schutz). | #43 |
| BR-3 | Bild-URL für Haustiere (`pictureUrl`). | #20 |
| BR-4 | Seed-Daten für Demo und Tests. | #16 |

## 8. Glossar

| Begriff | Bedeutung |
|---------|-----------|
| **Pet** | Haustier eines PetOwners. |
| **PetOwner** | Tierhalter, nutzt die Plattform zur Suche nach Betreuung. |
| **Host** | Gastgeber, bietet Betreuung an. |
| **Request** | Betreuungsanfrage eines PetOwners (Zeitraum, Pet). |
| **Offer** | Angebot eines Hosts zu einer Request. |
| **Status** | Zustand einer Request (z. B. *offen*, *angenommen*, *abgeschlossen*). |
