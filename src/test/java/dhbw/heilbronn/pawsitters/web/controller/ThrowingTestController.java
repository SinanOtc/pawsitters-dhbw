package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.service.exception.*;
import dhbw.heilbronn.pawsitters.service.exception.EmailAlreadyTakenException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Test-only Controller für GlobalExceptionHandlerTest.
 * Wirft jede gemappte Exception über einen eigenen
 Endpoint, damit der Handler
 * isoliert testbar ist — ohne echte Controller zu mocken.
 *
 * Liegt in src/test/java → nicht Teil des
 Production-Bundles.
 * Top-Level statt Inner Class weil Spring's
 Component-Scan in @WebMvcTest
 * innere Test-Klassen unzuverlässig erfasst.
 */
@Controller
class ThrowingTestController {

    @GetMapping("/test/pet-not-found")
    public String throwPetNotFound() {
        throw new PetNotFoundException(42L);
    }

    @GetMapping("/test/care-request-not-found")
    public String throwCareRequestNotFound() {
        throw new CareRequestNotFoundException(42L);
    }

    @GetMapping("/test/offer-not-found")
    public String throwOfferNotFound() {
        throw new OfferNotFoundException(42L);
    }

    @GetMapping("/test/host-profile-not-found")
    public String throwHostProfileNotFound() {
        throw new HostProfileNotFoundException(42L);
    }

    @GetMapping("/test/owner-profile-not-found")
    public String throwOwnerProfileNotFound() {
        throw new OwnerProfileNotFoundException(42L);
    }

    @GetMapping("/test/offer-not-pending")
    public String throwOfferNotPending() {
        throw new OfferNotPendingException(42L);
    }

    @GetMapping("/test/offer-not-eligible")
    public String throwOfferNotEligible() {
        throw new OfferNotEligibleException(42L);
    }

    @GetMapping("/test/email-already-taken")
    public String throwEmailAlreadyTaken() { throw new EmailAlreadyTakenException("owner@test.de"); }
}