package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.service.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Zentraler Exception-Handler für die ganze App.
 * Mapped Domain-Exceptions auf konsistente Error-Pages statt Default-500-Stacktrace.
 * 404 (Not Found) → Resource existiert nicht oder gehört nicht zum eingeloggten User
 * (Wir leaken bewusst nicht die Unterscheidung — Schutz vor Probing-Angriffen)
 * 409 (Conflict)  → Resource existiert, aber Aktion im aktuellen State nicht erlaubt
 * Unerwartete RuntimeExceptions bleiben absichtlich ungefangen → Spring's
 * Default-Handler (500) damit echte Bugs sichtbar bleiben.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            PetNotFoundException.class,
            CareRequestNotFoundException.class,
            OfferNotFoundException.class,
            HostProfileNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(RuntimeException e, Model model){
        model.addAttribute("message", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler({
            OfferNotPendingException.class,
            OfferNotEligibleException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(RuntimeException e, Model model){
        model.addAttribute("message", e.getMessage());
        return "error/409";
    }



}
