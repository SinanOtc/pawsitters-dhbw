package dhbw.heilbronn.pawsitters.domain;

/**
 * Enum representing the possible statuses of a request in the system.
 * - OPEN: The request is currently active and awaiting further action.
 * - MATCHED: The request has been successfully assigned or matched to another entity.
 * - CLOSED: The request is no longer active, either because it has been completed or terminated.
 */
public enum RequestStatus {
    OPEN,
    MATCHED,
    CLOSED
}
