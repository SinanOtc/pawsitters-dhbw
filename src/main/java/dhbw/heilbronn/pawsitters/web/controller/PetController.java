package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.domain.Pet;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import dhbw.heilbronn.pawsitters.service.PetService;
import dhbw.heilbronn.pawsitters.web.form.PetForm;
import jakarta.validation.Valid;
import
        org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Routes für die Pets eines PetOwners
 * Liste, Anlegen, Bearbeiten, Löschen
 * Zugriff nur für ROLE_OWNER, das regelt schon SecurityConfig
 */

@Controller
@RequestMapping("/owner/pets")
public class PetController {

    private static final String  LIST_VIEW = "owner/pets/list";
    private static final String FORM_VIEW = "owner/pets/form";
    private static final String REDIRECT_LIST = "redirect:/owner/pets";

    private final PetService petService;
    private final UserRepository userRepository;

    public PetController(PetService petService, UserRepository userRepository) {
        this.petService = petService;
        this.userRepository = userRepository;
    }

    // === Liste ===
    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        Long userId = currentUserId(principal);
        model.addAttribute("pets", petService.findAllByOwner(userId));
        return LIST_VIEW;
    }

    // === Anlegen ===
    @GetMapping("/new")
    public String newForm(Model model) {
        // Leeres Form-Objekt damit Thymeleaf die Felder mappen kann
        model.addAttribute("petForm", emptyForm());
        // mode steuert Titel +Action-URL im Form Template
        model.addAttribute("mode", "new");
        return FORM_VIEW;
    }

    @PostMapping("/new")
    public String createSubmit(@Valid @ModelAttribute("petForm") PetForm petForm, BindingResult bindingResult, @AuthenticationPrincipal UserDetails principal, Model model) {
        // Bei Validierungfehlern zurück zu Form
        if(bindingResult.hasErrors()) {
            model.addAttribute("mode", "new");
            return FORM_VIEW;
        }
        Long userId = currentUserId(principal);
        petService.register(userId, petForm);
        return REDIRECT_LIST;
    }

    // === Bearbeiten ===
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal, Model model) {
        Long userId = currentUserId(principal);
        // findByIdOwner prüft die Zugehörigkeit --> fremde Pets erzeugen PetNotFoundException
        Pet pet = petService.findByIdForOwner(id, userId);
        // Form mit aktuellen Werten vorbefüllen
        model.addAttribute("petForm", toForm(pet));
        model.addAttribute("mode", "edit");
        model.addAttribute("petId", id);
        return FORM_VIEW;
    }

    @PostMapping("/{id}/edit")
    public String editSubmit(@PathVariable Long id, @Valid @ModelAttribute("petForm") PetForm petForm, BindingResult bindingResult, @AuthenticationPrincipal UserDetails principal, Model model) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("petId", id);
            return FORM_VIEW;
        }
        Long userId = currentUserId(principal);
        petService.update(id, userId, petForm);
        return REDIRECT_LIST;
    }

    // === Löschen ===
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserId(principal);
        petService.delete(id, userId);
        return REDIRECT_LIST;
    }

    // === Hilfsfunktionen ===

    // E-Mail aus Principal -> User aus DB -> UserID
    // Spring kennt nur die Mail aber der Service brucht aber die UserID
    private Long currentUserId(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Eingeloggter User nicht gefunden"))
                .getId();
    }

    // leeres PetForm für /new (alle Felder null bzw. false)
    // leeres PetForm für GET /new — Felder sind null weil der User noch nichts ausgewählt hat
    // Bean Validation läuft erst beim POST über @Valid, nicht im Konstruktor -> null ist hier sicher
    @SuppressWarnings({"DataFlowIssue", "java:S2637"})
    private PetForm emptyForm() {
    return new PetForm (null, null, null, null, null, false, null, false, false, null);
    }

    // Pet -> PetForm Mapping für das Vorbefüllen in /edit
    private PetForm toForm(Pet pet) {
        return new PetForm(
                pet.getName(),
                pet.getSpecies(),
                pet.getBreed(),
                pet.getGender(),
                pet.getBirthYear(),
                pet.isChipped(),
                pet.getChipNumber(),
                pet.isVaccinated(),
                pet.isNeutered(),
                pet.getDescription()
        );
    }

}
