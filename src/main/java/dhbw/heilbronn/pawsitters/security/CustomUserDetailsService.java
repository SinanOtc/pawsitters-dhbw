package dhbw.heilbronn.pawsitters.security;

import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Verbindung zwischen User-Entität und Spring Security.
 * Wird bei Login von Spring aufgerufen um edn User aus der DB zu
 * holen und dem Spring internen UserDetails-Format zu geben
 *
 * @Profile("!dev") => Bean wird nur in Prod aktiviert.
 * In Dev bleibt InMemoryUserDetailsManager aus DevUsersConfig bestehen
 */
@Service
@Profile("!dev")
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // User aus DB laden. Bei "nicht gefunden" kein explizites "User existiert nicht"
        // zurückgeben, sonst werden Informationen über gültige Accounts herausgegeben.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Login fehlgeschlagen"));

        // Spring internes UserDetails Objekt bauen
        // "ROLE_" Präfix muss manuell dran, weil Spring bei hasRole("OWNER")
        // wieder ROLE_ vorne anhängt, dann wird es nicht matchen
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHashed(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
