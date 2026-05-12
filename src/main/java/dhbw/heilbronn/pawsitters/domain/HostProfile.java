package dhbw.heilbronn.pawsitters.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

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

    // === Stammdaten (Gleich wie Owner) ===

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

    // === Host spezifische Felder ===

    // Set<Enum> als Element Collection mit eigener Join Tabelle.
    // @Size mind. 1 prüft die Collection Größe --> Mind. 1 Tierart benötigt.
    @NotNull
    @Size(min = 1, message = "Mindestens eine Tierart wird benötigt")
    @ElementCollection(targetClass = PetSpecies.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "host_accepted_species",
            joinColumns = @JoinColumn(name = "host_profile_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "species", nullable = false, length = 32)
    private Set<PetSpecies> acceptedSpecies = EnumSet.noneOf(PetSpecies.class);

    @NotNull
    @FutureOrPresent
    @Column(nullable = false)
    private LocalDate availableFrom;

    @NotNull
    @FutureOrPresent
    @Column(nullable = false)
    private LocalDate availableUntil;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false, message = "Preis pro Woche muss höher als 0€ sein")
    @Digits(integer = 6, fraction = 2)
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal pricePerWeek;

    // 8 Pflichtfelder ohne sinnvolle Subgruppierungen
    @SuppressWarnings("java:S107")
    public HostProfile(User user, String firstName, String lastName, String address, Set<PetSpecies> acceptedSpecies, LocalDate availableFrom, LocalDate availableUntil, BigDecimal pricePerWeek) {
        this.user = user;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;

        this.acceptedSpecies = (acceptedSpecies == null || acceptedSpecies.isEmpty()) ? EnumSet.noneOf(PetSpecies.class) : EnumSet.copyOf(acceptedSpecies);
        this.availableFrom = availableFrom;
        this.availableUntil = availableUntil;
        this.pricePerWeek = pricePerWeek;
    }

    /**
     * Constraint: availableFrom muss VOR availableUntil liegen.
     * @Future und @FutureOrPresent prüfen schon, dass beide zeitlich gültig sind.
     */
    @AssertTrue(message = "Verfügbarkeit-Enddatum muss nach dem Startdatum liegen")
    public boolean isAvailabilityRangeValid() {
        if(availableFrom == null || availableUntil == null) {
            return true;
        }
        return availableUntil.isAfter(availableFrom);
    }


}
