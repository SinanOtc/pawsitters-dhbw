package dhbw.heilbronn.pawsitters.dto;

import jakarta.validation.constraints.NotBlank;
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
        @Size(max = 256)
        String address
){
}
