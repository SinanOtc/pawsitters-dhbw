package dhbw.heilbronn.pawsitters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Hardcoded Test-User nur fürs Dev-Profil.
 * In Produktion (kein dev-Profil aktiv) wird diese Klasse nicht geladen,
 * sodass keine Default-Credentials in den Live-Build gelangen.
 */
@Configuration
@Profile("dev")
public class DevUsersConfig {

    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
        UserDetails owner = User.builder()
                .username("dummyOwner@test.de")
                .password(encoder.encode("testPWD123"))
                .roles("OWNER")
                .build();

        UserDetails host = User.builder()
                .username("dummyHost@test.de")
                .password(encoder.encode("testPWD123"))
                .roles("HOST")
                .build();

        return new InMemoryUserDetailsManager(owner, host);
    }
}
