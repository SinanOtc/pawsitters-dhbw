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
@RequestMapping("/pets")
public class PetController {

    private static final String  LIST_VIEW = "owner/pets/list";
    private static final String FORM_VIEW = "owner/pets/form";

    private final PetService petService;
    private final UserRepository userRepository;

    public PetController(PetService petService, UserRepository userRepository) {
        this.petService = petService;
        this.userRepository = userRepository;
    }

    // === Liste ===
    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model)

}
