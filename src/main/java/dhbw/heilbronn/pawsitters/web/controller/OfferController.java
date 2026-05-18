package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.domain.CareRequest;
import dhbw.heilbronn.pawsitters.domain.Offer;
import dhbw.heilbronn.pawsitters.security.CurrentUserResolver;
import dhbw.heilbronn.pawsitters.service.OfferService;
import dhbw.heilbronn.pawsitters.dto.OfferForm;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * Routes für Offer, sowohl Hostseitig (passende Anfragen browsen, Offer senden, eigene Offers sehen),
 * als auch Ownerseitig (eingegangene Offers pro CareRequest).
 * Bewusster Pattern-Bruch: ein COntroller für beide Seiten, weil Offer per Definition zwei PArteien verbindet.
 * Auth wird über SecurityConfig geregelt (/host/** -> HOST | /owner/** -> OWNER)
 */
@Controller
public class OfferController {

    private static final String HOST_BROWSE_VIEW = "host/care-requests/list";
    private static final String  HOST_OFFER_FORM_VIEW = "host/care-requests/offer-form";
    private static final String HOST_OFFERS_VIEW = "host/offers/list";
    private static final String OWNER_OFFERS_VIEW = "owner/care-requests/offers";
    private static final String REDIRECT_HOST_OFFERS = "redirect:/host/offers";
    private static final String REDIRECT_OWNER_CARE_REQUESTS = "redirect:/owner/care-requests";


    private final OfferService offerService;
    private final CurrentUserResolver currentUserResolver;

    public OfferController(OfferService offerService, CurrentUserResolver currentUserResolver) {
        this.offerService = offerService;
        this.currentUserResolver = currentUserResolver;
    }

    // === Host: passende Anfragen browsen ===
    @GetMapping("/host/care-requests")
    public String browseMatchingRequests(@AuthenticationPrincipal UserDetails principal, Model model){
        Long userId = currentUserResolver.userId(principal);
        List<CareRequest> matching = offerService.findMatchingRequests(userId);
        model.addAttribute("matchingRequests", matching);
        return HOST_BROWSE_VIEW;
    }

    // === Host: Offer-Form für eine konkrete CareRequest
    @GetMapping("/host/care-requests/{id}/offer")
    public String offerForm(@PathVariable Long id, Model model) {
        // leeres Offer Form, damit Thymeleaf direkt binden kann
        model.addAttribute("offerForm", emptyOfferForm());
        model.addAttribute("careRequestId", id);
        return HOST_OFFER_FORM_VIEW;
    }

    // === Host: Offer senden ===
    @PostMapping("/host/care-requests/{id}/offer")
    public String createOffer(@PathVariable Long id, @Valid @ModelAttribute("offerForm") OfferForm form, BindingResult bindingResult, @AuthenticationPrincipal UserDetails principal, Model model) {
        if(bindingResult.hasErrors()) {
            // careRequestId muss erneut ins Model, sonst kennt das Form-Template
            // den Action-Path-Parameter beim Re-Render nicht mehr
            model.addAttribute("careRequestId", id);
            return HOST_OFFER_FORM_VIEW;
        }

        Long userId = currentUserResolver.userId(principal);
        offerService.createOffer(userId, id, form);
        return REDIRECT_HOST_OFFERS;
    }

    // === Host: Eigene gesendete Offer ===
    @GetMapping("/host/offers")
    public String hostOffers(@AuthenticationPrincipal UserDetails principal, Model model) {
        Long userId = currentUserResolver.userId(principal);
        List<Offer> offers = offerService.findOffersByHost(userId);
        model.addAttribute("offers", offers);
        return HOST_OFFERS_VIEW;
    }

    // === Owner: Eingegangene Offers pro CareRequest ===
    @GetMapping("/owner/care-requests/{id}/offers")
    public String careRequestOffers(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal, Model model){
        Long userId = currentUserResolver.userId(principal);
        List<Offer> offers = offerService.findOffersByCareRequest(userId, id);
        model.addAttribute("offers", offers);
        model.addAttribute("careRequestId", id);
        return OWNER_OFFERS_VIEW;
    }

    // === Owner: Offer annehmen
    @PostMapping("/owner/offers/{offerId}/accept")
    public String acceptOffer(@PathVariable Long offerId, @AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserResolver.userId(principal);
        Offer accepted = offerService.accept(offerId, userId);
        // Zurück zur Offers-Seite derselben CareRequest, damit der User
        // direkt die aktualisierten Status-Badges (ACCEPTED / REJECTED) sieht.
        // careRequestId aus dem zurückgegebenen Offer ableiten — keine User-Eingabe
        // nötig, daher kein zusätzlicher Form-Parameter (kein IDOR-Vektor).
        return "redirect:/owner/care-requests/" + accepted.getCareRequest().getId() + "/offers";
    }

    // === Owner: Offer manuell ablehnen
    @PostMapping("/owner/offers/{offerId}/reject")
    public String rejectOffer(@PathVariable Long offerId, @AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserResolver.userId(principal);
        Offer rejected = offerService.reject(offerId, userId);
        // Zurück zur Offers-Seite derselben CareRequest — User sieht das neue
        // REJECTED-Badge sofort. careRequestId aus dem Offer abgeleitet (kein IDOR).
        return "redirect:/owner/care-requests/" + rejected.getCareRequest().getId() + "/offers";
    }

    // === Hilfsfunktionen ===

    // Leeres OfferForm für GET weeklyPrice ist null, Validation läuft erst beim POST
    @SuppressWarnings({"DataFlowIssue", "java:S2637"})
    private OfferForm emptyOfferForm() {
        return new OfferForm(null);
    }




}
