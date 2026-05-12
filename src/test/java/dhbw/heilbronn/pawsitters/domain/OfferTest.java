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

// Domain-Test für Offer. Nutzt den Jakarta Validator direkt,
// gleiches Pattern wie CareRequestTest und HostProfileTest.
class OfferTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory =
                Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    // === Helper ===

    // Gültiges Default-Offer mit voller Objekt-Hierarchie (Host, Owner, Pet, CareRequest).
    // Aufwendig, aber unvermeidlich wegen der ManyToOne-Beziehungen.
    private Offer validOffer() {
        User hostUser = new User("host@t.de", "hash",
                UserRole.HOST);
        HostProfile host = new HostProfile(
                hostUser, "Erika", "Mustermann",
                "Hoststraße 5",
                EnumSet.of(PetSpecies.DOG),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                new BigDecimal("50.00")
        );

        User ownerUser = new User("owner@t.de", "hash",
                UserRole.OWNER);
        OwnerProfile owner = new OwnerProfile(ownerUser,
                "Max", "Muster", "Adresse 1");
        Pet pet = new Pet(owner, "Bello", PetSpecies.DOG,
                PetGender.MALE);
        CareRequest cr = new CareRequest(owner, pet,
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(10));

        return new Offer(host, cr, new
                BigDecimal("60.00"));
    }

    // === Tests ===

    @Test
    void constructor_setsAllFields() {
        Offer offer = validOffer();
        assertThat(offer.getHost()).isNotNull();
        assertThat(offer.getCareRequest()).isNotNull();
        assertThat(offer.getWeeklyPrice()).isEqualByComparingTo("60.00");
    }

    @Test
    void constructor_setsStatusToPending() {
        // Schutzregel: Neue Offers starten IMMER PENDING,nie direkt ACCEPTED/REJECTED
        Offer offer = validOffer();
        assertThat(offer.getStatus()).isEqualTo(OfferStatus
                .PENDING);
    }

    @Test
    void host_null_failsValidation() {
        Offer offer = validOffer();
        offer.setHost(null);
        Set<ConstraintViolation<Offer>> violations =
                validator.validate(offer);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("host"));
    }

    @Test
    void careRequest_null_failsValidation() {
        Offer offer = validOffer();
        offer.setCareRequest(null);
        Set<ConstraintViolation<Offer>> violations =
                validator.validate(offer);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("careRequest"));
    }

    @Test
    void weeklyPrice_zero_failsValidation() {
        // @DecimalMin(inclusive = false) — 0 ist NICHT erlaubt
        Offer offer = validOffer();
        offer.setWeeklyPrice(BigDecimal.ZERO);
        Set<ConstraintViolation<Offer>> violations =
                validator.validate(offer);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("weeklyPrice"));
    }

    @Test
    void weeklyPrice_negative_failsValidation() {
        Offer offer = validOffer();
        offer.setWeeklyPrice(new BigDecimal("-1.00"));
        Set<ConstraintViolation<Offer>> violations =
                validator.validate(offer);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("weeklyPrice"));
    }
}
