package dhbw.heilbronn.pawsitters.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Form für ein neues Offer. Einziges Feld von User: weeklyPrice.
 * Host kommt aus Principal, careRequestId aus dem URL Path.
 */
public record OfferForm(

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false, message = "Wochenpreis muss höher als 0€ sein")
        @Digits(integer = 6, fraction = 2)
        BigDecimal weeklyPrice
) {
}
