package dhbw.heilbronn.pawsitters.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OwnerProfileTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private User validUser() {
        return new User("owner@test.de", "TestPWD", UserRole.OWNER);
    }

    @Test
    void constructor_setAllFields() {
        User user = validUser();
        OwnerProfile profile = new OwnerProfile(user, "Vincenzo Ronaldo", "Schybe", "Am Bahnhof 1, 74072 Heilbronn");

        assertThat(profile.getUser()).isSameAs(user);
        assertThat(profile.getFirstName()).isEqualTo("Vincenzo Ronaldo");
        assertThat(profile.getLastName()).isEqualTo("Schybe");
        assertThat(profile.getAddress()).isEqualTo("Am Bahnhof 1, 74072 Heilbronn");
    }

    @Test
    void firstName_blank_failValidation() {
        OwnerProfile profile = new OwnerProfile(validUser(),"", "Schybe", "Am Bahnhof 1, 74072 Heilbronn");

        Set<ConstraintViolation<OwnerProfile>> violations = validator.validate(profile);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
    }

    @Test
    void lastName_blank_failValidation() {
        OwnerProfile profile = new OwnerProfile(validUser(),"Vincenzo Ronaldo", "", "Am Bahnhof 1, 74072 Heilbronn");

        Set<ConstraintViolation<OwnerProfile>> violations = validator.validate(profile);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
    }

    @Test
    void address_blank_failValidation() {
        OwnerProfile profile = new OwnerProfile(validUser(),"Vincenzo Ronaldo", "Schybe", "");

        Set<ConstraintViolation<OwnerProfile>> violations = validator.validate(profile);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("address"));
    }

    @Test
    void firstName_tooLong_failValidation() {

        String tooLongName = "KevinVincentSinan".repeat(6);

        OwnerProfile profile = new OwnerProfile(validUser(), tooLongName, "Schybe", "Am Bahnhof 1, 74072 Heilbronn");

        Set<ConstraintViolation<OwnerProfile>> violations = validator.validate(profile);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
    }
}
