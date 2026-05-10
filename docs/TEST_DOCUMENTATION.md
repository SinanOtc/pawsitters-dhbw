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

## PetServiceTest
Datei: `src/test/java/dhbw/heilbronn/pawsitters/service/PetServiceTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `register_validForm_savesPetWithOwner` | Pet-Anlage mit gültigem Form | Pet wird mit Owner-Referenz gespeichert | Normal |
| `register_appliesAllOptionalFields` | Optionale Felder werden übernommen | breed, birthYear, chip*, vaccinated, neutered, description gesetzt | Normal |
| `findAllByOwner_returnsOwnersPets` | Liste der Pets eines Owners | Repository liefert nur die Pets des Owners | Normal |
| `findByIdForOwner_existing_returnsPet` | Eigenes Pet per ID laden | Pet wird zurückgegeben | Normal |
| `findByIdForOwner_notOwnedByUser_throwsPetNotFound` | Fremdes Pet per URL-Manipulation | `PetNotFoundException` | Edge Case (Security) |
| `update_existingPet_updatesAllFields` | Update aller änderbaren Felder | Alle Felder am Pet aktualisiert | Normal |
| `delete_existingPet_callsRepositoryDelete` | Eigenes Pet löschen | `rep

## PetControllerTest
Datei: `src/test/java/dhbw/heilbronn/pawsitters/web/controller/PetControllerTest.java`

Integrationstest mit `@WebMvcTest` + `@WithMockUser(roles="OWNER")` gegen
MockMvc + echte SecurityConfig.

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `getPets_returnsListView` | GET `/owner/pets` | HTTP 200, View `owner/pets/list`, `pets` im Model | Normal |
| `getNewForm_returnsFormViewInNewMode` | GET `/owner/pets/new` | HTTP 200, View `owner/pets/form`, `mode=new` | Normal |
| `postNew_validForm_redirectsToList` | POST mit gültigem Form | HTTP 302 → `/owner/pets`, Service aufgerufen | Normal |
| `postNew_emptyName_returnsFormWithFieldError` | POST mit leerem Name (`@NotBlank`) | HTTP 200, Field-Error auf `name`, Service NICHT aufgerufen | Edge Case (Validation) |
| `postNew_chippedWithoutChipNumber_failsAssertTrue` | chipped=true ohne Chip-Nummer | HTTP 200, `@AssertTrue isChipDataConsistent` feuert | Edge Case (Cross-Field) |
| `getEditForm_existingPet_returnsFormPrefilled` | GET `/owner/pets/{id}/edit` | View `owner/pets/form`, `mode=edit`, `petId` im Model | Normal |
| `postEdit_validForm_redirectsToList` | Update durchläuft | HTTP 302 → `/owner/pets`, `update()` aufgerufen | Normal |
| `postEdit_invalidForm_returnsFormInEditModeWithoutUpdate` | POST mit ungültigem Form bei Edit | HTTP 200, Field-Error, kein Update | Edge Case (Validation) |
| `postDelete_redirectsToList` | POST `/owner/pets/{id}/delete` | HTTP 302 → `/owner/pets`, `delete()` aufgerufen | Normal |
| `getPets_unauthenticated_redirectsToLogin` | GET ohne Auth | HTTP 302 zu `/login` | Edge Case (Security) |
