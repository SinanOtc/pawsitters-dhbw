package dhbw.heilbronn.pawsitters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Konfiguration, legt fest welche URLs für Auth gebraucht werden
 * und wie Login Flow aussieht.
 */

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                // Startseite, Loginseite, assets
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/owner/register", "/host/register", "/css/**","/js/**").permitAll()
                        // alle Ownerroutes nur für eingeloggte PetOwner
                        .requestMatchers("/owner/**").hasRole("OWNER")
                        // alle Hostroutes nur für eingeloggte Hosts
                        .requestMatchers("/host/**").hasRole("HOST")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );
        return http.build();
    }

    // BCrypt verschlüsselt Passwörter. Nur der Hashwert wird gespeichert
    // Bei Login wird der eingegebene Wert direkt mit dem Hash verglichen
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}