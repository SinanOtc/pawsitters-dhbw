package dhbw.heilbronn.pawsitters.service;

import dhbw.heilbronn.pawsitters.domain.HostProfile;
import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import dhbw.heilbronn.pawsitters.repository.HostProfileRepository;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import dhbw.heilbronn.pawsitters.service.exception.EmailAlreadyTakenException;
import dhbw.heilbronn.pawsitters.web.form.RegisterHostForm;
import dhbw.heilbronn.pawsitters.web.form.UpdateHostForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Kapselt User + HostProfile, damit Controller pro Use Case nur ein Objekt sieht. */
@Service
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
     * Legt User + HostProfile in einer Transaktion an — schlägt eines fehl, wird das andere zurückgerollt.
     * @throws EmailAlreadyTakenException wenn die Email schon vergeben ist
     */
    @Transactional
    public HostProfile register(RegisterHostForm form) {
        if(userRepository.existsByEmail(form.email())) {
            throw new EmailAlreadyTakenException(form.email());
        }

        String passwordHashed = passwordEncoder.encode(form.password());

        User user = new User(form.email(), passwordHashed, UserRole.HOST);
        User savedUser = userRepository.save(user);

        HostProfile profile = new HostProfile(
                savedUser,
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
     * @throws IllegalStateException wenn kein HostProfile existiert
     *         (Invariante: jeder User mit Rolle HOST hat ein Profil)
     */
    @Transactional(readOnly = true)
    public HostProfile findByUserId(Long userId) {
        return hostProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Kein HostProfile zu UserID"));
    }

    @Transactional
    public HostProfile update(Long userId, UpdateHostForm form) {
        HostProfile profile = hostProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Kein HostProfile zu UserID"));
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
