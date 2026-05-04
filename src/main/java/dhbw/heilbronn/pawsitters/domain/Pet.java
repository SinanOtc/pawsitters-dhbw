package dhbw.heilbronn.pawsitters.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Haustier eines dazugehörigen PetOwners.
 * Mehrere Pets pro Owner möglich (1:N).
 * Pflichtfelder: name, species, gender, owner.
 * Rest ist optional aus logischen Gründen
 */

@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pet {

    // === Pflichtfelder ===
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetSpecies species;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetGender gender;

    // === optionale Felder ===
    @Size(max = 100)
    @Column(length = 100)
    private String breed;

    @Min(1985)
    @Max(2026)
    private Integer birthYear;

    private boolean chipped;

    @Size(max = 20)
    @Column(length = 20)
    private String chipNumber;

    private boolean vaccinated;

    private boolean neutered;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private OwnerProfile owner;

    public Pet(OwnerProfile owner, String name, PetSpecies species, PetGender gender) {
        this.owner = owner;
        this.name = name;
        this.species = species;
        this.gender = gender;
    }
}
