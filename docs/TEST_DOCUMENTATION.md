# Testdokumentation

## Übersicht

**155 Tests · 100 % grün · 95 % Line Coverage (JaCoCo)**

Aufgeteilt in:

- **146 funktionale Tests** — Smoke, Security/Config, Domain, Service, Web/Controller, Repository
- **9 Architektur-Tests** — ArchUnit erzwingt Layer-Boundaries, Naming-Konventionen und Zyklenfreiheit

Coverage wird mit JaCoCo gemessen (`./mvnw verify` → `target/site/jacoco/index.html`) und als Build-Gate enforced: LINE ≥ 80 %, BRANCH ≥ 65 % — Schwellen liegen ~15 pp unter dem aktuellen Stand (95 % / 79 %) als Sicherheits-Marge gegen Regression.

### Verteilung nach Schicht

| Schicht | Tests | Stil |
|---|---|---|
| Smoke (`@SpringBootTest`) | 1 | Spring-Kontext lädt fehlerfrei |
| Security / Config | 7 | `@WebMvcTest` + Mockito gegen echte `SecurityConfig` |
| Domain (Entity-Validation) | 33 | Jakarta `Validator` direkt, keine Spring-Last |
| Service (Geschäftslogik) | 49 | Mockito-Unit-Tests |
| Web / Controller | 51 | `@WebMvcTest` mit `MockMvc` + Spring Security Test |
| Repository | 5 | `@DataJpaTest` gegen In-Memory-H2 |
| Architektur (ArchUnit) | 9 | Schicht-Regeln, Naming, Zyklen |

---

## Smoke-Test

### PawsittersApplicationTests

Datei: `src/test/java/dhbw/heilbronn/pawsitters/PawsittersApplicationTests.java`

`@SpringBootTest` ohne weitere Konfiguration — verifiziert, dass der vollständige Spring-Kontext fehlerfrei startet. Fängt Konfigurations-Regressionen (z. B. inkompatible Bean-Definitionen, kaputte Property-Verweise), bevor sie die Live-Demo erreichen.

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `contextLoads` | Vollständiger Spring-Kontext startet | Kontext lädt ohne Exception | Normal |

---

## Security & Config

### SecurityConfigTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/config/SecurityConfigTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `homeIsPubliclyAccessible` | GET `/` ohne Login | HTTP 200 | Normal |
| `dashboardRedirectAnonymousUserToLogIn` | GET `/dashboard` ohne Login | HTTP 302 Redirect zu `/login` | Edge Case (Schutzregel) |
| `dashboardIsAccessibleForUser` | GET `/dashboard` mit eingeloggtem User | HTTP 200 | Normal |
| `validLoginRedirectsToDashboard` | POST `/login` mit gültigen Credentials | HTTP 302 Redirect zu `/dashboard` | Normal |
| `invalidLoginRedirectsWithError` | POST `/login` mit ungültigem Passwort | HTTP 302 Redirect zu `/login?error` | Edge Case (Fehlerpfad) |

### CurrentUserResolverTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/security/CurrentUserResolverTest.java`

Reine Unit-Tests mit Mockito — keine Spring-Context-Last. Testet den zentralen Helper, der `UserDetails` → User-ID auflöst.

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `userId_userExists_returnsId` | Happy Path mit existierendem User | Liefert die ID des gefundenen Users | Normal |
| `userId_userNotFound_throwsIllegalStateException` | Eingeloggter User existiert nicht (mehr) in der DB | `IllegalStateException` — keine `NullPointerException` | Edge Case (Defense-in-Depth) |

---

## Domain (Entity-Validation)

### UserTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/domain/UserTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `constructor_setAllFields` | Konstruktor setzt email, passwordHashed, role korrekt | Alle Getter geben die übergebenen Werte zurück | Normal |
| `email_invalidFormat_failValidation` | E-Mail ohne `@`-Zeichen wird validiert | Validation-Violation auf Feld `email` | Edge Case (ungültiges Format) |
| `email_blank_failValidation` | Leere E-Mail wird validiert | Validation-Violation auf Feld `email` | Edge Case (leerer Pflichtwert) |
| `role_null_failValidation` | `null` als Rolle wird validiert | Validation-Violation auf Feld `role` | Edge Case (Pflichtfeld nicht gesetzt) |

### OwnerProfileTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/domain/OwnerProfileTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `constructor_setAllFields` | Konstruktor setzt user, firstName, lastName, address korrekt | Alle Getter geben die übergebenen Werte zurück, `user` ist identisch | Normal |
| `firstName_blank_failValidation` | Leerer Vorname wird validiert | Validation-Violation auf Feld `firstName` | Edge Case (leerer Pflichtwert) |
| `lastName_blank_failValidation` | Leerer Nachname wird validiert | Validation-Violation auf Feld `lastName` | Edge Case (leerer Pflichtwert) |
| `address_blank_failValidation` | Leere Adresse wird validiert | Validation-Violation auf Feld `address` | Edge Case (leerer Pflichtwert) |
| `firstName_tooLong_failValidation` | Vorname mit 101 Zeichen (über `@Size(max=100)`) | Validation-Violation auf Feld `firstName` | Edge Case (Längen-Constraint) |

### HostProfileTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/domain/HostProfileTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `constructor_setsAllFields` | Konstruktor setzt alle Felder | Alle Getter liefern korrekte Werte | Normal |
| `constructor_nullAcceptedSpecies_doesNotThrow` | Defensive Null-Behandlung | Konstruktor wirft nicht, Feld ist leeres EnumSet | Edge Case |
| `constructor_emptyAcceptedSpecies_doesNotThrow` | Regression-Test für `EnumSet.copyOf`-Bug | Konstruktor wirft nicht | Edge Case (Bug-Fix) |
| `firstName_blank_failsValidation` | `@NotBlank` auf firstName | Violation auf `firstName` | Edge Case |
| `acceptedSpecies_empty_failsValidation` | `@Size(min=1)` auf Collection | Violation auf `acceptedSpecies` | Edge Case |
| `availableFrom_inPast_failsValidation` | `@FutureOrPresent` | Violation auf `availableFrom` | Edge Case |
| `availableUntil_beforeAvailableFrom_failsAssertTrue` | Cross-Field-Reihenfolge | Violation auf `availabilityRangeValid` | Edge Case (Cross-Field) |
| `availableUntil_equalsAvailableFrom_failsAssertTrue` | Boundary: gleicher Tag | Violation auf `availabilityRangeValid` | Edge Case (Boundary) |
| `pricePerWeek_zero_failsValidation` | `@DecimalMin(inclusive=false)` | Violation auf `pricePerWeek` | Edge Case |
| `pricePerWeek_negative_failsValidation` | Negativer Preis | Violation auf `pricePerWeek` | Edge Case |

### CareRequestTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/domain/CareRequestTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `constructor_setsAllFields` | Konstruktor setzt owner, pet, startDate, endDate | Alle Getter geben übergebene Werte | Normal |
| `constructor_setsStatusToOpen` | Default-Status nach Konstruktor | Status ist `OPEN` (Workflow-Schutz: nie direkt MATCHED/CLOSED erzeugen) | Normal |
| `startDate_inPast_failsValidation` | `@Future` auf startDate | Validation-Violation auf `startDate` | Edge Case |
| `endDate_inPast_failsValidation` | `@Future` auf endDate | Validation-Violation auf `endDate` | Edge Case |
| `endDate_beforeStartDate_failsAssertTrue` | Cross-Field: Enddatum vor Startdatum | Violation auf `dateRangeValid` | Edge Case (Cross-Field) |
| `endDate_equalsStartDate_failsAssertTrue` | Boundary: gleicher Tag | Violation auf `dateRangeValid` (Enddatum nicht NACH Startdatum) | Edge Case (Boundary) |
| `owner_null_failsValidation` | `@NotNull` auf owner | Violation auf `owner` | Edge Case |
| `pet_null_failsValidation` | `@NotNull` auf pet | Violation auf `pet` | Edge Case |

### OfferTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/domain/OfferTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `constructor_setsAllFields` | Konstruktor setzt host, careRequest, weeklyPrice | Alle Getter liefern korrekte Werte | Normal |
| `constructor_setsStatusToPending` | Default-Status nach Konstruktor | Status ist `PENDING` (Workflow-Schutz, nie direkt ACCEPTED/REJECTED) | Normal |
| `host_null_failsValidation` | `@NotNull` auf host | Violation auf `host` | Edge Case |
| `careRequest_null_failsValidation` | `@NotNull` auf careRequest | Violation auf `careRequest` | Edge Case |
| `weeklyPrice_zero_failsValidation` | `@DecimalMin(inclusive=false)` | Violation auf `weeklyPrice` | Edge Case |
| `weeklyPrice_negative_failsValidation` | Negativer Preis | Violation auf `weeklyPrice` | Edge Case |

---

## Service-Schicht

### OwnerServiceTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/service/OwnerServiceTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `register_validForm_savesUserAndProfile` | Registrierung mit gültigem Form | User und OwnerProfile werden gespeichert | Normal |
| `register_passwordIsHashed_neverStoresPlaintext` | Klartext-Passwort darf nicht in der Entity landen | User bekommt nur den BCrypt-Hash, nicht das Klartext-Passwort | Edge Case (Security) |
| `register_userRoleIsOwner` | Rollen-Schutzregel bei der Registrierung | User wird mit Rolle `OWNER` gespeichert (nie HOST) | Edge Case (Privilege-Schutz) |
| `register_emailAlreadyTaken_throwsException` | Doppelte E-Mail bei der Registrierung | `EmailAlreadyTakenException`, nichts wird gespeichert | Edge Case (Duplikat) |
| `findByUserId_existing_returnsProfile` | Profil zu existierender User-ID | Korrektes OwnerProfile wird zurückgegeben | Normal |
| `findByUserId_notFound_throwsOwnerProfileNotFoundException` | Profil zu nicht-existierender User-ID | `OwnerProfileNotFoundException` | Edge Case |
| `update_existingProfile_updatesAllFields` | Update der änderbaren Felder | firstName, lastName, address werden aktualisiert | Normal |

### HostServiceTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/service/HostServiceTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `register_validForm_savesUserAndProfile` | Registrierung mit gültigem Form | User und HostProfile werden gespeichert | Normal |
| `register_passwordIsHashed_neverStoresPlaintext` | Passwort-Hashing | Nur BCrypt-Hash, nie Klartext in der Entity | Edge Case (Security) |
| `register_userRoleIsHost` | Rollen-Schutzregel | User wird mit Rolle `HOST` gespeichert (nie OWNER) | Edge Case (Privilege-Schutz) |
| `register_emailAlreadyTaken_throwsException` | Doppelte E-Mail | `EmailAlreadyTakenException`, nichts gespeichert | Edge Case (Duplikat) |
| `findByUserId_existing_returnsProfile` | Profil zu existierender User-ID | Korrektes HostProfile | Normal |
| `findByUserId_notFound_throwsHostProfileNotFoundException` | Profil fehlt | `HostProfileNotFoundException` | Edge Case |
| `update_existingProfile_updatesAllFields` | Update aller änderbaren Felder | Alle Felder aktualisiert | Normal |
| `update_notFound_throwsHostProfileNotFoundException` | Update für nicht existierendes Profil | `HostProfileNotFoundException` | Edge Case |

### PetServiceTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/service/PetServiceTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `register_validForm_savesPetWithOwner` | Pet-Anlage mit gültigem Form | Pet wird mit Owner-Referenz gespeichert | Normal |
| `register_appliesAllOptionalFields` | Optionale Felder werden übernommen | breed, birthYear, chip*, vaccinated, neutered, description gesetzt | Normal |
| `findAllByOwner_returnsOwnersPets` | Liste der Pets eines Owners | Repository liefert nur die Pets des Owners | Normal |
| `findByIdForOwner_existing_returnsPet` | Eigenes Pet per ID laden | Pet wird zurückgegeben | Normal |
| `findByIdForOwner_notOwnedByUser_throwsPetNotFound` | Fremdes Pet per URL-Manipulation | `PetNotFoundException` (gleiche Exception wie bei Nichtexistenz — kein Info-Leak) | Edge Case (Security) |
| `update_existingPet_updatesAllFields` | Update aller änderbaren Felder | Alle Felder am Pet aktualisiert | Normal |
| `delete_existingPet_callsRepositoryDelete` | Eigenes Pet löschen | `repository.delete(pet)` wird aufgerufen | Normal |
| `delete_notOwnedByUser_throwsAndDoesNotDelete` | Fremdes Pet per URL-Manipulation löschen | `PetNotFoundException`, kein `delete()`-Aufruf | Edge Case (Security) |

### CareRequestServiceTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/service/CareRequestServiceTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `register_validForm_savesRequestWithOpenStatus` | Anfrage-Anlage | Status `OPEN`, owner + pet korrekt verknüpft, `save()` aufgerufen | Normal |
| `register_petNotOwnedByUser_propagatesPetNotFound` | Pet gehört nicht dem User | `PetNotFoundException` propagiert, nichts gespeichert | Edge Case (Security) |
| `findAllByOwner_returnsOwnersRequests` | Liste der Anfragen | Repository liefert nur die des Owners | Normal |
| `findByIdForOwner_existing_returnsRequest` | Eigene Anfrage per ID | Anfrage wird zurückgegeben | Normal |
| `findByIdForOwner_notFoundOrNotOwned_throwsCareRequestNotFound` | Anfrage existiert nicht oder fremd | `CareRequestNotFoundException` | Edge Case (Security) |
| `closeExpiredRequests_setsExpiredToClosed` | Scheduled Job: abgelaufene OPEN-Anfragen | Status wechselt auf `CLOSED`, return-Count > 0 | Normal |
| `closeExpiredRequests_noneExpired_returnsZero` | Scheduled Job ohne Treffer | Kein Status-Wechsel, return-Count = 0 | Edge Case |

### OfferServiceTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/service/OfferServiceTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `findMatchingRequests_returnsResultsFromRepo` | Pass-Through zur Repo-Matching-Query | Repository-Ergebnis wird durchgereicht | Normal |
| `findOffersByHost_returnsHostsOffers` | Eigene Offers eines Hosts | Repository-Ergebnis wird durchgereicht | Normal |
| `findOffersByCareRequest_ownedByUser_returnsOffers` | Owner sieht Offers für eigene CareRequest | Liste der Offers | Normal |
| `findOffersByCareRequest_notOwnedByUser_throwsCareRequestNotFound` | Fremde CareRequest per URL-Manipulation | `CareRequestNotFoundException`, kein Offer-Lookup | Edge Case (Security) |
| `createOffer_matchingRequest_savesOfferWithPendingStatus` | Happy Path | Offer mit korrekten Feldern gespeichert | Normal |
| `createOffer_withMessage_storesMessageOnEntity` | Optionales Nachrichten-Feld | Message wird mitpersistiert | Normal |
| `createOffer_careRequestNotFound_throwsException` | CareRequest-ID existiert nicht | `CareRequestNotFoundException` | Edge Case |
| `createOffer_notMatching_throwsOfferNotEligible` | CareRequest existiert, passt aber nicht (URL-Manipulation) | `OfferNotEligibleException`, kein Save | Edge Case (Security) |
| `accept_pending_setsOfferAcceptedAndRequestMatched` | Happy Path: Offer PENDING→ACCEPTED, CareRequest OPEN→MATCHED | Status-Wechsel beider Entities korrekt | Normal |
| `accept_cascadeRejectsOtherPendingOffers` | Kaskade: 3 PENDING-Offers, eins wird angenommen | Akzeptiertes Offer ACCEPTED, andere zwei REJECTED, CareRequest MATCHED | Normal |
| `accept_offerNotFound_throwsOfferNotFound` | Offer-ID existiert nicht | `OfferNotFoundException`, kein Status-Wechsel | Edge Case |
| `accept_offerNotOwnedByUser_throwsOfferNotFound` | Owner versucht fremdes Offer anzunehmen | `OfferNotFoundException` (gleiche Exception, kein Info-Leak) | Edge Case (Security) |
| `accept_offerAlreadyAccepted_throwsOfferNotPending` | Offer ist schon ACCEPTED/REJECTED | `OfferNotPendingException` | Edge Case |
| `accept_careRequestAlreadyMatched_throwsOfferNotPending` | CareRequest schon MATCHED (Race-Condition) | `OfferNotPendingException` | Edge Case |
| `reject_pending_setsOfferRejectedAndLeavesRequestOpen` | Happy Path: PENDING→REJECTED, CareRequest bleibt OPEN | Status-Wechsel nur am Offer | Normal |
| `reject_doesNotAffectOtherPendingOffers` | Reject ist isoliert | Andere PENDING-Offers bleiben unverändert | Normal |
| `reject_offerNotFound_throwsOfferNotFound` | Offer-ID existiert nicht | `OfferNotFoundException`, kein Status-Wechsel | Edge Case |
| `reject_offerNotOwnedByUser_throwsOfferNotFound` | Owner versucht fremdes Offer abzulehnen | `OfferNotFoundException` (gleiche Exception, kein Info-Leak) | Edge Case (Security) |
| `reject_offerAlreadyRejected_throwsOfferNotPending` | Offer ist schon REJECTED/ACCEPTED | `OfferNotPendingException` | Edge Case |

---

## Web / Controller

### OwnerControllerTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/web/controller/OwnerControllerTest.java`

Integrationstest mit `@WebMvcTest`, läuft gegen MockMvc + echte SecurityConfig.

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `getRegister_returnsRegisterView` | GET `/owner/register` | HTTP 200, View `owner/register`, leeres `registerForm` im Model | Normal |
| `postRegister_validForm_redirectsToLoginWithFlag` | POST `/owner/register` mit gültigem Form | HTTP 302 Redirect zu `/login?registered`, `OwnerService.register` aufgerufen | Normal |
| `postRegister_invalidEmail_returnsRegisterWithErrors` | POST mit ungültiger E-Mail | HTTP 200, View `owner/register`, Field-Error auf `email`, Service NICHT aufgerufen | Edge Case (Validation) |
| `postRegister_emailAlreadyTaken_showsFieldError` | POST mit bereits vergebener E-Mail | HTTP 200, View `owner/register`, Field-Error auf `email` | Edge Case (Duplikat) |

### HostControllerTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/web/controller/HostControllerTest.java`

Integrationstest mit `@WebMvcTest` + `@WithMockUser(roles="HOST")` gegen MockMvc + echte SecurityConfig.

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `getRegister_returnsRegisterView` | GET `/host/register` | View `host/register`, `registerForm` + `allSpecies` im Model | Normal |
| `postRegister_validForm_redirectsToLoginWithFlag` | POST mit gültigem Form | HTTP 302 zu `/login?registered`, Service aufgerufen | Normal |
| `postRegister_invalidEmail_returnsRegisterWithFieldError` | POST mit ungültiger E-Mail | Field-Error auf `email`, kein Service-Call | Edge Case (Validation) |
| `postRegister_endBeforeStart_returnsRegisterWithoutCallingService` | Cross-Field `availableUntil` < `availableFrom` | Form-View, kein Service-Call | Edge Case (Cross-Field) |
| `postRegister_emailAlreadyTaken_showsFieldError` | E-Mail-Duplikat | Field-Error auf `email` | Edge Case (Duplikat) |
| `getProfile_returnsProfileView` | GET `/host/profile` | View `host/profile`, `profile` im Model | Normal |
| `getEditForm_returnsEditViewPrefilled` | GET `/host/profile/edit` | View `host/profile-edit`, `updateForm` vorbefüllt, `allSpecies` im Model | Normal |
| `postEditForm_validForm_redirectsToProfile` | POST mit gültigem Form | HTTP 302 zu `/host/profile`, `update()` aufgerufen | Normal |
| `postEditForm_invalidForm_returnsEditViewWithoutUpdate` | POST mit leerem firstName | Field-Error auf `firstName`, kein Update | Edge Case (Validation) |
| `getProfile_unauthenticated_redirectsToLogin` | GET ohne Auth | HTTP 302 zu `/login` | Edge Case (Security) |

### PetControllerTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/web/controller/PetControllerTest.java`

Integrationstest mit `@WebMvcTest` + `@WithMockUser(roles="OWNER")` gegen MockMvc + echte SecurityConfig.

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `getPets_returnsListView` | GET `/owner/pets` | HTTP 200, View `owner/pets/list`, `pets` im Model | Normal |
| `getNewForm_returnsFormViewInNewMode` | GET `/owner/pets/new` | HTTP 200, View `owner/pets/form`, `mode=new` | Normal |
| `postNew_validForm_redirectsToList` | POST mit gültigem Form | HTTP 302 → `/owner/pets`, Service aufgerufen | Normal |
| `postNew_emptyName_returnsFormWithFieldError` | POST mit leerem Name (`@NotBlank`) | HTTP 200, Field-Error auf `name`, Service NICHT aufgerufen | Edge Case (Validation) |
| `postNew_chippedWithoutChipNumber_failsAssertTrue` | `chipped=true` ohne Chip-Nummer | HTTP 200, `@AssertTrue isChipDataConsistent` feuert | Edge Case (Cross-Field) |
| `getEditForm_existingPet_returnsFormPrefilled` | GET `/owner/pets/{id}/edit` | View `owner/pets/form`, `mode=edit`, `petId` im Model | Normal |
| `postEdit_validForm_redirectsToList` | Update durchläuft | HTTP 302 → `/owner/pets`, `update()` aufgerufen | Normal |
| `postEdit_invalidForm_returnsFormInEditModeWithoutUpdate` | POST mit ungültigem Form bei Edit | HTTP 200, Field-Error, kein Update | Edge Case (Validation) |
| `postDelete_redirectsToList` | POST `/owner/pets/{id}/delete` | HTTP 302 → `/owner/pets`, `delete()` aufgerufen | Normal |
| `getPets_unauthenticated_redirectsToLogin` | GET ohne Auth | HTTP 302 zu `/login` | Edge Case (Security) |

### CareRequestControllerTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/web/controller/CareRequestControllerTest.java`

Integrationstest mit `@WebMvcTest` + `@WithMockUser(roles="OWNER")` gegen MockMvc + echte SecurityConfig.

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `getCareRequests_returnsListView` | GET `/owner/care-requests` | View `owner/care-requests/list`, `careRequests` im Model | Normal |
| `getNewForm_returnsFormViewWithPetsAndEmptyRequestForm` | GET `/new` | View `owner/care-requests/form`, `careRequestForm` + `pets` im Model | Normal |
| `postNew_validForm_redirectsToList` | POST mit gültigem Form | HTTP 302 → `/owner/care-requests`, Service aufgerufen | Normal |
| `postNew_endBeforeStart_returnsFormWithoutRegister` | `@AssertTrue` Cross-Field | View `form`, Service NICHT aufgerufen | Edge Case (Cross-Field) |
| `postNew_pastStartDate_returnsFormWithFieldError` | `@Future` auf startDate | Field-Error auf `startDate`, kein Service-Call | Edge Case (Validation) |
| `postNew_missingPetId_returnsFormWithFieldError` | `@NotNull` auf petId | Field-Error auf `petId`, kein Service-Call | Edge Case (Validation) |
| `getDetail_existingCareRequest_returnsDetailView` | GET `/owner/care-requests/{id}` | View `owner/care-requests/detail`, `careRequest` im Model | Normal |
| `getCareRequests_unauthenticated_redirectsToLogin` | GET ohne Auth | HTTP 302 zu `/login` | Edge Case (Security) |
| `getDetail_unauthenticated_redirectsToLogin` | GET Detail ohne Auth | HTTP 302 zu `/login` | Edge Case (Security) |

### OfferControllerTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/web/controller/OfferControllerTest.java`

Integrationstest mit `@WebMvcTest` — gemischte Rollen (HOST und OWNER), pro Test per `@WithMockUser` gesetzt.

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `getBrowseMatchingRequests_returnsListView` | GET `/host/care-requests` (Host) | View `host/care-requests/list`, `matchingRequests` im Model | Normal |
| `getOfferForm_returnsFormViewWithCareRequestId` | GET `/host/care-requests/{id}/offer` | View, `offerForm` + `careRequestId` im Model | Normal |
| `postCreateOffer_validForm_redirectsToHostOffers` | POST mit gültigem Form | HTTP 302 → `/host/offers`, Service aufgerufen | Normal |
| `postCreateOffer_invalidPrice_returnsFormViewWithoutService` | `weeklyPrice = 0` | Field-Error auf `weeklyPrice`, kein Service-Call | Edge Case (Validation) |
| `getHostOffers_returnsHostOffersView` | GET `/host/offers` | View `host/offers/list`, `offers` im Model | Normal |
| `getOwnerOffers_returnsOwnerOffersView` | GET `/owner/care-requests/{id}/offers` (Owner) | View `owner/care-requests/offers`, `offers` + `careRequestId` im Model | Normal |
| `getBrowseMatchingRequests_unauthenticated_redirectsToLogin` | GET ohne Auth | HTTP 302 zu `/login` | Edge Case (Security) |
| `postAcceptOffer_validOffer_redirectsToOffersOfCareRequest` | POST `/owner/offers/{id}/accept` als Owner | HTTP 302 → Offers-Liste der CareRequest, Service mit korrektem offerId + userId aufgerufen | Normal |
| `postRejectOffer_validOffer_redirectsToOffersOfCareRequest` | POST `/owner/offers/{id}/reject` als Owner | HTTP 302 → Offers-Liste der CareRequest, `OfferService.reject()` aufgerufen | Normal |
| `postRejectOffer_unauthenticated_redirectsToLogin` | POST Reject ohne Auth | HTTP 302 zu `/login` | Edge Case (Security) |

### GlobalExceptionHandlerTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/web/controller/GlobalExceptionHandlerTest.java`

Integrationstest mit `@WebMvcTest` + Top-Level-`ThrowingTestController`, der gezielt jede gemappte Exception wirft. So wird der Handler-Pfad isoliert getestet, ohne die echten Controller zu mocken.

| Test | Exception | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `petNotFound_returns404View` | `PetNotFoundException` | HTTP 404, View `error/404`, `message` im Model | Edge Case |
| `careRequestNotFound_returns404View` | `CareRequestNotFoundException` | HTTP 404, View `error/404` | Edge Case |
| `offerNotFound_returns404View` | `OfferNotFoundException` | HTTP 404, View `error/404` | Edge Case |
| `hostProfileNotFound_returns404View` | `HostProfileNotFoundException` | HTTP 404, View `error/404` | Edge Case |
| `ownerProfileNotFound_returns404View` | `OwnerProfileNotFoundException` | HTTP 404, View `error/404` | Edge Case |
| `offerNotPending_returns409View` | `OfferNotPendingException` | HTTP 409, View `error/409` | Edge Case |
| `offerNotEligible_returns409View` | `OfferNotEligibleException` | HTTP 409, View `error/409` | Edge Case |
| `emailAlreadyTaken_returns409View` | `EmailAlreadyTakenException` | HTTP 409, View `error/409` | Edge Case |

---

## Repository

### CareRequestRepositoryTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/repository/CareRequestRepositoryTest.java`

Integrationstest mit `@DataJpaTest` gegen In-Memory-H2 — verifiziert die JPQL-Matching-Query gegen echte SQL-Ausführung. Mockito kann SQL-Bugs nicht fangen, daher diese Schicht.

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `findMatchingForHost_speciesAndDateMatch_returnsRequest` | Happy Path — alle 4 Filter passen | CareRequest erscheint im Ergebnis | Normal |
| `findMatchingForHost_speciesMismatch_returnsEmpty` | Filter: Species-Check | Pet-Species nicht in `acceptedSpecies` → ausgefiltert | Edge Case (Filter-Pfad) |
| `findMatchingForHost_dateOutsideAvailability_returnsEmpty` | Filter: Datum-Range | CareRequest-Range außerhalb Host-Verfügbarkeit → ausgefiltert | Edge Case (Filter-Pfad) |
| `findMatchingForHost_hostAlreadyOffered_excludesRequest` | Filter: `NOT EXISTS`-Subquery | Host hat schon Offer → ausgefiltert | Edge Case (Filter-Pfad) |
| `findMatchingForHost_closedRequest_returnsEmpty` | Filter: Status `OPEN` | MATCHED/CLOSED-Requests werden ausgefiltert | Edge Case (Filter-Pfad) |

---

## Architektur

### ArchitectureTest

Datei: `src/test/java/dhbw/heilbronn/pawsitters/architecture/ArchitectureTest.java`

Architektur-Tests mit ArchUnit. Jede Regel ist eine Architektur-Aussage, die der Build erzwingt — Verstöße scheitern CI. So bleibt die MVC- / Layered-Architektur enforced statt nur dokumentiert. Scope: nur Produktionscode (`ImportOption.DoNotIncludeTests`), damit Test-Hilfsklassen keine Schicht-Regeln verletzen.

| Test | Was wird geprüft | Erwartetes Ergebnis | Typ |
|---|---|---|---|
| `web_must_not_directly_access_repositories` | Web-Layer importiert nicht direkt aus `repository` — Controller müssen über Service-Schicht gehen | Keine Verletzung im Production-Code | Architektur |
| `domain_must_not_depend_on_other_layers` | Domain hat keine Outbound-Dependencies zu `web` / `service` / `repository` / `config` / `security` | Keine Verletzung | Architektur |
| `domain_must_not_depend_on_spring` | Entities sind framework-frei (kein `org.springframework`-Import; Jakarta-Persistence-Annotations okay) | Keine Verletzung | Architektur |
| `repositories_must_not_depend_on_services_or_web` | Persistence-Layer kennt weder Service noch Web — Dependency-Flow nur nach unten | Keine Verletzung | Architektur |
| `classes_annotated_controller_belong_in_web_controller_package` | `@Controller`-Klassen leben nur in `web.controller` | Keine Verletzung | Lokation |
| `controllers_should_have_controller_suffix` | `@Controller`-Klassennamen enden auf "Controller" | Keine Verletzung | Naming |
| `services_in_service_package_should_have_service_suffix` | `@Service`-Klassen im `service`-Paket enden auf "Service" | Keine Verletzung | Naming |
| `repositories_should_have_repository_suffix` | Klassen im `repository`-Paket enden auf "Repository" | Keine Verletzung | Naming |
| `no_cycles_between_top_level_packages` | Keine zyklischen Abhängigkeiten zwischen Top-Level-Paketen | Keine Verletzung | Strukturell |

> **Hinweis zur Entstehung:** `no_cycles_between_top_level_packages` hat beim Erstellen einen impliziten `service ↔ web`-Zyklus aufgedeckt — Services nahmen Form-DTOs aus `web.form` als Parameter. Behoben durch Verschieben der Form-Records in ein neutrales Top-Level-Paket `dto`. Beispiel für „Architektur als Code": ArchUnit hat einen Smell sichtbar gemacht, der bei reinem Code-Reading unsichtbar geblieben wäre.
