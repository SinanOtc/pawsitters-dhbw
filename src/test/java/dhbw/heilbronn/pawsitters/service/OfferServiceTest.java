package dhbw.heilbronn.pawsitters.service;

import dhbw.heilbronn.pawsitters.domain.CareRequest;
import dhbw.heilbronn.pawsitters.domain.HostProfile;
import dhbw.heilbronn.pawsitters.domain.Offer;
import dhbw.heilbronn.pawsitters.domain.OwnerProfile;
import dhbw.heilbronn.pawsitters.domain.Pet;
import dhbw.heilbronn.pawsitters.domain.PetGender;
import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import dhbw.heilbronn.pawsitters.repository.CareRequestRepository;
import dhbw.heilbronn.pawsitters.repository.OfferRepository;
import dhbw.heilbronn.pawsitters.service.exception.CareRequestNotFoundException;
import dhbw.heilbronn.pawsitters.service.exception.OfferNotEligibleException;
import dhbw.heilbronn.pawsitters.web.form.OfferForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    private static final Long HOST_USER_ID = 1L;
    private static final Long HOST_ID = 10L;
    private static final Long OWNER_USER_ID = 2L;
    private static final Long OWNER_ID = 20L;
    private static final Long CARE_REQUEST_ID = 42L;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private CareRequestRepository careRequestRepository;

    @Mock
    private HostService hostService;

    @Mock
    private OwnerService ownerService;

    @InjectMocks
    private OfferService offerService;

    private HostProfile host;
    private OwnerProfile owner;
    private CareRequest careRequest;
    private OfferForm validForm;

    @BeforeEach
    void setUp() {
        User hostUser = new User("host@t.de", "hash",
                UserRole.HOST);
        hostUser.setId(HOST_USER_ID);
        host = new HostProfile(
                hostUser, "Erika", "Mustermann",
                "Hoststraße 5",
                EnumSet.of(PetSpecies.DOG),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                new BigDecimal("50.00")
        );
        host.setId(HOST_ID);

        User ownerUser = new User("owner@t.de", "hash",
                UserRole.OWNER);
        ownerUser.setId(OWNER_USER_ID);
        owner = new OwnerProfile(ownerUser, "Max",
                "Muster", "Adresse 1");
        owner.setId(OWNER_ID);

        Pet pet = new Pet(owner, "Bello", PetSpecies.DOG,
                PetGender.MALE);
        careRequest = new CareRequest(owner, pet,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(15));
        careRequest.setId(CARE_REQUEST_ID);

        validForm = new OfferForm(new BigDecimal("60.00"));
    }

    // === findMatchingRequests ===

    @Test
    void findMatchingRequests_returnsResultsFromRepo() {
        when(hostService.findByUserId(HOST_USER_ID)).thenReturn(host);
        when(careRequestRepository.findMatchingForHost(
                host.getAcceptedSpecies(),
                host.getAvailableFrom(),
                host.getAvailableUntil(),
                host.getId()
        )).thenReturn(List.of(careRequest));

        List<CareRequest> result =
                offerService.findMatchingRequests(HOST_USER_ID);

        assertThat(result).containsExactly(careRequest);
    }

    // === findOffersByHost ===

    @Test
    void findOffersByHost_returnsHostsOffers() {
        Offer offer = new Offer(host, careRequest, new
                BigDecimal("60.00"));
        when(hostService.findByUserId(HOST_USER_ID)).thenReturn(host);
        when(offerRepository.findByHostId(HOST_ID)).thenReturn(List.of(offer));

        List<Offer> result =
                offerService.findOffersByHost(HOST_USER_ID);

        assertThat(result).containsExactly(offer);
    }

    // === findOffersByCareRequest ===

    @Test
    void
    findOffersByCareRequest_ownedByUser_returnsOffers() {
        Offer offer = new Offer(host, careRequest, new
                BigDecimal("60.00"));
        when(ownerService.findByUserId(OWNER_USER_ID)).thenReturn(owner);
        when(careRequestRepository.findByIdAndOwnerId(CARE_REQUEST_ID, OWNER_ID))
                .thenReturn(Optional.of(careRequest));

        when(offerRepository.findByCareRequestId(CARE_REQUEST_ID))
                .thenReturn(List.of(offer));

        List<Offer> result =
                offerService.findOffersByCareRequest(OWNER_USER_ID,
                        CARE_REQUEST_ID);

        assertThat(result).containsExactly(offer);
    }

    @Test
    void findOffersByCareRequest_notOwnedByUser_throwsCareRequestNotFound() {
        // Security: Owner kann nur Offers für EIGENECareRequests sehen
        when(ownerService.findByUserId(OWNER_USER_ID)).thenReturn(owner);
        when(careRequestRepository.findByIdAndOwnerId(CARE_REQUEST_ID, OWNER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                offerService.findOffersByCareRequest(OWNER_USER_ID,
                        CARE_REQUEST_ID))

                .isInstanceOf(CareRequestNotFoundException.class);

        verify(offerRepository,
                never()).findByCareRequestId(any());
    }

    // === createOffer ===

    @Test
    void
    createOffer_matchingRequest_savesOfferWithPendingStatus() {
        when(hostService.findByUserId(HOST_USER_ID)).thenReturn(host);

        when(careRequestRepository.findById(CARE_REQUEST_ID))
                .thenReturn(Optional.of(careRequest));
        when(careRequestRepository.findMatchingForHost(
                host.getAcceptedSpecies(),
                host.getAvailableFrom(),
                host.getAvailableUntil(),
                host.getId()
        )).thenReturn(List.of(careRequest));

        when(offerRepository.save(any(Offer.class))).thenAnswer(inv
                -> inv.getArgument(0));

        Offer result =
                offerService.createOffer(HOST_USER_ID, CARE_REQUEST_ID,
                        validForm);

        // Offer wird gespeichert mit dem korrekten Host, CareRequest und Preis
        assertThat(result.getHost()).isSameAs(host);

        assertThat(result.getCareRequest()).isSameAs(careRequest);
        assertThat(result.getWeeklyPrice()).isEqualByComparingTo("60.00");
    }

    @Test
    void createOffer_careRequestNotFound_throwsException()
    {
        when(hostService.findByUserId(HOST_USER_ID)).thenReturn(host);

        when(careRequestRepository.findById(CARE_REQUEST_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                offerService.createOffer(HOST_USER_ID, CARE_REQUEST_ID,
                        validForm))

                .isInstanceOf(CareRequestNotFoundException.class);

        verify(offerRepository, never()).save(any());
    }

    @Test
    void createOffer_notMatching_throwsOfferNotEligible() {
        // URL-Manipulation: CareRequest existiert, ist aber NICHT in der Matching-Liste
        // (z.B. Species mismatch, schon Offer abgegeben, Status nicht OPEN)
        when(hostService.findByUserId(HOST_USER_ID)).thenReturn(host);

        when(careRequestRepository.findById(CARE_REQUEST_ID))
                .thenReturn(Optional.of(careRequest));
        when(careRequestRepository.findMatchingForHost(
                host.getAcceptedSpecies(),
                host.getAvailableFrom(),
                host.getAvailableUntil(),
                host.getId()
        )).thenReturn(List.of());  // leer → careRequest nicht drin

        assertThatThrownBy(() ->
                offerService.createOffer(HOST_USER_ID, CARE_REQUEST_ID,
                        validForm))

                .isInstanceOf(OfferNotEligibleException.class);

        verify(offerRepository, never()).save(any());
    }
}