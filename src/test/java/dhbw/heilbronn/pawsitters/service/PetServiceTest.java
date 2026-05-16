package dhbw.heilbronn.pawsitters.service;

import dhbw.heilbronn.pawsitters.domain.OwnerProfile;
import dhbw.heilbronn.pawsitters.domain.Pet;
import dhbw.heilbronn.pawsitters.domain.PetGender;
import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import dhbw.heilbronn.pawsitters.repository.PetRepository;
import dhbw.heilbronn.pawsitters.service.exception.PetNotFoundException;
import dhbw.heilbronn.pawsitters.dto.PetForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private OwnerService ownerService;

    @InjectMocks
    private PetService petService;

    private OwnerProfile owner;
    private PetForm validForm;

    @BeforeEach
    void setUp() {
        // OwnerProfile mit gesetzter ID, weil das Repository nach owner.getId() filtert
        User user = new User("o@t.de", "h", UserRole.OWNER);
        owner = new OwnerProfile(user, "Max", "Muster", "Adresse 1");
        owner.setId(7L);

        validForm = new PetForm(
                "Bello",
                PetSpecies.DOG,
                "Labrador",
                PetGender.MALE,
                2020,
                true,
                "12345678901234567",
                true,
                false,
                "Mag keine Katzen"
        );
    }

    @Test
    void register_validForm_savesPetWithOwner() {
        when(ownerService.findByUserId(1L)).thenReturn(owner);
        when(petRepository.save(any(Pet.class))).thenAnswer(inv -> inv.getArgument(0));

        Pet result = petService.register(1L, validForm);

        assertThat(result.getName()).isEqualTo("Bello");
        assertThat(result.getSpecies()).isEqualTo(PetSpecies.DOG);
        assertThat(result.getOwner()).isSameAs(owner);
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void register_appliesAllOptionalFields() {
        when(ownerService.findByUserId(1L)).thenReturn(owner);
        when(petRepository.save(any(Pet.class))).thenAnswer(inv -> inv.getArgument(0));

        Pet result = petService.register(1L, validForm);

        assertThat(result.getBreed()).isEqualTo("Labrador");
        assertThat(result.getBirthYear()).isEqualTo(2020);
        assertThat(result.isChipped()).isTrue();
        assertThat(result.getChipNumber()).isEqualTo("12345678901234567");
        assertThat(result.isVaccinated()).isTrue();
        assertThat(result.isNeutered()).isFalse();
        assertThat(result.getDescription()).isEqualTo("Mag keine Katzen");
    }

    @Test
    void findAllByOwner_returnsOwnersPets() {
        Pet pet1 = new Pet(owner, "Bello", PetSpecies.DOG, PetGender.MALE);
        Pet pet2 = new Pet(owner, "Mauzi", PetSpecies.CAT, PetGender.FEMALE);
        when(ownerService.findByUserId(1L)).thenReturn(owner);
        when(petRepository.findByOwnerId(7L)).thenReturn(List.of(pet1, pet2));

        List<Pet> pets = petService.findAllByOwner(1L);

        assertThat(pets).containsExactly(pet1, pet2);
    }

    @Test
    void findByIdForOwner_existing_returnsPet() {
        Pet pet = new Pet(owner, "Bello", PetSpecies.DOG, PetGender.MALE);
        when(ownerService.findByUserId(1L)).thenReturn(owner);
        when(petRepository.findByIdAndOwnerId(42L, 7L)).thenReturn(Optional.of(pet));

        Pet result = petService.findByIdForOwner(42L, 1L);

        assertThat(result).isSameAs(pet);
    }

    @Test
    void findByIdForOwner_notOwnedByUser_throwsPetNotFound() {
        // Wichtigster Security-Test: das Repo liefert empty wenn das Pet
        // entweder gar nicht existiert ODER einem anderen Owner gehört.
        // Service unterscheidet bewusst NICHT zwischen den Fällen
        when(ownerService.findByUserId(1L)).thenReturn(owner);
        when(petRepository.findByIdAndOwnerId(99L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.findByIdForOwner(99L, 1L))
                .isInstanceOf(PetNotFoundException.class);
    }

    @Test
    void update_existingPet_updatesAllFields() {
        Pet pet = new Pet(owner, "Bello", PetSpecies.DOG, PetGender.MALE);
        when(ownerService.findByUserId(1L)).thenReturn(owner);
        when(petRepository.findByIdAndOwnerId(42L, 7L)).thenReturn(Optional.of(pet));

        PetForm updateForm = new PetForm(
                "Bello-Neu",
                PetSpecies.DOG,
                "Golden Retriever",
                PetGender.MALE,
                2021,
                false,
                null,
                true,
                true,
                "Update beschreibung"
        );
        Pet result = petService.update(42L, 1L, updateForm);

        assertThat(result.getName()).isEqualTo("Bello-Neu");
        assertThat(result.getBreed()).isEqualTo("Golden Retriever");
        assertThat(result.isNeutered()).isTrue();
    }

    @Test
    void delete_existingPet_callsRepositoryDelete() {
        Pet pet = new Pet(owner, "Bello", PetSpecies.DOG, PetGender.MALE);
        when(ownerService.findByUserId(1L)).thenReturn(owner);
        when(petRepository.findByIdAndOwnerId(42L, 7L)).thenReturn(Optional.of(pet));

        petService.delete(42L, 1L);

        verify(petRepository).delete(pet);
    }

    @Test
    void delete_notOwnedByUser_throwsAndDoesNotDelete() {
        // Schutz vor Delete fremder Pets via URL-Manipulation
        when(ownerService.findByUserId(1L)).thenReturn(owner);
        when(petRepository.findByIdAndOwnerId(99L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.delete(99L, 1L))
                .isInstanceOf(PetNotFoundException.class);

        verify(petRepository, never()).delete(any());
    }
}