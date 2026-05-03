# Testdokumentation
## Übersicht

## SecurityConfigTest
Datei: `src/test/java/dhbw/heilbronn/pawsitters/config/SecurityConfigTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `homeIsPubliclyAccessible` | GET `/` ohne Login | HTTP 200 | Normal |
| `dashboardRedirectAnonymousUserToLogIn` | GET `/dashboard` ohne Login | HTTP 302 Redirect zu `/login` | Edge Case (Schutzregel) |
| `dashboardIsAccessibleForLoggedInUser` | GET `/dashboard` mit eingeloggtem User | HTTP 200 | Normal |
| `validLoginRedirectsToDashboard` | POST `/login` mit gültigen Credentials | HTTP 302 Redirect zu `/dashboard` | Normal |
| `invalidLoginRedirectsToLoginWithError` | POST `/login` mit ungültigem Passwort | HTTP 302 Redirect zu `/login?error` | Edge Case (Fehlerpfad) |

## UserTest
Datei: `src/test/java/dhbw/heilbronn/pawsitters/domain/UserTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `constructor_setsAllFields` | Konstruktor setzt email, passwordHashed, role korrekt | Alle Getter geben die übergebenen Werte zurück | Normal |
| `email_invalidFormat_failsValidation` | E-Mail ohne `@`-Zeichen wird validiert | Validation-Violation auf Feld `email` | Edge Case (ungültiges Format) |
| `email_blank_failsValidation` | Leere E-Mail wird validiert | Validation-Violation auf Feld `email` | Edge Case (leerer Pflichtwert) |
| `passwordHashed_blank_failsValidation` | Leerer Passwort-Hash wird validiert | Validation-Violation auf Feld `passwordHashed` | Edge Case (leerer Pflichtwert) |
| `role_null_failsValidation` | `null` als Rolle wird validiert | Validation-Violation auf Feld `role` | Edge Case (Pflichtfeld nicht gesetzt) |

## OwnerProfileTest
Datei: `src/test/java/dhbw/heilbronn/pawsitters/domain/OwnerProfileTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `constructor_setsAllFields` | Konstruktor setzt user, firstName, lastName, address korrekt | Alle Getter geben die übergebenen Werte zurück, `user` ist identisch | Normal |
| `firstName_blank_failsValidation` | Leerer Vorname wird validiert | Validation-Violation auf Feld `firstName` | Edge Case (leerer Pflichtwert) |
| `lastName_blank_failsValidation` | Leerer Nachname wird validiert | Validation-Violation auf Feld `lastName` | Edge Case (leerer Pflichtwert) |
| `address_blank_failsValidation` | Leere Adresse wird validiert | Validation-Violation auf Feld `address` | Edge Case (leerer Pflichtwert) |
| `firstName_tooLong_failsValidation` | Vorname mit 101 Zeichen (über `@Size(max=100)`) | Validation-Violation auf Feld `firstName` | Edge Case (Längen-Constraint) |

