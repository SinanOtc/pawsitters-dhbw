package dhbw.heilbronn.pawsitters.service.exception;

/**
 * Wird geworfen, wenn ein Pet nicht existiert ODER dem aktuellen User nicht gehört.
 * Beide Fälle werden aus Sicherheitsgründen gleich behandelt.
 */

public class PetNotFoundException extends RuntimeException {
    public PetNotFoundException(Long petId) {
        super("Pet nicht gefunden: " + petId);
    }
}
