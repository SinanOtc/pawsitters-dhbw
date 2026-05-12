package dhbw.heilbronn.pawsitters.domain;

/**
 * Status eines Angebots.
 * PENDING  → vom Host gesendet, vom Owner noch nichtentschieden
 * ACCEPTED → Owner hat dieses Offer angenommen
 * REJECTED → Owner hat dieses Offer abgelehnt
 *            (manuell ODER durch Annahme eines anderen Offers — #9)
 */
public enum OfferStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
