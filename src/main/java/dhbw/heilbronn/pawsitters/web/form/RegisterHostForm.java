package dhbw.heilbronn.pawsitters.web.form;

import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Formular DTO für Registrierung eines neuen Hosts
 * Wird im Controller an Form gebunden, nicht direkt an die Entität
 *
 */

public record RegisterHostForm (

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
    String address,

    @NotNull
    @Size(min = 1, message = "Mindestens eine Tierart auswählen")
    Set<PetSpecies> acceptedSpecies,

    @NotNull
    LocalDate availableFrom,

    @NotNull
    LocalDate availableUntil,

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    @Digits(integer = 8, fraction = 2)
    BigDecimal pricePerWeek
    ) {

}