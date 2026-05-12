package dhbw.heilbronn.pawsitters.service;

import dhbw.heilbronn.pawsitters.domain.HostProfile;
import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import dhbw.heilbronn.pawsitters.repository.HostProfileRepository;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import dhbw.heilbronn.pawsitters.service.exception.EmailAlreadyTakenException;
import dhbw.heilbronn.pawsitters.service.exception.HostProfileNotFoundException;
import dhbw.heilbronn.pawsitters.web.form.RegisterHostForm;
import dhbw.heilbronn.pawsitters.web.form.UpdateHostForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logik für Host-Registrierung und Profilverwaltung.
 * Schema wie bei OwnerService.
 * Host ist als USER(role=HOST) + HostProfile umgesetzt.
 */
public class HostService {

    private final UserRepository userRepository;
    private final HostProfileRepository hostProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public HostService(UserRepository userRepository, HostProfileRepository hostProfileRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.hostProfileRepository = hostProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registreiert einen neuen Host. legt User + HostProfile in einer Transaktion zusammen.
     * Erzeugt EmailAlreadyTakenException wenn Email bereits vergeben ist.
     * (Über alle Rollen hinweg: Eine Email -> ein Account)
     */
    @Transactional
    public HostProfile register(RegisterHostForm form) {
        if(userRepository.existsByEmail(form.email())) {
            throw new EmailAlreadyTakenException(form.email());
        }

        // PWD hashen bevor es in die DB geht
        User user = new User(form.email(), passwordEncoder.encode(form.password()), UserRole.HOST);
        user = userRepository.save(user);

        HostProfile profile = new HostProfile(
                user,
                form.firstName(),
                form.lastName(),
                form.address(),
                form.acceptedSpecies(),
                form.availableFrom(),
                form.availableUntil(),
                form.pricePerWeek()
        );
        return hostProfileRepository.save(profile);
    }

    /**
     * Lädt Profil eines eingeloggten Hosts.
     * Wirft HostProfilNotFoundException als Konsistenzsciherung.
     */
    @Transactional(readOnly = true)
    public HostProfile findByUserId(Long userId) {
        return hostProfileRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new HostProfileNotFoundException(userId));
    }

    /**
     * Profilupdate. Lookup direkt im Repository ansatt findByUserId.
     */
    @Transactional
    public HostProfile update(Long userId, UpdateHostForm form) {
        HostProfile profile = hostProfileRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new HostProfileNotFoundException(userId));

        profile.setFirstName(form.firstName());
        profile.setLastName(form.lastName());
        profile.setAddress(form.address());
        profile.setAcceptedSpecies(form.acceptedSpecies());
        profile.setAvailableFrom(form.availableFrom());
        profile.setAvailableUntil(form.availableUntil());
        profile.setPricePerWeek(form.pricePerWeek());

        return profile;
    }
}
