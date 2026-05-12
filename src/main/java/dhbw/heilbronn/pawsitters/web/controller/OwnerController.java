package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.domain.OwnerProfile;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import dhbw.heilbronn.pawsitters.service.OwnerService;
import dhbw.heilbronn.pawsitters.service.exception.EmailAlreadyTakenException;
import dhbw.heilbronn.pawsitters.web.form.RegisterOwnerForm;
import dhbw.heilbronn.pawsitters.web.form.UpdateOwnerForm;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Routes für PetOwner: Registrierung, Profil anzeigen und Profil bearbeiten*
 * Registrierung ist Public, Profileinsicht und -Bearbeitung
 * nur für eingeloggte PetOwner zugänglich
 */

@Controller
@RequestMapping("/owner")
public class OwnerController {

    private static final String REGISTER_VIEW = "owner/register";
    private static final String EDIT_VIEW = "owner/profile-edit";

    private final OwnerService ownerService;
    private final UserRepository userRepository;

    public OwnerController(OwnerService ownerService, UserRepository userRepository) {
        this.ownerService = ownerService;
        this.userRepository = userRepository;
    }

    // === Registrierung ===
    @GetMapping("/register")
    public String registerForm(Model model) {
        // leeres Form-Objekt damit Thymeleaf Felder Mappen kann
        model.addAttribute("registerForm",
                new RegisterOwnerForm("","","","","","","",""));
        return REGISTER_VIEW;
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("registerForm") RegisterOwnerForm registerForm, BindingResult bindingResult) {
        // Falls Validierungsfehler aus den Constraints @NotBlank, @Email, @Size auftreten
        if(bindingResult.hasErrors()) {
            return REGISTER_VIEW;
        }

        try {
            ownerService.register(registerForm);
        }
        catch (EmailAlreadyTakenException e) {
            bindingResult.rejectValue("email", "email.allreadyTaken", "Diese E-Mail existiert bereits");
            return REGISTER_VIEW;
        }

        // Nach erfolgreicher Registrierung zurück zum Login
        return "redirect:/login?registered";
    }

    // === Profil anzeigen ====
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails principal, Model model) {
        OwnerProfile profile = loadProfileByEmail(principal.getUsername());
        model.addAttribute("profile", profile);
        model.addAttribute("addressParts", splitAddress(profile.getAddress()));
        return "owner/profile";
    }

    // === Profil bearbeiten ===
    @GetMapping("/profile/edit")
    public String editForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        OwnerProfile profile = loadProfileByEmail(principal.getUsername());
        AddressParts addressParts = splitAddress(profile.getAddress());
        // Form mit Werten befüllen
        model.addAttribute("updateForm", new UpdateOwnerForm(
                profile.getFirstName(),
                profile.getLastName(),
                addressParts.street(),
                addressParts.streetNumber(),
                addressParts.postalCode(),
                addressParts.city()
        ));
        return EDIT_VIEW;
    }

    @PostMapping("/profile/edit")
    public String editSubmit(@Valid UpdateOwnerForm updateForm, BindingResult bindingResult, @AuthenticationPrincipal UserDetails principal) {
        if(bindingResult.hasErrors()) {
            return EDIT_VIEW;
        }
        Long userId = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Eingeloggter User nicht gefunden"))
                .getId();
        ownerService.update(userId, updateForm);
        return "redirect:/owner/profile";
    }

    // === Helper ===
    // E-Mail aus Principal → User aus DB → Profil
    // notwendig, weil Spring nur die E-Mail kennt, aber nicht die dazugehörige UserID
    private OwnerProfile loadProfileByEmail(String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Eingeloggter User nicht gefunden"))
                .getId();
        return ownerService.findByUserId(userId);
    }

    private AddressParts splitAddress(String address) {
        if (address == null || address.isBlank()) {
            return new AddressParts("", "", "", "");
        }

        String[] addressSections = address.split(",", 2);
        String streetSection = addressSections[0].trim();
        String citySection = addressSections.length > 1 ? addressSections[1].trim() : "";

        int lastStreetSpace = streetSection.lastIndexOf(' ');
        String street = lastStreetSpace > 0 ? streetSection.substring(0, lastStreetSpace).trim() : streetSection;
        String streetNumber = lastStreetSpace > 0 ? streetSection.substring(lastStreetSpace + 1).trim() : "";

        int firstCitySpace = citySection.indexOf(' ');
        String postalCode = firstCitySpace > 0 ? citySection.substring(0, firstCitySpace).trim() : "";
        String city = firstCitySpace > 0 ? citySection.substring(firstCitySpace + 1).trim() : citySection;

        return new AddressParts(street, streetNumber, postalCode, city);
    }

    public record AddressParts(String street, String streetNumber, String postalCode, String city) {
    }

}
