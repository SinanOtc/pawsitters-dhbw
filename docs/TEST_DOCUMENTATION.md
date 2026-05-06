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

## OwnerServiceTest
Datei: `src/test/java/dhbw/heilbronn/pawsitters/service/OwnerServiceTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `register_validForm_savesUserAndProfile` | Registrierung mit gültigem Form | User und OwnerProfile werden gespeichert | Normal |
| `register_passwordIsHashed_neverStoresPlaintext` | Klartext-Passwort darf nicht in der Entity landen | User bekommt nur den BCrypt-Hash, nicht das Klartext-Passwort | Edge Case (Security) |
| `register_userRoleIsOwner` | Rollen-Schutzregel bei der Registrierung | User wird mit Rolle `OWNER` gespeichert (nie HOST) | Edge Case (Privilege-Schutz) |
| `register_emailAlreadyTaken_throwsException` | Doppelte E-Mail bei der Registrierung | `EmailAlreadyTakenException`, nichts wird gespeichert | Edge Case (Duplikat) |
| `findByUserId_existing_returnsProfile` | Profil zu existierender User-ID | Korrektes OwnerProfile wird zurückgegeben | Normal |
| `findByUserId_notFound_throwsIllegalStateException` | Profil zu nicht-existierender User-ID | `IllegalStateException` (Daten-Konsistenzfehler) | Edge Case |
| `update_existingProfile_updatesAllFields` | Update der änderbaren Felder | firstName, lastName, address werden aktualisiert | Normal |

## OwnerControllerTest
Datei: `src/test/java/dhbw/heilbronn/pawsitters/web/controller/OwnerControllerTest.java`

Integrationstest mit `@WebMvcTest`, läuft gegen MockMvc + echte SecurityConfig.

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `getRegister_returnsRegisterView` | GET `/owner/register` | HTTP 200, View `owner/register`, leeres `registerForm` im Model | Normal |
| `postRegister_validForm_redirectsToLoginWithFlag` | POST `/owner/register` mit gültigem Form | HTTP 302 Redirect zu `/login?registered`, `OwnerService.register` aufgerufen | Normal |
| `postRegister_invalidEmail_returnsRegisterWithErrors` | POST mit ungültiger E-Mail | HTTP 200, View `owner/register`, Field-Error auf `email`, Service NICHT aufgerufen | Edge Case (Validation) |
| `postRegister_emailAlreadyTaken_showsFieldError` | POST mit bereits vergebener E-Mail | HTTP 200, View `owner/register`, Field-Error auf `email` | Edge Case (Duplikat) |
