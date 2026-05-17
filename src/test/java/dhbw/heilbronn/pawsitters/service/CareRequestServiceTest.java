package dhbw.heilbronn.pawsitters.service;

import dhbw.heilbronn.pawsitters.domain.CareRequest;
import dhbw.heilbronn.pawsitters.domain.OwnerProfile;
import dhbw.heilbronn.pawsitters.domain.Pet;
import dhbw.heilbronn.pawsitters.domain.PetGender;
import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import dhbw.heilbronn.pawsitters.domain.RequestStatus;
import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import dhbw.heilbronn.pawsitters.repository.CareRequestRepository;
import dhbw.heilbronn.pawsitters.service.exception.CareRequestNotFoundException;
import dhbw.heilbronn.pawsitters.service.exception.PetNotFoundException;
import dhbw.heilbronn.pawsitters.dto.CareRequestForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Reine Unit-Tests für CareRequestService — kein Spring-Context, alle Collaborators mocken.
// Gleiches Pattern wie OwnerServiceTest und PetServiceTest.
@ExtendWith(MockitoExtension.class)
class CareRequestServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OWNER_ID = 10L;
    private static final Long PET_ID = 42L;
    private static final Long REQUEST_ID = 99L;

    @Mock
    private CareRequestRepository careRequestRepository;

    @Mock
    private OwnerService ownerService;

    @Mock
    private PetService petService;

    @InjectMocks
    private CareRequestService careRequestService;

    private OwnerProfile owner;
    private Pet pet;
    private CareRequestForm validForm;

    @BeforeEach
    void setUp() {
        User user = new User("o@t.de", "hash", UserRole.OWNER);
        user.setId(USER_ID);
        owner = new OwnerProfile(user, "Max", "Muster", "Adresse 1");
        owner.setId(OWNER_ID);
        pet = new Pet(owner, "Bello", PetSpecies.DOG, PetGender.MALE);
        pet.setId(PET_ID);
        validForm = new CareRequestForm(
                PET_ID,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(8)
        );
    }

    // === register ===

    @Test
    void register_validForm_savesRequestWithOpenStatus() {
        when(ownerService.findByUserId(USER_ID)).thenReturn(owner);
        when(petService.findByIdForOwner(PET_ID, USER_ID)).thenReturn(pet);
        // thenAnswer "gib zurück was reinkam" - simuliert das save() Verhalten
        when(careRequestRepository.save(any(CareRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CareRequest result = careRequestService.register(USER_ID, validForm);

        // Schutzregel: Neue Anfragen starten IMMER OPEN
        assertThat(result.getStatus()).isEqualTo(RequestStatus.OPEN);
        assertThat(result.getOwner()).isSameAs(owner);
        assertThat(result.getPet()).isSameAs(pet);
        assertThat(result.getStartDate()).isEqualTo(validForm.startDate());
        assertThat(result.getEndDate()).isEqualTo(validForm.endDate());
    }

    @Test
    void register_petNotOwnedByUser_propagatesPetNotFound() {
        // Pet gehört nicht dem User → PetService wirft, CareRequestService darf NICHTS speichern
        when(ownerService.findByUserId(USER_ID)).thenReturn(owner);
        when(petService.findByIdForOwner(PET_ID, USER_ID))
                .thenThrow(new PetNotFoundException(PET_ID));

        assertThatThrownBy(() -> careRequestService.register(USER_ID, validForm))
                .isInstanceOf(PetNotFoundException.class);

        verify(careRequestRepository, never()).save(any());
    }

    // === findAllByOwner ===

    @Test
    void findAllByOwner_returnsOwnersRequests() {
        CareRequest cr = new CareRequest(owner, pet,
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(9));
        when(ownerService.findByUserId(USER_ID)).thenReturn(owner);
        when(careRequestRepository.findByOwnerId(OWNER_ID)).thenReturn(List.of(cr));

        List<CareRequest> result = careRequestService.findAllByOwner(USER_ID);

        assertThat(result).hasSize(1).containsExactly(cr);
    }

    // === findByIdForOwner ===

    @Test
    void findByIdForOwner_existing_returnsRequest() {
        CareRequest cr = new CareRequest(owner, pet,
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(9));
        when(ownerService.findByUserId(USER_ID)).thenReturn(owner);
        when(careRequestRepository.findByIdAndOwnerId(REQUEST_ID, OWNER_ID))
                .thenReturn(Optional.of(cr));

        CareRequest result = careRequestService.findByIdForOwner(REQUEST_ID, USER_ID);

        assertThat(result).isSameAs(cr);
    }

    @Test
    void findByIdForOwner_notFoundOrNotOwned_throwsCareRequestNotFound() {
        // Anfrage existiert nicht ODER gehört nicht dem User — beides gleich behandelt
        when(ownerService.findByUserId(USER_ID)).thenReturn(owner);
        when(careRequestRepository.findByIdAndOwnerId(REQUEST_ID, OWNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> careRequestService.findByIdForOwner(REQUEST_ID, USER_ID))
                .isInstanceOf(CareRequestNotFoundException.class);
    }
}