package dhbw.heilbronn.pawsitters.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Profil eines PetOwners
 * 1:1 zu User, darf ohne ihn nicht exitieren
 * Daten, unabhängig von Login
 */

@Entity
@Table(name = "owner_profiles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OwnerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @NotBlank
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Column(nullable = false)
    private String lastName;

    @NotBlank
    @Column(nullable = false)
    private String address;

    public OwnerProfile(User user, String firstName, String lastName, String address) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }
}
