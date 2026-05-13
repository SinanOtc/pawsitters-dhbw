package dhbw.heilbronn.pawsitters.service.exception;

/**
 * Wird erzeugt wenn versucht wird ein Offer anzunehmen, das nicht (mehr)
 * PENDING
 */
public class OfferNotPendingException extends RuntimeException {
    public OfferNotPendingException(Long offerId) {

        super("Offer kann nicht mehr angenommen werden: " + offerId);
    }
}
