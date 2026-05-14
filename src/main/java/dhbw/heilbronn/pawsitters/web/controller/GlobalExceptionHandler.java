package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.service.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Zentraler Exception-Handler für die ganze App.
 * Mapped Domain-Exceptions auf konsistente Error-Pages
 statt Default-500-Stacktrace.
 * 404 (Not Found) → Resource existiert nicht oder gehört nicht zum eingeloggten User
 *   (Wir leaken bewusst nicht die Unterscheidung — Schutz vor Probing-Angriffen)
 * 409 (Conflict)  → Resource existiert, aber Aktion im aktuellen State nicht erlaubt
 * Wichtig: Wir benutzen ModelAndView statt
 * @ResponseStatus + String-View.
 * @ResponseStatus auf @ExceptionHandler triggert intern sendError() → der Default-BasicErrorController übernimmt das Rendering
 * und unsere View
 * wird verworfen. ModelAndView.setStatus(...) setzt den Status ohne
 * sendError, View wird normal gerendert.
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
    public ModelAndView handleNotFound(RuntimeException e){
        ModelAndView mav = new ModelAndView("error/404");
        mav.addObject("message", e.getMessage());
        mav.setStatus(HttpStatus.NOT_FOUND);
        return mav;
    }

    @ExceptionHandler({
            OfferNotPendingException.class,
            OfferNotEligibleException.class
    })
    public ModelAndView handleConflict(RuntimeException e){
        ModelAndView mav = new ModelAndView("error/409");
        mav.addObject("message", e.getMessage());
        mav.setStatus(HttpStatus.CONFLICT);
        return mav;
    }
}
