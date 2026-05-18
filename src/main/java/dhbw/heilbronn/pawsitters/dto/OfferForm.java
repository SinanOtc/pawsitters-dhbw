package dhbw.heilbronn.pawsitters.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Form für ein neues Offer.
 *  - weeklyPrice: Pflicht, vom Host eingegeben.
 *  - message:     Optional, vom Host an den Owner (z. B. Vorstellung,
 *                 Verfügbarkeits-Details). 500 Zeichen max.
 *
 * Host kommt aus Principal, careRequestId aus dem URL-Path.
 */
public record OfferForm(

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false, message = "Wochenpreis muss höher als 0€ sein")
        @Digits(integer = 6, fraction = 2)
        BigDecimal weeklyPrice,

        @Size(max = 500, message = "Nachricht darf maximal 500 Zeichen lang sein")
        String message
) {
}
