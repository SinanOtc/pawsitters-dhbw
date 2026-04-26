# Testdokumentation
## Übersicht

## SecurityConfigTest
Datei: `src/test/java/dhbw/heilbronn/pawsitters/config/SecurityConfigTest.java`

| Test | Was wird getestet | Erwartetes Ergebnis | Typ |
  |---|---|---|---|
| `homeIsPubliclyAccessible` | GET `/` ohne Login | HTTP 200 | Normal |
| `dashboardRedirectAnonymousUserToLogIn` | GET `/dashboard` ohne Login | HTTP 302 Redirect zu
  `/login` | Edge Case (Schutzregel) |
| `dashboardIsAccessibleForAuthenticatedUser` | GET `/dashboard` mit eingeloggtem User | HTTP 200 |
  Normal |
| `validLoginRedirectsToDashboard` | POST `/login` mit gültigen Credentials | HTTP 302 Redirect zu
  `/dashboard` | Normal |
| `invalidLoginRedirectsBackWithError` | POST `/login` mit ungültigem Passwort | HTTP 302 Redirect zu
   `/login?error` | Edge Case (Fehlerpfad) |
