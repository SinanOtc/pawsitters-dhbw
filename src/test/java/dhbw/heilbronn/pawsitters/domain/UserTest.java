package dhbw.heilbronn.pawsitters.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void constructor_setAllFields() {
        User user = new User("TestOwner@test.de", "TestPWD", UserRole.OWNER);

        assertThat(user.getEmail()).isEqualTo("TestOwner@test.de");
        assertThat(user.getPasswordHashed()).isEqualTo("TestPWD");
        assertThat(user.getRole()).isEqualTo(UserRole.OWNER);
    }

    @Test
    void email_invalidFormat_failValidation() {
        User user = new User("falschesFormat?", "TestPWD2", UserRole.OWNER);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void email_blank_failValidation() {
        User user = new User("", "TestPWD3", UserRole.OWNER);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    void role_null_failValidation() {
        User user = new User("owner@test.de", "TestPWDxxx", null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("role"));
    }
}
