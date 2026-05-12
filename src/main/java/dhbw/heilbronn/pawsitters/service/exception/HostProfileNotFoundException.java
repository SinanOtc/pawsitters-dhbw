package dhbw.heilbronn.pawsitters.service.exception;

/**
 *
 */
public class HostProfileNotFoundException extends RuntimeException {
    public HostProfileNotFoundException(Long userId) {
        super("Host-Profil zu UserID nicht gefunden: " + userId);
    }
}
