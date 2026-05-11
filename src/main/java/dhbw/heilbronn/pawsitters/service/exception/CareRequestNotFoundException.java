package dhbw.heilbronn.pawsitters.service.exception;

public class CareRequestNotFoundException extends RuntimeException {
    public CareRequestNotFoundException(Long id) {

        super("Betreuungsanfrage nicht gefunden: " + id);
    }
}
