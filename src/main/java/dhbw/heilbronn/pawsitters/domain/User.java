package dhbw.heilbronn.pawsitters.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Allgemeiner-User für die Anmeldung im System
 * Profildaten liegen in Owner- bzw. HostProfile
 */
@Entity
@Table(name = "users")
@Getter
@Setter

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String passwordHashed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    public User(String email, String passwordHashed, UserRole role) {
        this.email = email;
        this. passwordHashed = passwordHashed;
        this.role = role;
    }
}
