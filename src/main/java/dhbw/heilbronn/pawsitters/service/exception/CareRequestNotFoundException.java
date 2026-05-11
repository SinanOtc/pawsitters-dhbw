package dhbw.heilbronn.pawsitters.service.exception;

/**
 * Wird geworfen wenn eine Care Request nicht existiert ODER nicht dem angefragten
 * Owner gehört. Beide Fälle bewusst gleich behandelt aus Sicherheitsgründen (URL Manipulation)
 */
public class CareRequestNotFoundException extends RuntimeException {
    public CareRequestNotFoundException(Long id) {

        super("Betreuungsanfrage nicht gefunden: " + id);
    }
}
