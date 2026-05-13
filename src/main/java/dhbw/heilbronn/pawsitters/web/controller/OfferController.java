package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.domain.CareRequest;
import dhbw.heilbronn.pawsitters.domain.Offer;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import dhbw.heilbronn.pawsitters.service.OfferService;
import dhbw.heilbronn.pawsitters.web.form.OfferForm;
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
    private final UserRepository userRepository;

    public OfferController(OfferService offerService, UserRepository userRepository) {
        this.offerService = offerService;
        this.userRepository = userRepository;
    }

    // === Host: passende Anfragen browsen ===
    @GetMapping("/host/care-requests")
    public String browseMatchingRequests(@AuthenticationPrincipal UserDetails principal, Model model){
        Long userId = currentUserId(principal);
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

        Long userId = currentUserId(principal);
        offerService.createOffer(userId, id, form);
        return REDIRECT_HOST_OFFERS;
    }

    // === Host: Eigene gesendete Offer ===
    @GetMapping("/host/offers")
    public String hostOffers(@AuthenticationPrincipal UserDetails principal, Model model) {
        Long userId = currentUserId(principal);
        List<Offer> offers = offerService.findOffersByHost(userId);
        model.addAttribute("offers", offers);
        return HOST_OFFERS_VIEW;
    }

    // === Owner: Eingegangene Offers pro CareRequest ===
    @GetMapping("/owner/care-requests/{id}/offers")
    public String careRequestOffers(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal, Model model){
        Long userId = currentUserId(principal);
        List<Offer> offers = offerService.findOffersByCareRequest(userId, id);
        model.addAttribute("offers", offers);
        model.addAttribute("careRequestId", id);
        return OWNER_OFFERS_VIEW;
    }

    // === Owner: Offer annehmen
    @PostMapping("/owner/offers/{offerId}/accept")
    public String acceptOffer(@PathVariable Long offerId, @AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserId(principal);
        offerService.accept(offerId, userId);
        // Zurück zur CareRequest Liste, diese zeigt jetzt den Status MATCHED
        return REDIRECT_OWNER_CARE_REQUESTS;
    }

    // === Hilfsfunktionen ===

    // Email aus Principal -> User aus DB -> UserID
    private Long currentUserId(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Eingeloggter User nicht gefunden"))
                .getId();
    }

    // leeres OfferFOrm für GET weeklyPrice ist null, Validation läuft erst beim POST
    @SuppressWarnings({"DataFlowIssue", "java:S2637"})
    private OfferForm emptyOfferForm() {
        return new OfferForm(null);
    }




}
