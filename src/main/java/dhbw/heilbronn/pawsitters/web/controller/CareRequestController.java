package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.security.CurrentUserResolver;
import dhbw.heilbronn.pawsitters.service.CareRequestService;
import dhbw.heilbronn.pawsitters.service.PetService;
import dhbw.heilbronn.pawsitters.dto.CareRequestForm;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/owner/care-requests")
public class CareRequestController {

    private static final String LIST_VIEW = "owner/care-requests/list";
    private static final String FORM_VIEW = "owner/care-requests/form";
    private static final String REDIRECT_LIST = "redirect:/owner/care-requests";

    private final CareRequestService careRequestService;
    private final PetService petService;
    private final CurrentUserResolver currentUserResolver;

    public CareRequestController(CareRequestService careRequestService, PetService petService, CurrentUserResolver currentUserResolver) {
        this.careRequestService = careRequestService;
        this.petService = petService;
        this.currentUserResolver = currentUserResolver;
    }

    // === Liste ===
    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        Long userId = currentUserResolver.userId(principal);
        model.addAttribute("careRequests", careRequestService.findAllByOwner(userId));
        return LIST_VIEW;
    }

    // === Anlegen ===
    @SuppressWarnings({"DataFlowIssue", "java:S2637"})
    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        Long userId = currentUserResolver.userId(principal);

        // leeres Form Objekt damit Thymeleaf binden kann
        model.addAttribute("careRequestForm", new CareRequestForm(null, null, null));

        // Pets des Owners für das Dropdown im Form
        model.addAttribute("pets", petService.findAllByOwner(userId));

        return FORM_VIEW;
    }

    @PostMapping("/new")
    public String createSubmit(@Valid @ModelAttribute("careRequestForm") CareRequestForm form, BindingResult bindingResult, @AuthenticationPrincipal UserDetails principal, Model model) {

        Long userId = currentUserResolver.userId(principal);

        // Bei Validierungsfehlern zurück zum Form
        if(bindingResult.hasErrors()) {
            // Pet Liste muss erneut ins Model, sonst ist Model bei Re Rendering leer
            model.addAttribute("pets", petService.findAllByOwner(userId));
            return FORM_VIEW;
        }

        careRequestService.register(userId, form);
        return REDIRECT_LIST;
    }
}
