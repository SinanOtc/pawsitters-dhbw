
# Security-Konzept

  ## Worum geht's

  Pawsitters ist eine Web-Anwendung mit Login. Sobald irgendwo ein Login existiert, gibt es einen Angriffsvektor, und sobald wir Tierhalter-Daten und Wohnadressen speichern, gibt es einen
  Grund, warum jemand reinwollen könnte. Dieses Dokument beschreibt, welche Daten wir als sensibel einstufen, gegen welche typischen Angriffe wir uns absichern, was davon im aktuellen Code
   schon drin ist und was in einer Produktivversion noch dazukommen müsste.

  Wir schreiben das absichtlich kein typisches Compliance-PDF. Statt einer Checkliste „CIS Benchmark Item X.Y.Z" stehen hier die Entscheidungen, die wir im Code wirklich getroffen haben,
  mit dem Warum dahinter.

  ## Welche Daten sind sensibel?

  Drei Stufen, von schlimm zu egal:

  *Kritisch (Datenleck wäre echter Schaden):*
  - Passwörter der Nutzer. Liegen niemals im Klartext, weder in der DB noch in Logs.
  - Wohnadressen von Tierhaltern und Hosts. Im Brief explizit angefordert, weil ein Host ja wissen muss, wo das Tier hingebracht wird — aber für die Plattform-Sicht heißt das: privat
  halten.
  - Session-Cookies. Wer den hat, ist eingeloggt.

  *Wichtig (verärgert oder kompromittiert Vertrauen):*
  - E-Mail-Adressen. Login-Identifier und potentieller Vektor für Phishing.
  - Profil-Daten (Vor- und Nachname, Verfügbarkeitszeiträume des Hosts).
  - Geschäfts-Daten: welcher Owner hat welche Anfrage gestellt, welcher Host hat welches Angebot gemacht. Sollte zwischen den Nutzern getrennt bleiben.

  *Unkritisch:*
  - Statische Daten wie Tierarten, Status-Werte, Preise als Zahl.

  ## Bedrohungsmodell

  Wir sind ein Schulprojekt mit einer öffentlich erreichbaren Live-Demo, also realistisch genug für ein Pentest-Mindset, aber ohne Ressourcen für ein vollwertiges Threat-Modeling. Die
  Angriffsklassen, die wir aktiv im Kopf hatten, sind diese vier:

  *1. Privilege Escalation horizontal (User A liest Daten von User B).*
  Das ist für uns die wahrscheinlichste Bedrohung, weil der Anwendungsfall genau dazu einlädt: Angreifer ist eingeloggt, ändert in der URL ⁠ /owner/pets/42 ⁠ auf ⁠ /owner/pets/43 ⁠ und landet
  beim Pet eines anderen Owners.

  *2. Privilege Escalation vertikal (Owner ruft Host-Routen oder umgekehrt).*
  Ein Owner-Account, der die Host-Browse-Seite ansurft. Oder ein Host, der versucht ein Offer im Namen eines anderen Hosts zu erzeugen.

  *3. Klassische Web-Angriffe.*
  CSRF beim POST-Endpoints, XSS in Benutzereingaben, SQL-Injection in Repository-Queries, Session-Hijacking.

  *4. Lieferketten-Angriffe.*
  Jemand in einer transitiven Dependency hat eine CVE-bekannte Schwachstelle, und wir bekommen das nicht mit, weil wir die nicht selbst gepflegt haben.

  Was wir bewusst nicht in den Scope nehmen: DDoS, physischer Server-Zugriff, Insider-Threats. Für eine Schul-Demo auf einer Free-Tier-Hosting-Plattform sind das die falschen Prioritäten.

  ## Was wir konkret tun

  ### Authentifizierung

  Spring Security mit Standard-Form-Login. E-Mail als Benutzername, Passwort wird beim Anlegen mit BCrypt gehasht und in der Spalte ⁠ password_hashed ⁠ der ⁠ users ⁠-Tabelle abgelegt. Wir
  machen das nicht selbst, sondern lassen es Spring Security machen — eine eigene Hash-Implementierung schreiben heißt sich selbst ins Knie schießen.

  Konkret im Code: ⁠ SecurityConfig ⁠ definiert die Filter-Chain, ⁠ CustomUserDetailsService ⁠ mappt unsere ⁠ User ⁠-Entity auf das von Spring erwartete ⁠ UserDetails ⁠-Interface. Beim Login prüft
   Spring den BCrypt-Hash gegen das eingegebene Passwort — wir sehen das Klartext-Passwort genau einmal, beim Registrierungs-POST, und auch dort nur als String in
  ⁠ RegisterOwnerForm.password() ⁠, der direkt in den Encoder geht und danach verworfen wird.

  Sessions liegen im Standard-Spring-Session-Cookie, ⁠ HttpOnly ⁠ und mit Path-Restriction. In Prod hinter Railway's TLS-Terminator läuft alles über HTTPS, das Cookie kann also nicht im
  Klartext aus dem Netzwerk gefischt werden.

  ### Autorisierung

  Wir haben zwei Rollen, ⁠ OWNER ⁠ und ⁠ HOST ⁠, gesetzt am ⁠ User.role ⁠-Feld. Spring Security setzt sie als Spring-Authorities, und in der ⁠ SecurityConfig ⁠ mappen wir Routen darauf:

  /owner/**   → hasRole("OWNER")
  /host/**    → hasRole("HOST")
  /, /login, /register-*, /css/, /js/, /actuator/health, /actuator/info → permitAll
  anyRequest  → authenticated

  Das deckt die vertikale Privilege Escalation ab. Wenn ein Owner versucht ⁠ /host/offers ⁠ aufzurufen, gibt's einen 403, bevor irgendein Controller-Code überhaupt anläuft.

  Für die horizontale Privilege Escalation reicht die Routen-Autorisierung nicht. Ein Owner darf zwar ⁠ /owner/pets/42 ⁠, aber Pet 42 könnte dem anderen Owner gehören. Hier kommt das
  *Owner-Scoping-Pattern* rein, das wir in der Service-Schicht durchhalten: jede Methode, die ein Aggregat über die ID lädt, prüft im selben Schritt, ob der eingeloggte User der Besitzer
   ist. Wenn nicht, wird die gleiche Exception geworfen wie bei „Aggregat existiert nicht" — also ⁠ PetNotFoundException ⁠ statt ⁠ AccessDeniedException ⁠. Das macht der Reviewer beim ersten
  Lesen vielleicht falsch, aber es ist Absicht: ein Angreifer soll nicht über die Antwort unterscheiden können, ob Pet 43 existiert (gehört aber jemand anderem) oder nicht existiert.
  Beides gibt 404, kein Info-Leak.

  Beispiel aus ⁠ PetService ⁠:

  ```java
  public Pet findByIdForOwner(Long petId, Long ownerUserId) {
      OwnerProfile owner = ownerService.findByUserId(ownerUserId);
      return petRepository.findByIdAndOwnerId(petId, owner.getId())
              .orElseThrow(() -> new PetNotFoundException(petId));
  }

  Das Repository macht den Owner-Scope-Check im SQL-Query selbst (WHERE id = ? AND owner_id = ?) — das ist auf der DB-Schicht. Auf der Service-Schicht ist die Method-Signatur so
  geschnitten, dass „ohne ownerUserId lädt nichts" praktisch unmöglich ist, weil die Methode nicht ohne diesen Parameter aufrufbar ist.

  Input-Validation

  Jakarta Bean Validation auf zwei Ebenen, was etwas redundant aussieht, aber bewusst ist. Forms im dto/-Paket haben @NotNull, @Size, @Email, @DecimalMin, @Future, @AssertTrue für
  Cross-Field-Regeln. Das fängt Müll ab, bevor der Service überhaupt aufgerufen wird, und gibt dem User direkt eine Fehlermeldung am Form-Feld.

  Aber die gleichen Validierungen stehen auch noch mal an den Entity-Feldern, weil ein Form ja nicht der einzige Weg ist, ein Entity in die DB zu schreiben — Migrations, Tests, Dev-Seed
  könnten das Form-Layer umgehen. Wenn Hibernate beim Persistieren eine Constraint Violation findet, knallt es ehrlich, statt schweigend kaputte Daten zu speichern.

  Beispiel für Cross-Field-Validation in der CareRequest-Entity:

  @AssertTrue(message = "Enddatum muss nach Startdatum liegen")
  public boolean isDateRangeValid() {
      if (startDate == null || endDate == null) return true;
      return !endDate.isBefore(startDate);
  }

  Auf Form-Ebene haben wir die gleiche Regel nochmal, weil die Fehlermeldung auf der Form-Seite anders aussehen soll.

  CSRF

  CSRF-Schutz von Spring Security ist eingeschaltet (Default) und wir haben ihn nicht abgeschaltet. Jeder POST-Endpoint will einen CSRF-Token, und der wird automatisch in die
  Thymeleaf-Templates injected. In den @WebMvcTest-Tests benutzen wir with(csrf()), damit die Tests nicht 403 zurückgeben.

  Wenn ein Angreifer einem eingeloggten Owner ein Bild von <img src="https://pawcation.up.railway.app/owner/offers/123/accept"> unterschiebt, passiert nichts, weil der POST-Endpoint einen
  Token erwartet, den der Angreifer nicht hat.

  SQL-Injection

  Alle Datenbankzugriffe gehen über Spring Data JPA und parameterisierte Queries. Wir bauen keine SQL-Strings durch String-Konkatenation. Die einzigen Stellen mit Custom-Queries sind
  @Query-Methoden in den Repositories, und auch die nutzen Named oder Positional Parameters, niemals + mit User-Input.

  XSS

  Thymeleaf escaped per Default alles, was via ${...} ausgegeben wird. Wir benutzen nirgendwo [(...)] oder th:utext für unsanitisierte User-Eingaben. Wenn ein Owner als Pet-Namen
  <script>alert(1)</script> einträgt, landet das als &lt;script&gt;alert(1)&lt;/script&gt; im HTML — nicht als ausführbares Script.

  Info-Leak-Schutz

  Wir haben einen GlobalExceptionHandler als @ControllerAdvice, der die domänenspezifischen Exceptions auf saubere HTTP-Statuscodes mapped: *NotFoundException → 404, *NotPendingException
  und Konflikte → 409. Das Wichtige daran ist, dass User keine Stack-Traces zu sehen bekommen. Spring würde im Default beim ungefangenen Exception eine HTML-Fehlerseite mit Stack-Trace
  ausspielen, und in diesem Stack-Trace stehen Datenbank-Tabellen-Namen, Klassennamen, manchmal sogar Datenstrukturen.

  Dazu passend: alle Endpoints von Spring Boot Actuator sind in der application.yaml explizit auf eine Whitelist begrenzt:

  management:
    endpoints:
      web:
        exposure:
          include: health,info

  Standardmäßig sind nur health und info öffentlich. env ist gar nicht erst angeschaltet, was vermeidet, dass jemand /actuator/env aufruft und unsere Datenbank-Passwörter im Klartext
  sieht. Plus haben wir in der SecurityConfig zusätzlich /actuator/health und /actuator/info explizit permittet — das ist Defense in Depth: wenn jemand in der yaml versehentlich * setzt,
  ist der nächste Endpoint trotzdem hinter Auth versteckt.

  Shift Security Left

  „Shift Security Left" heißt: Sicherheit nicht erst am Ende vom Pentest, sondern so früh wie möglich in der Pipeline. Im klassischen Wasserfall war das Sicherheitsaudit der letzte Schritt
   vor Go-Live — und entsprechend teuer, wenn was gefunden wurde. Heute will man, dass Sicherheits-Checks beim Tippen und beim Commit passieren, nicht beim Release.

  Wie wir das im Projekt anwenden, in der Reihenfolge wie es passiert wenn jemand Code ändert:

  Beim Tippen schon Validation. Bean-Validation-Annotations am Code sind eine Form von „Security Spec im Code". Ein @NotNull auf einem Feld zwingt jeden Entwickler, sich Gedanken zu
  machen, was bei null passieren würde — die Frage muss nicht später in einem Code-Review aufkommen, sie ist schon beantwortet.

  Beim Commit ArchUnit. Unsere 9 ArchUnit-Regeln erzwingen, dass die Schichten-Trennung nicht unterlaufen wird. Eine davon ist sicherheits-relevant: Controller dürfen nicht direkt aufs
  Repository zugreifen. Das ist nicht nur Architektur-Hygiene — es ist auch eine Security-Regel, weil die Service-Schicht ist, wo Owner-Scoping passiert. Wenn ein Controller direkt auf das
   Repository zugreifen würde, könnte er den Scope-Check umgehen. ArchUnit verhindert genau das, automatisch, in CI.

  Bei jedem PR die Tests. 150+ Tests, davon viele auf Sicherheits-Verhalten. Beispiele:
  - accept_offerNotOwnedByUser_throwsOfferNotFound — testet explizit, dass die gleiche Exception geworfen wird wie bei Nichtexistenz (kein Info-Leak).
  - dashboardRedirectAnonymousUserToLogIn — testet, dass /dashboard ohne Auth zu /login umleitet.
  - postRejectOffer_unauthenticated_redirectsToLogin — analog für den neuen Reject-Endpoint.

  Wenn jemand eine Sicherheits-Regel im Code aufweicht, scheitert mindestens einer dieser Tests, und der PR wird nicht gemergt. Das ist Shift-Security-Left in seiner reinsten Form: die
  Regel ist nicht in einem Dokument, sie ist eine Assertion in einem Test.

  Beim manuellen Trigger der Dependency-Scan. OWASP Dependency-Check läuft via ./mvnw dependency-check:check gegen die National Vulnerability Database und gibt einen Report über alle
  bekannten CVEs in unseren Maven-Dependencies. Build-Gate scheitert bei CVSS ≥ 7.0 (HIGH/CRITICAL). Der Erst-Scan hat 9 Findings produziert, davon 4 CRITICAL — wir haben sieben durch
  Versions-Upgrades behoben (Tomcat 11.0.21 → 11.0.22, PostgreSQL-Driver 42.7.10 → 42.7.11, Hibernate-Validator 9.0.1 → 9.1.0), zwei sind als bewusst akzeptiert in
  .dependency-check-suppressions.xml dokumentiert, jeweils mit Datum und Re-Review-Frist. Der Scan ist nicht an die verify-Phase gebunden — der erste NVD-Download dauert ohne API-Key 30+
  Minuten, das wäre für jeden Build untragbar. Aber jeder Entwickler kann ihn manuell triggern, und wir sollten das vor jedem Release tun.

  In Production der Healthcheck. Railway pingt alle 30 Sekunden /actuator/health. Wenn die DB-Verbindung weg ist, antwortet der Endpoint nicht mit UP, Railway behält den alten gesunden
  Deploy am Leben und schaltet nicht auf einen kaputten neuen um. Das ist nicht direkt Security im klassischen Sinne, aber Availability ist Teil von „CIA" (Confidentiality, Integrity,
  Availability), und für die Live-Demo bei der Präsentation ist das praktisch relevant.

  Was in einer Produktivversion noch dazukommen müsste

  Wir sind ehrlich darüber, was fehlt. Eine Schul-Demo ist nicht das gleiche wie ein Produkt mit echten Nutzern, und ein Reviewer sollte sehen, dass wir das wissen.

  Rate Limiting auf Login. Aktuell kann jemand Brute-Force gegen /login machen. In Prod würden wir das mit bucket4j oder einer Reverse-Proxy-Regel limitieren (z. B. 5 Fehl-Logins pro
  Minute pro IP → 15 Min Sperre).

  Account-Lockout nach mehrfachen Fehl-Versuchen. Komplementär zum Rate-Limiting, aber pro Account statt pro IP. Schutz gegen Distributed Brute-Force.

  E-Mail-Verifikation bei Registrierung. Aktuell registrieren wir einfach. In Prod würden wir einen Bestätigungslink per Mail schicken und den Account erst aktivieren wenn der Link
  geklickt wurde. Verhindert, dass jemand fremde E-Mails registriert.

  Password Reset. Den haben wir komplett nicht. Wer sein Passwort vergisst, ist verloren. In Prod würde das mit Token-Mail-Flow gelöst.

  2FA / TOTP. Für Hosts und Owner zwar nicht zwingend, aber für ein potentielles Admin-Panel später ein Must.

  Audit-Logs. Aktuell loggen wir nur das, was Spring Boot von sich aus loggt. In Prod würden wir security-relevante Aktionen (Login-Success, Login-Failure, Offer-Accept, Profile-Update) in
   eine separate Log-Sektion schreiben, mit Zeitstempel, User-ID, IP. Wichtig sowohl für Forensik nach einem Vorfall als auch für die DSGVO-Auskunftspflicht.

  Logging-Privacy. Wir loggen aktuell User-IDs, aber keine E-Mails oder Adressen. Das ist eher Glück als Konzept — in Prod sollten wir explizit Logback-Pattern haben, die PII redacten, und
   alle Service-Methoden sollten so loggen, dass kein versehentlicher PII-Leak in stdout/Datadog/wo auch immer landet.

  HTTPS-Erzwingung im Code. Aktuell macht Railway TLS-Terminierung, und der Spring-Boot-Container hört intern auf HTTP. Das ist OK so, weil Railway nichts anderes durchlässt — aber in
  einer selbst-gemanagten Prod-Umgebung würden wir HSTS-Header und HTTP→HTTPS-Redirects in Spring konfigurieren.

  Regelmäßige Dependency-Updates. Dependabot ist als Issue #78 im Backlog. Würde automatisch PRs erstellen wenn eine Dependency-Version mit CVE-Fix verfügbar ist. Aktuell machen wir das
  manuell, was funktioniert solange jemand aktiv am Projekt ist — aber in Prod „on autopilot" wäre Dependabot der Standard.

  CSP-Header (Content Security Policy). Aktuell setzen wir keinen. Würde XSS noch einmal restriktiver einschränken, indem der Browser keine Inline-Scripts oder externe Scripts ausführt,
  die nicht explizit erlaubt sind.

  Personenbezogene-Daten-Konzept (DSGVO). Auskunfts-, Lösch- und Berichtigungsrecht müssten implementiert sein. Aktuell hat ein Nutzer keinen Knopf „Account löschen". Für Prod wäre das
  Pflicht.

  Zusammenfassung in einem Satz

  Das, was Pflicht ist und worauf User direkt klicken können, haben wir abgesichert — Auth, Owner-Scoping, CSRF, Input-Validation, kein Info-Leak. Das, was Tooling-getrieben und in der
  Pipeline läuft — ArchUnit, JaCoCo, Dependency-Check, Tests — ist unser konkreter Beitrag zu „Shift Security Left". Was darüber hinausgehen würde — Rate-Limiting, 2FA, Audit-Logs,
  DSGVO-Workflows — haben wir bewusst draußen gelassen und transparent dokumentiert.