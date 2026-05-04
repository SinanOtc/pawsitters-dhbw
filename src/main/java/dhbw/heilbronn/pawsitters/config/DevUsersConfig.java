package dhbw.heilbronn.pawsitters.config;

import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import dhbw.heilbronn.pawsitters.service.OwnerService;
import dhbw.heilbronn.pawsitters.web.form.RegisterOwnerForm;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seed Daten für Dev und Tests
 * legt beim App Start zwei Dummy-User in nder DB an,
 * damit man sich ohne Registrierung einloggen kann.
 * Nur in Dev aktiv → in Prod existieren keine Dummy-User
 */
@Configuration
@Profile("dev")
public class DevUsersConfig {

    @Bean
    public CommandLineRunner seedDevUsers(UserRepository userRepository, OwnerService ownerService, PasswordEncoder passwordEncoder) {
        return args -> {
            // Dummy Owner mit komplettem Profil
            if(!userRepository.existsByEmail("dummyOwner@test.de")) {
                ownerService.register(new RegisterOwnerForm(
                        "dummyOwner@test.de",
                        "testPWD123",
                        "DemoMax",
                        "DemoMustermann",
                        "Demostraße 1, 74072 Heilbronn"
                ));
            }

            // Dummy Host als reiner User. Host Profil gibt es noch nicht
            // TODO: Kommt in Kevin seinen Aufgabenbereich. Muss danach angepasst werden
            if(!userRepository.existsByEmail("dummyHost@test.de")) {
                User host = new User(
                        "dummyHost@test.de",
                        passwordEncoder.encode("testPWD123"),
                        UserRole.HOST
                );
                userRepository.save(host);
            }
        };
    }

}
