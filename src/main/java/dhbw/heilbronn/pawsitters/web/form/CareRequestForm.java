package dhbw.heilbronn.pawsitters.web.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Form für Betreuungsanfrage anlegen.
 * Kein Formupdate nötig, Statuswechsel passiert Worklflowbasiert über den Service.
 * Nie direkt durch den User.
 */
public record CareRequestForm(

        @NotNull
        Long petId,

        @NotNull
        @Future
        LocalDate startDate,

        @NotNull
        @Future
        LocalDate endDate

) {
    /**
     * Enddatum muss nach Startdatum liegen.
     */
    @AssertTrue(message = "Enddatum muss nach Startdatum liegen")
    public boolean isDateRangeValid() {
        if(startDate == null || endDate == null) {
            return true;
        }
        return endDate().isAfter(startDate);
    }
}
