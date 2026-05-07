package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.domain.HostProfile;
import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;

@Controller
@RequestMapping("/host")
public class HostController {

    private static final String REGISTER_VIEW = "host/register";
    private static final String EDIT_VIEW = "host/profile-edit";

    private final HostService hostService;
    private final UserRepository userRepository;

    public HostController(HostService hostService, UserRepository userRepository) {
        this.hostService = hostService;
        this.userRepository = userRepository;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterHostForm(
                "", "", "", "", "",
                EnumSet.noneOf(PetSpecies.class),
                LocalDate.now(),
                LocalDate.now(),
                BigDecimal.ZERO
        ));
        return REGISTER_VIEW;
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("registerForm") RegisterHostForm registerForm,
                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return REGISTER_VIEW;
        }

        try {
            hostService.register(registerForm);
        } catch (EmailAlreadyTakenException e) {
            bindingResult.rejectValue("email", "email.allreadyTaken", "Diese E-Mail existiert bereits");
            return REGISTER_VIEW;
        }

        return "redirect:/login?registered";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails principal, Model model) {
        HostProfile profile = loadProfileByEmail(principal.getUsername());
        model.addAttribute("profile", profile);
        return "host/profile";
    }

    @GetMapping("/profile/edit")
    public String editForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        HostProfile profile = loadProfileByEmail(principal.getUsername());
        model.addAttribute("updateForm", new UpdateHostForm(
                profile.getFirstName(),
                profile.getLastName(),
                profile.getAddress(),
                profile.getAcceptedSpecies(),
                profile.getAvailableFrom(),
                profile.getAvailableUntil(),
                profile.getPricePerWeek()
        ));
        return EDIT_VIEW;
    }

    @PostMapping("/profile/edit")
    public String editSubmit(@Valid @ModelAttribute("updateForm") UpdateHostForm updateForm,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal UserDetails principal) {
        if (bindingResult.hasErrors()) {
            return EDIT_VIEW;
        }
        Long userId = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Eingeloggter User nicht gefunden"))
                .getId();
        hostService.update(userId, updateForm);
        return "redirect:/host/profile";
    }

    private HostProfile loadProfileByEmail(String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Eingeloggter User nicht gefunden"))
                .getId();
        return hostService.findByUserId(userId);
    }
}
