package dhbw.heilbronn.pawsitters.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// Domain-Test für CareRequest. Nutzt den Jakarta Validator direkt
// um Field-Constraints und @AssertTrue Cross-Field-Check zu prüfen,
// ganz ohne Spring-Context (gleiches Pattern wie UserTest, OwnerProfileTest).
class CareRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    // === Helper ===

    // Gültige Anfrage mit Standard-Werten — Basis für Tests die einzelne Felder kaputt machen
    private CareRequest validRequest() {
        User user = new User("o@t.de", "hash", UserRole.OWNER);
        OwnerProfile owner = new OwnerProfile(user, "Max", "Muster",
                "Adresse 1");
        Pet pet = new Pet(owner, "Bello", PetSpecies.DOG, PetGender.MALE);
        return new CareRequest(owner, pet,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(8));
    }

    // === Tests ===

    @Test
    void constructor_setsAllFields() {
        CareRequest cr = validRequest();
        assertThat(cr.getOwner()).isNotNull();
        assertThat(cr.getPet()).isNotNull();
        assertThat(cr.getStartDate()).isAfter(LocalDate.now());
        assertThat(cr.getEndDate()).isAfter(cr.getStartDate());
    }

    @Test
    void constructor_setsStatusToOpen() {
        // Schutzregel: Neue Anfragen starten IMMER OPEN, nie in MATCHED/CLOSED
        CareRequest cr = validRequest();
        assertThat(cr.getStatus()).isEqualTo(RequestStatus.OPEN);
    }

    @Test
    void startDate_inPast_failsValidation() {
        CareRequest cr = validRequest();
        cr.setStartDate(LocalDate.now().minusDays(1));
        Set<ConstraintViolation<CareRequest>> violations =
                validator.validate(cr);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("startDate"));
    }

    @Test
    void endDate_inPast_failsValidation() {
        CareRequest cr = validRequest();
        cr.setEndDate(LocalDate.now().minusDays(1));
        Set<ConstraintViolation<CareRequest>> violations =
                validator.validate(cr);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("endDate"));
    }

    @Test
    void endDate_beforeStartDate_failsAssertTrue() {
        // Beide Daten in Zukunft, aber Enddatum vor Startdatum → @AssertTrue feuert
        CareRequest cr = validRequest();
        cr.setStartDate(LocalDate.now().plusDays(10));
        cr.setEndDate(LocalDate.now().plusDays(5));
        Set<ConstraintViolation<CareRequest>> violations =
                validator.validate(cr);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("dateRangeValid"));
    }

    @Test
    void endDate_equalsStartDate_failsAssertTrue() {
        // Boundary: gleicher Tag ist nicht "nach" Startdatum → muss fehlschlagen
        CareRequest cr = validRequest();
        LocalDate sameDay = LocalDate.now().plusDays(5);
        cr.setStartDate(sameDay);
        cr.setEndDate(sameDay);
        Set<ConstraintViolation<CareRequest>> violations =
                validator.validate(cr);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("dateRangeValid"));
    }

    @Test
    void owner_null_failsValidation() {
        CareRequest cr = validRequest();
        cr.setOwner(null);
        Set<ConstraintViolation<CareRequest>> violations =
                validator.validate(cr);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("owner"));
    }

    @Test
    void pet_null_failsValidation() {
        CareRequest cr = validRequest();
        cr.setPet(null);
        Set<ConstraintViolation<CareRequest>> violations =
                validator.validate(cr);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("pet"));
    }
}