package dhbw.heilbronn.pawsitters.service.exception;


/**
 * Wird erzeugt wenn ein Host versucht ein Offer für eine CareRequest abzugeben,
 * die ihm nicht erlaubt ist, aus diversen Gründen:
 *      - Status nicht OPEN
 *      - PetSpecies passt nicht zu accepted Species
 *      - uvm.
 * Sollte eigentlich nicht passieren, da nur matching Offers angezeigt werden,
 * fängt aber URL-Manipulation ab.
 */
public class OfferNotEligibleException extends RuntimeException {
    public OfferNotEligibleException(Long careRequestId) {
        super("CareRequest nicht möglich für diesen Host: " + careRequestId);
    }
}
