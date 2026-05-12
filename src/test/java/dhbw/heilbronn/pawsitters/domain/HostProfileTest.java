package dhbw.heilbronn.pawsitters.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

// Domain-Test für HostProfile. Nutzt den Jakarta Validator direkt,
// gleiches Pattern wie OwnerProfileTest und CareRequestTest.
class HostProfileTest {

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

    // Gültiges Default-HostProfile. Edge-Case-Tests starten hier und brechen genau ein Feld.
    private HostProfile validProfile() {
        User user = new User("h@t.de", "hash", UserRole.HOST);
        return new HostProfile(
                user,
                "Erika",
                "Mustermann",
                "Hoststraße 5",
                EnumSet.of(PetSpecies.DOG, PetSpecies.CAT),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                new BigDecimal("50.00")
        );
    }

    // === Tests ===

    @Test
    void constructor_setsAllFields() {
        HostProfile hp = validProfile();
        assertThat(hp.getFirstName()).isEqualTo("Erika");
        assertThat(hp.getLastName()).isEqualTo("Mustermann");
        assertThat(hp.getAddress()).isEqualTo("Hoststraße 5");
        assertThat(hp.getAcceptedSpecies()).containsExactlyInAnyOrder
                (PetSpecies.DOG, PetSpecies.CAT);

        assertThat(hp.getPricePerWeek()).isEqualByComparingTo("50.00");
    }

    @Test
    void constructor_nullAcceptedSpecies_doesNotThrow() {
        // Defensive Null-Behandlung im Konstruktor — sonst NPE statt sauberer Validation-Meldung
        User user = new User("h@t.de", "hash", UserRole.HOST);
        assertThatCode(() -> new HostProfile(
                user, "Erika", "Mustermann", "Hoststraße 5",
                null,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                new BigDecimal("50.00")
        )).doesNotThrowAnyException();
    }

    @Test
    void constructor_emptyAcceptedSpecies_doesNotThrow() {
        // Bug-Fix-Regression-Test gegenüber Kevins alter Implementierung:
        // EnumSet.copyOf(...) wirft IllegalArgumentException bei leerer Menge.
        // Unser Konstruktor prüft .isEmpty() defensiv.
        User user = new User("h@t.de", "hash", UserRole.HOST);
        assertThatCode(() -> new HostProfile(
                user, "Erika", "Mustermann", "Hoststraße 5",
                EnumSet.noneOf(PetSpecies.class),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                new BigDecimal("50.00")
        )).doesNotThrowAnyException();
    }

    @Test
    void firstName_blank_failsValidation() {
        HostProfile hp = validProfile();
        hp.setFirstName("");
        Set<ConstraintViolation<HostProfile>> violations =
                validator.validate(hp);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("firstName"));
    }

    @Test
    void acceptedSpecies_empty_failsValidation() {
        // @Size(min = 1) auf der Collection — mind. eine Tierart Pflicht
        HostProfile hp = validProfile();
        hp.setAcceptedSpecies(EnumSet.noneOf(PetSpecies.class));
        Set<ConstraintViolation<HostProfile>> violations =
                validator.validate(hp);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("acceptedSpecies"));
    }

    @Test
    void availableFrom_inPast_failsValidation() {
        HostProfile hp = validProfile();
        hp.setAvailableFrom(LocalDate.now().minusDays(1));
        Set<ConstraintViolation<HostProfile>> violations =
                validator.validate(hp);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("availableFrom"));
    }

    @Test
    void availableUntil_beforeAvailableFrom_failsAssertTrue() {
        // Beide Daten in Zukunft, aber until vor from → @AssertTrue feuert
        HostProfile hp = validProfile();
        hp.setAvailableFrom(LocalDate.now().plusDays(20));
        hp.setAvailableUntil(LocalDate.now().plusDays(10));
        Set<ConstraintViolation<HostProfile>> violations =
                validator.validate(hp);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("availabilityRangeValid"));
    }

    @Test
    void availableUntil_equalsAvailableFrom_failsAssertTrue() {
        // Boundary: gleicher Tag ist nicht "nach" Startdatum
        HostProfile hp = validProfile();
        LocalDate sameDay = LocalDate.now().plusDays(5);
        hp.setAvailableFrom(sameDay);
        hp.setAvailableUntil(sameDay);
        Set<ConstraintViolation<HostProfile>> violations =
                validator.validate(hp);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("availabilityRangeValid"));
    }

    @Test
    void pricePerWeek_zero_failsValidation() {
        // @DecimalMin(inclusive = false) — 0 ist NICHT erlaubt
        HostProfile hp = validProfile();
        hp.setPricePerWeek(BigDecimal.ZERO);
        Set<ConstraintViolation<HostProfile>> violations =
                validator.validate(hp);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("pricePerWeek"));
    }

    @Test
    void pricePerWeek_negative_failsValidation() {
        HostProfile hp = validProfile();
        hp.setPricePerWeek(new BigDecimal("-1.00"));
        Set<ConstraintViolation<HostProfile>> violations =
                validator.validate(hp);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("pricePerWeek"));
    }
}