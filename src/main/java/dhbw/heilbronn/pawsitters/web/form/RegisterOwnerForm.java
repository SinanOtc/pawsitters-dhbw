package dhbw.heilbronn.pawsitters.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Formular DTO für Registrierung eines neuen PetOwners
 * Wird im Controller an Form gebunden, nicht direkt an die Entität
 *
 */

public record RegisterOwnerForm (

    @NotBlank
    @Email
    @Size(max = 254)
    String email,

    @NotBlank
    @Size(min = 8, max = 100)
    String password,

    @NotBlank
    @Size(max = 100)
    String firstName,

    @NotBlank
    @Size(max = 100)
    String lastName,

    @NotBlank
    @Size(max = 256)
    String address
    ) {

}
