package dhbw.heilbronn.pawsitters.security;

import dhbw.heilbronn.pawsitters.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Zentraler Helper um aus einem Spring Security Principal die UserID aus der DB
 * zu resolven. Wird von allen Controllern injected.
 * Ersetzt die zuvor in jedem Controller duplizierte currentUserId()-Methode.
 * Hintergrund: Spring liefert über @AuthenticationPrincipal nur die E-Mail
 * (als username), der Service-Layer braucht aber die UserID. Vorher war
 * diese Auflösung in 5 Controllern wortwörtlich kopiert (DRY-Verletzung).
 * Liegt im security-Package neben CustomUserDetailsService — beides
 * Auth/Principal-Belange.
 */

@Component
public class CurrentUserResolver {

    private final UserRepository userRepository;

    public CurrentUserResolver(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    /**
     * Resolve die UserID des eingeloggten Users.
     * Wirft IllegalStateException wenn der eingeloggte User nicht in der DB existiert.
     * Sollte im Normalbetrieb nicht passieren (Auth läuft gegen dieselbe DB).
     * Defense-in-Depth gegen inkonsistente States.
     */
    public Long userId(UserDetails principal){
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Eingeloggter User nicht gefunden"))
                .getId();
    }
}
