package dhbw.heilbronn.pawsitters.web.form;

import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Form für Host-Profil-Bearbeitung. Identisch zu RegisterHostForm, aber ohne PWD und Email,
 * die nicht im Profil-Edit geändert werden.
 */
public record UpdateHostForm(
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
        @FutureOrPresent
        LocalDate availableFrom,

        @NotNull
        @Future
        LocalDate availableUntil,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false, message = "Preis pro Woche muss höher als 0€ sein")
        @Digits(integer = 6, fraction = 2)
        BigDecimal pricePerWeek
) {
    @AssertTrue(message = "Verfügbarkeits-Startdatum muss vor dem Enddatum liegen")
    public boolean isAvailabilityRangeValid() {
        if(availableFrom == null || availableUntil == null){
            return true;
        }
        return availableUntil.isAfter(availableFrom);
    }
}
