package dhbw.heilbronn.pawsitters.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

/** Profil-Daten getrennt von {@link User}, weil Login-Felder und Domain-Felder unterschiedliche Lebenszyklen haben. */
@Entity
@Table(name = "host_profiles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HostProfile {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @OneToOne(optional = false)
   @JoinColumn(name = "user_id", unique = true, nullable = false)
   private User user;

   @NotBlank
   @Size(max = 100)
   @Column(nullable = false, length = 100)
   private String firstName;

   @NotBlank
   @Size(max = 100)
   @Column(nullable = false, length = 100)
    private String lastName;
   
   @NotBlank
   @Size(max = 256)
   @Column(nullable = false, length = 256)
   private String address;

   @NotNull
   @Size(min = 1, message = "Mindestens eine Tierart muss akzeptiert werden")
   @ElementCollection(targetClass = PetSpecies.class, fetch = FetchType.EAGER)
   @CollectionTable(
            name = "host_accepted_species",
            joinColumns = @JoinColumn(name = "host_profile_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "species", nullable = false, length = 32)
    private Set<PetSpecies> acceptedSpecies = EnumSet.noneOf(PetSpecies.class);

    @NotNull
    @Column(nullable = false)
    private LocalDate availableFrom;

    @NotNull
    @Column(nullable = false)
    private LocalDate availableUntil;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true)
    @Digits(integer = 8, fraction = 2)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerWeek;

    public HostProfile(User user,
                       String firstName,
                       String lastName,
                       String address,
                       Set<PetSpecies> acceptedSpecies,
                       LocalDate availableFrom,
                       LocalDate availableUntil,
                       BigDecimal pricePerWeek) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.acceptedSpecies = acceptedSpecies == null
                ? EnumSet.noneOf(PetSpecies.class)
                : EnumSet.copyOf(acceptedSpecies);
        this.availableFrom = availableFrom;
        this.availableUntil = availableUntil;
        this.pricePerWeek = pricePerWeek;
    }
}
