package dhbw.heilbronn.pawsitters.service.exception;

/**
 * Wird geworfen wenn ein OwnerProfile zu einer User-ID nicht existiert.
 * Sollte im Normalbetrieb nicht passieren (jeder eingeloggte Owner hat ein Profil),
 * ist aber Daten-Konsistenz-Safeguard.
 * (Spiegelbild zu HostProfileNotFoundException.)
 */
public class OwnerProfileNotFoundException extends RuntimeException {

    public OwnerProfileNotFoundException(Long userId) {
        super("OwnerProfile zu User-ID nicht gefunden: " + userId);
    }
}
