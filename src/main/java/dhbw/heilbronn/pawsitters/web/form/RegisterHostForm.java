package dhbw.heilbronn.pawsitters.web.form;

import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Form für Hostregistrierung. Vereint Logindaten mit Profildaten.
 * Service teilt später in User + HostProfile
 */
public record RegisterHostForm (
        @NotBlank
        @Email
        @Size(max = 256)
        String email,

        @NotBlank
        @Size(min = 8, max = 100, message = "Passwort muss mindestens 8 Zeichen enthalten")
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
        @FutureOrPresent
        LocalDate availableFrom,

        @NotNull
        @Future
        LocalDate availableUntil,

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false, message = "Preis pro Woche muss höher als 0€ sein")
        @Digits(integer = 6, fraction = 2)
        BigDecimal pricePerWeek
){
    /**
     * Constraint: availableFrom muss VOR availableUntil liegen.
     * Gleicher Aufbau wie bei den anderen Timeconstraints.
     */
    @AssertTrue(message = "Verfügbarkeits-Startdatum muss vor dem Enddateum liegen")
    public boolean isAvailabilityRangeValid() {
        if(availableFrom == null || availableUntil == null){
            return true;
        }
        return availableUntil.isAfter(availableFrom);
    }
}
