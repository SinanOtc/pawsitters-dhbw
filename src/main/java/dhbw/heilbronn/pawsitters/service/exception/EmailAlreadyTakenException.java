package dhbw.heilbronn.pawsitters.service.exception;

/**
 * Wird von OwnerService geworfen, wenn jemand sich mit einer
 * Email registrieren will, die schon vergeben ist
 */

public class EmailAlreadyTakenException extends RuntimeException {

    public EmailAlreadyTakenException(String email) {
        super("E-Mail existiert bereits: " + email);
    }

}
