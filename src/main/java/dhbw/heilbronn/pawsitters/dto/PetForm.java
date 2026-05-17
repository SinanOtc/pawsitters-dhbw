package dhbw.heilbronn.pawsitters.dto;

import dhbw.heilbronn.pawsitters.domain.PetGender;
import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import jakarta.validation.constraints.*;

/**
 * Form für Pet anlegen UND bearbeiten.
 * Hier reicht eine Form, weil die Felder immer identisch bleiben
 */
public record PetForm(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        PetSpecies species,

        @Size(max = 100)
        String breed,

        @NotNull
        PetGender gender,

        @Min(1985)
        @Max(2026)
        Integer birthYear,

        boolean chipped,

        @Size(max = 20)
        String chipNumber,

        boolean vaccinated,

        boolean neutered,

        @Size(max = 500)
        String description
) {
    /**
     * - chipped == true → chipNumber muss befüllt sein
     * - chipped == false → chipNumber muss leer sein.
     * Spring ruft Methode automatisch auf
     */
    @AssertTrue(message = "Chip-Nummer ist Pflicht wenn das Tier gechippt ist")
    public boolean isChipDataConsistent() {
        boolean hasChipNumber = chipNumber != null && !chipNumber.isBlank();
        return chipped == hasChipNumber;
    }
}
