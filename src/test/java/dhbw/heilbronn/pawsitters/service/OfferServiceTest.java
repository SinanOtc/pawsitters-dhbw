package dhbw.heilbronn.pawsitters.service;

import dhbw.heilbronn.pawsitters.domain.*;
import dhbw.heilbronn.pawsitters.repository.CareRequestRepository;
import dhbw.heilbronn.pawsitters.repository.OfferRepository;
import dhbw.heilbronn.pawsitters.service.exception.CareRequestNotFoundException;
import dhbw.heilbronn.pawsitters.service.exception.OfferNotEligibleException;
import dhbw.heilbronn.pawsitters.service.exception.OfferNotFoundException;
import dhbw.heilbronn.pawsitters.service.exception.OfferNotPendingException;
import dhbw.heilbronn.pawsitters.dto.OfferForm;
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

    // === accept ===

    // Neue Konstante
    private static final Long OFFER_ID = 100L;

    @Test
    void accept_pending_setsOfferAcceptedAndRequestMatched() {
        Offer offer = new Offer(host, careRequest, new
                BigDecimal("60.00"));
        offer.setId(OFFER_ID);

        when(ownerService.findByUserId(OWNER_USER_ID)).thenReturn(owner);
        when(offerRepository.findById(OFFER_ID)).thenReturn(Optional.of(offer));
        when(offerRepository.findByCareRequestIdAndStatus(CARE_REQUEST_ID, OfferStatus.PENDING))
                .thenReturn(List.of(offer));  // nur dieses Offer, keine anderen

        Offer result = offerService.accept(OFFER_ID,
                OWNER_USER_ID);

        assertThat(result.getStatus()).isEqualTo(OfferStatus.ACCEPTED);
        assertThat(careRequest.getStatus()).isEqualTo(RequestStatus.MATCHED);
    }

    @Test
    void accept_cascadeRejectsOtherPendingOffers() {
        // #9: Andere PENDING-Offers werden REJECTED beim Annehmen eines Offers
        Offer toAccept = new Offer(host, careRequest, new
                BigDecimal("60.00"));
        toAccept.setId(OFFER_ID);

        HostProfile host2 = new HostProfile(
                new User("host2@t.de", "hash", UserRole.HOST),
                "Maria", "Schmidt", "Adresse 2",
                EnumSet.of(PetSpecies.DOG),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                new BigDecimal("55.00")
        );
        Offer other1 = new Offer(host2, careRequest, new
                BigDecimal("55.00"));
        other1.setId(200L);

        HostProfile host3 = new HostProfile(
                new User("host3@t.de", "hash", UserRole.HOST),
                "Klaus", "Meier", "Adresse 3",
                EnumSet.of(PetSpecies.DOG),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                new BigDecimal("70.00")
        );
        Offer other2 = new Offer(host3, careRequest, new
                BigDecimal("70.00"));
        other2.setId(300L);

        when(ownerService.findByUserId(OWNER_USER_ID)).thenReturn(owner);
        when(offerRepository.findById(OFFER_ID)).thenReturn(Optional.of(toAccept));
        when(offerRepository.findByCareRequestIdAndStatus(CARE_REQUEST_ID, OfferStatus.PENDING))
                .thenReturn(List.of(toAccept, other1,
                        other2));

        offerService.accept(OFFER_ID, OWNER_USER_ID);

        // Akzeptiertes Offer ACCEPTED, andere REJECTED
        assertThat(toAccept.getStatus()).isEqualTo(OfferStatus
                .ACCEPTED);
        assertThat(other1.getStatus()).isEqualTo(OfferStatus.REJECTED);
        assertThat(other2.getStatus()).isEqualTo(OfferStatus.REJECTED);
        assertThat(careRequest.getStatus()).isEqualTo(RequestStatus.MATCHED);
    }

    @Test
    void accept_offerNotFound_throwsOfferNotFound() {
        when(ownerService.findByUserId(OWNER_USER_ID)).thenReturn(owner);
        when(offerRepository.findById(OFFER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.accept(OFFER_ID,
                OWNER_USER_ID))
                .isInstanceOf(OfferNotFoundException.class);
    }

    @Test
    void accept_offerNotOwnedByUser_throwsOfferNotFound() {
        // Security: Offer existiert, gehört aber zu einer fremden CareRequest.
        // Gleiche Exception wie "nicht existent" → kein Info-Leak.
                OwnerProfile otherOwner = new OwnerProfile(
                new User("other@t.de", "hash",
                        UserRole.OWNER), "Anna", "X", "Y");
        otherOwner.setId(999L);  // nicht OWNER_ID
        Pet otherPet = new Pet(otherOwner, "Miez",
                PetSpecies.CAT, PetGender.FEMALE);
        CareRequest otherCr = new CareRequest(otherOwner,
                otherPet,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(15));
        Offer offer = new Offer(host, otherCr, new
                BigDecimal("60.00"));
        offer.setId(OFFER_ID);

        when(ownerService.findByUserId(OWNER_USER_ID)).thenReturn(owner);
        when(offerRepository.findById(OFFER_ID)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> offerService.accept(OFFER_ID,
                OWNER_USER_ID))
                .isInstanceOf(OfferNotFoundException.class);
    }

    @Test
    void accept_offerAlreadyAccepted_throwsOfferNotPending() {
        Offer offer = new Offer(host, careRequest, new
                BigDecimal("60.00"));
        offer.setId(OFFER_ID);
        offer.setStatus(OfferStatus.ACCEPTED);  // schon angenommen

        when(ownerService.findByUserId(OWNER_USER_ID)).thenReturn(owner);
        when(offerRepository.findById(OFFER_ID)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> offerService.accept(OFFER_ID,
                OWNER_USER_ID))
                .isInstanceOf(OfferNotPendingException.class);
    }

    @Test
    void
    accept_careRequestAlreadyMatched_throwsOfferNotPending() {
        // Race-Condition-Schutz: Offer noch PENDING, aber CareRequest schon MATCHED
        // (anderes Offer wurde davor angenommen) → reject this attempt
        Offer offer = new Offer(host, careRequest, new
                BigDecimal("60.00"));
        offer.setId(OFFER_ID);
        careRequest.setStatus(RequestStatus.MATCHED);

        when(ownerService.findByUserId(OWNER_USER_ID)).thenReturn(owner);
        when(offerRepository.findById(OFFER_ID)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> offerService.accept(OFFER_ID,
                OWNER_USER_ID))
                .isInstanceOf(OfferNotPendingException.class);
    }

    // === reject ===

    @Test
    void reject_pending_setsOfferRejectedAndLeavesRequestOpen() {
        // Happy Path: PENDING → REJECTED, CareRequest bleibt OPEN.
        Offer offer = new Offer(host, careRequest, new BigDecimal("60.00"));
        offer.setId(OFFER_ID);

        when(ownerService.findByUserId(OWNER_USER_ID)).thenReturn(owner);
        when(offerRepository.findById(OFFER_ID)).thenReturn(Optional.of(offer));

        Offer result = offerService.reject(OFFER_ID, OWNER_USER_ID);

        assertThat(result.getStatus()).isEqualTo(OfferStatus.REJECTED);
        assertThat(careRequest.getStatus()).isEqualTo(RequestStatus.OPEN);
    }

    @Test
    void reject_doesNotAffectOtherPendingOffers() {
        // Anders als accept: ein manueller Reject lässt die anderen Offers in Ruhe.
        Offer toReject = new Offer(host, careRequest, new BigDecimal("60.00"));
        toReject.setId(OFFER_ID);

        HostProfile host2 = new HostProfile(
                new User("host2@t.de", "hash", UserRole.HOST),
                "Maria", "Schmidt", "Adresse 2",
                EnumSet.of(PetSpecies.DOG),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                new BigDecimal("55.00")
        );
        Offer otherPending = new Offer(host2, careRequest, new BigDecimal("55.00"));
        otherPending.setId(200L);

        when(ownerService.findByUserId(OWNER_USER_ID)).thenReturn(owner);
        when(offerRepository.findById(OFFER_ID)).thenReturn(Optional.of(toReject));

        offerService.reject(OFFER_ID, OWNER_USER_ID);

        assertThat(toReject.getStatus()).isEqualTo(OfferStatus.REJECTED);
        assertThat(otherPending.getStatus()).isEqualTo(OfferStatus.PENDING);
    }

    @Test
    void reject_offerNotFound_throwsOfferNotFound() {
        when(ownerService.findByUserId(OWNER_USER_ID)).thenReturn(owner);
        when(offerRepository.findById(OFFER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> offerService.reject(OFFER_ID, OWNER_USER_ID))
                .isInstanceOf(OfferNotFoundException.class);
    }

    @Test
    void reject_offerNotOwnedByUser_throwsOfferNotFound() {
        // Security: Offer gehört zu fremder CareRequest. Gleiche Exception wie
        // "nicht existent" → kein Info-Leak via URL-Manipulation.
        OwnerProfile otherOwner = new OwnerProfile(
                new User("other@t.de", "hash", UserRole.OWNER),
                "Anna", "X", "Y");
        otherOwner.setId(999L);
        Pet otherPet = new Pet(otherOwner, "Miez", PetSpecies.CAT, PetGender.FEMALE);
        CareRequest otherCr = new CareRequest(otherOwner, otherPet,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(15));
        Offer offer = new Offer(host, otherCr, new BigDecimal("60.00"));
        offer.setId(OFFER_ID);

        when(ownerService.findByUserId(OWNER_USER_ID)).thenReturn(owner);
        when(offerRepository.findById(OFFER_ID)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> offerService.reject(OFFER_ID, OWNER_USER_ID))
                .isInstanceOf(OfferNotFoundException.class);
    }

    @Test
    void reject_offerAlreadyRejected_throwsOfferNotPending() {
        // Doppelter Reject ergibt keinen Sinn — State-Guard schützt davor.
        Offer offer = new Offer(host, careRequest, new BigDecimal("60.00"));
        offer.setId(OFFER_ID);
        offer.setStatus(OfferStatus.REJECTED);

        when(ownerService.findByUserId(OWNER_USER_ID)).thenReturn(owner);
        when(offerRepository.findById(OFFER_ID)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> offerService.reject(OFFER_ID, OWNER_USER_ID))
                .isInstanceOf(OfferNotPendingException.class);
    }
}