package dhbw.heilbronn.pawsitters.service.exception;

/**
 * Wird erzeugt, wenn ein Offer nicht existiert oder nicht zur CareRequest des angefragten Owners gehört.
 * Bide Fälle bewusst gleich behandelt, damit URL-Manipulation keinen Info Leak erzeugt.
 */
public class OfferNotFoundException extends RuntimeException {
    public OfferNotFoundException(Long offerId) {

        super("Offer nicht gefunden: " + offerId);
    }
}
