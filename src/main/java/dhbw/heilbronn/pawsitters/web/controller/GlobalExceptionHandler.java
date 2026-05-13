package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.service.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

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
