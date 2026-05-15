package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.domain.HostProfile;
import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import dhbw.heilbronn.pawsitters.security.CurrentUserResolver;
import dhbw.heilbronn.pawsitters.service.HostService;
import dhbw.heilbronn.pawsitters.service.exception.EmailAlreadyTakenException;
import dhbw.heilbronn.pawsitters.web.form.RegisterHostForm;
import dhbw.heilbronn.pawsitters.web.form.UpdateHostForm;
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

import java.util.EnumSet;

/**
 * Routes für Host: Registrierung, Profil anzeigen, Profil bearbeiten.
 * /host/register ist public. Die anderen nur für ROLE_HOST.
 */
@Controller
@RequestMapping("/host")
public class HostController {

    private static final String REGISTER_VIEW = "host/register";
    private static final String PROFILE_VIEW = "host/profile";
    private static final String EDIT_VIEW = "host/profile-edit";

    private final HostService hostService;
    private final CurrentUserResolver currentUserResolver;

    public HostController(HostService hostService, CurrentUserResolver currentUserResolver) {
        this.hostService = hostService;
        this.currentUserResolver = currentUserResolver;
    }

    // === Registrierung ===
    @GetMapping("/register")
    public String registerForm(Model model) {
        // Leeres Formobjekt, damit Thmyeleaf binden kann
        model.addAttribute("registerForm", emptyRegisterForm());
        // Alle PetSpecies für Multiselect Checkbox in Form
        model.addAttribute("allSpecies", PetSpecies.values());

        return REGISTER_VIEW;
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("registerForm") RegisterHostForm form, BindingResult bindingResult, Model model) {
        if(bindingResult.hasErrors()) {
            // AllSpecies erneut in das Model, sonst hat das Formtemplate kein Checkbox Zustand. Tests ergeben also Sinn
            model.addAttribute("allSpecies", PetSpecies.values());

            return REGISTER_VIEW;
        }

        try {
            hostService.register(form);
        }
        catch(EmailAlreadyTakenException e) {
            bindingResult.rejectValue("email", "email.alreadyTaken", "Diese E-Mail existiert bereits.");
            model.addAttribute("allSpecies", PetSpecies.values());

            return REGISTER_VIEW;
        }

        return "redirect:/login?registered";
    }

    // === Profil anzeigen ===
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails principal, Model model) {
        HostProfile profile = hostService.findByUserId(currentUserResolver.userId(principal));
        model.addAttribute("profile", profile);

        return PROFILE_VIEW;
    }

    // === Profil bearbeiten ===
    @GetMapping("/profile/edit")
    public String editForm(@AuthenticationPrincipal UserDetails principal, Model model){
        HostProfile profile = hostService.findByUserId(currentUserResolver.userId(principal));
        model.addAttribute("updateForm", toForm(profile));
        model.addAttribute("allSpecies", PetSpecies.values());

        return EDIT_VIEW;
    }

    @PostMapping("/profile/edit")
    public String editSubmit(@Valid @ModelAttribute("updateForm") UpdateHostForm form, BindingResult bindingResult, @AuthenticationPrincipal UserDetails principal, Model model) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("allSpecies", PetSpecies.values());

            return EDIT_VIEW;
        }
        hostService.update(currentUserResolver.userId(principal), form);
        return "redirect:/host/profile";
    }

    // === Hilfsfunktionen ===

    // leeres RegisterHostForm für GET /register
    @SuppressWarnings({"DataFlowIssue", "java:S2637"})
    private RegisterHostForm emptyRegisterForm() {
        return new RegisterHostForm(
                "","","","","",
                EnumSet.noneOf(PetSpecies.class),
                null, null, null
        );
    }

    // HostProfile -> UpdateHostForm für Edit-Prefill
    private UpdateHostForm toForm(HostProfile profile) {
        return new UpdateHostForm(
                profile.getFirstName(),
                profile.getLastName(),
                profile.getAddress(),
                profile.getAcceptedSpecies(),
                profile.getAvailableFrom(),
                profile.getAvailableUntil(),
                profile.getPricePerWeek()
        );
    }
}
