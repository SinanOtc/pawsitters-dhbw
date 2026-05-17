package dhbw.heilbronn.pawsitters.service;

import dhbw.heilbronn.pawsitters.domain.OwnerProfile;
import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import dhbw.heilbronn.pawsitters.repository.OwnerProfileRepository;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import dhbw.heilbronn.pawsitters.service.exception.EmailAlreadyTakenException;
import dhbw.heilbronn.pawsitters.service.exception.OwnerProfileNotFoundException;
import dhbw.heilbronn.pawsitters.dto.RegisterOwnerForm;
import dhbw.heilbronn.pawsitters.dto.UpdateOwnerForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logik für PetOwner.
 *
 * "PetOwner" ist als User(role=OWNER) + OwnerProfile umgesetzt.
 * Diese Klasse kapselt beide Entitäten sodass der Controller sich nicht darum kümmern muss
 */
@Service
public class OwnerService {

    private final UserRepository userRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public OwnerService(UserRepository userRepository, OwnerProfileRepository ownerProfileRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.ownerProfileRepository = ownerProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Legt User + OwnerProfile in einer Transaktion an, um bei Fehlern keine Unvollständigen
     * Objekte in der DB zu erzeugen
     * @throws EmailAlreadyTakenException wenn die Email schon vergeben ist
     */
    @Transactional
    public OwnerProfile register(RegisterOwnerForm form) {
        if(userRepository.existsByEmail(form.email())) {
            throw new EmailAlreadyTakenException(form.email());
        }

        String passwordHashed = passwordEncoder.encode(form.password());

        User user = new User(form.email(), passwordHashed, UserRole.OWNER);
        User savedUser = userRepository.save(user);

        OwnerProfile profile = new OwnerProfile(savedUser, form.firstName(), form.lastName(), form.address());

        return ownerProfileRepository.save(profile);
    }

    /**
     * Lädt das Profil zu einer User ID.
     * readOnly = true → Optimierung, kein Dirty-Check nötig.
     * Wirft OwnerProfileNotFoundException wenn kein Profil existiert — wird im
     * GlobalExceptionHandler auf 404 gemapped (User-freundlich, kein Stacktrace).
     */
    @Transactional(readOnly = true)
    public OwnerProfile findByUserId(Long userId) {
        return ownerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new OwnerProfileNotFoundException(userId));
    }

    /**
     * Update der änderbaren Felder
     */
    @Transactional
    public OwnerProfile update(Long userId, UpdateOwnerForm form) {
        OwnerProfile profile = ownerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new OwnerProfileNotFoundException(userId));
        profile.setFirstName(form.firstName());
        profile.setLastName(form.lastName());
        profile.setAddress(form.address());

        return profile;
    }
}
