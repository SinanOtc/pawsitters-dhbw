package dhbw.heilbronn.pawsitters.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Form für das Bearbeiten eines bestehenden Owner Profils
 *
 * WICHTIG:
 *  - bei Update KEIN Passwort und Email Änderung
 *  (das ist zu sicherheitsrelevant und muss in einen eigenen Flow)
 */

public record UpdateOwnerForm(

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotBlank
        @Size(max = 100)
        String street,

        @NotBlank
        @Size(max = 20)
        String streetNumber,

        @NotBlank
        @Pattern(regexp = "\\d{5}")
        String postalCode,

        @NotBlank
        @Size(max = 100)
        String city
){
        public String address() {
                return street + " " + streetNumber + ", " + postalCode + " " + city;
        }
}
