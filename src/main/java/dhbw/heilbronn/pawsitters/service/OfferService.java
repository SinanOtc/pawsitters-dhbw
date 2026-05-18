package dhbw.heilbronn.pawsitters.service;

import dhbw.heilbronn.pawsitters.domain.*;
import dhbw.heilbronn.pawsitters.repository.CareRequestRepository;
import dhbw.heilbronn.pawsitters.repository.OfferRepository;
import dhbw.heilbronn.pawsitters.service.exception.CareRequestNotFoundException;
import dhbw.heilbronn.pawsitters.service.exception.OfferNotEligibleException;
import dhbw.heilbronn.pawsitters.service.exception.OfferNotFoundException;
import dhbw.heilbronn.pawsitters.service.exception.OfferNotPendingException;
import dhbw.heilbronn.pawsitters.dto.OfferForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final CareRequestRepository careRequestRepository;
    private final HostService hostService;
    private final OwnerService ownerService;

    public OfferService(
            OfferRepository offerRepository,
            CareRequestRepository careRequestRepository,
            HostService hostService,
            OwnerService ownerService
    ) {
        this.offerRepository = offerRepository;
        this.careRequestRepository = careRequestRepository;
        this.hostService = hostService;
        this.ownerService = ownerService;
    }

    // === Host-Seitig ===

    /**
     * Alle OPEN Requests, die zum Host passen für die Browseübersicht.
     * Filter:
     *      - Species
     *      - Verfügbarkeit
     *      - Status OPEN
     *      - kein eigenes Offer schon vorhanden
     */
    @Transactional(readOnly = true)
    public List<CareRequest> findMatchingRequests(Long hostUserId) {
        HostProfile host = hostService.findByUserId(hostUserId);
        return careRequestRepository.findMatchingForHost(
                host.getAcceptedSpecies(),
                host.getAvailableFrom(),
                host.getAvailableUntil(),
                host.getId()
        );
    }

    /**
     * Eigene gesendete Offers eines Hosts, für die "meine Angebote"-Übersicht.
     */
    @Transactional(readOnly = true)
    public List<Offer> findOffersByHost(Long hostUserId) {
        HostProfile host = hostService.findByUserId(hostUserId);
        return offerRepository.findByHostId(host.getId());
    }

    /**
     * Legt ein neues Offer an. Schutz CareRequest muss aktuell für diesen Host
     * passen, sonst URL Manipulation. Reverfikiation durch findMatchingForHost -> eine Source of Truth.
     */
    @Transactional
    public Offer createOffer(Long hostUserId, Long careRequestId, OfferForm form) {
        HostProfile host = hostService.findByUserId(hostUserId);

        CareRequest cr = careRequestRepository.findById(careRequestId)
                .orElseThrow(() -> new
                        CareRequestNotFoundException(careRequestId));

        // Reverify Matching -> keine Offer Erstellung für nicht passende oder schon bediente Anfragen, selbst bei URL-Manipulation
        List<CareRequest> matching = careRequestRepository.findMatchingForHost(
                host.getAcceptedSpecies(),
                host.getAvailableFrom(),
                host.getAvailableUntil(),
                host.getId()
        );
        if(!matching.contains(cr)){
            throw new OfferNotEligibleException(careRequestId);
        }
        Offer offer = new Offer(host, cr, form.weeklyPrice(), form.message());
        return offerRepository.save(offer);
    }

    // === Owner-Seitig ===

    /**
     * Alle Offers für eine CareRequest des angegebenen Owners.
     * Ownerscoping über CareRequestRepository.findByIdAndOwnerId schützt vor URL Manipulation
     * (Owner kann nur Offers für eigene CareRequests sehen).
     */
    @Transactional(readOnly = true)
    public List<Offer> findOffersByCareRequest(Long ownerUserId, Long careRequestId) {
        OwnerProfile owner = ownerService.findByUserId(ownerUserId);

        careRequestRepository.findByIdAndOwnerId(careRequestId, owner.getId())
                .orElseThrow(() -> new CareRequestNotFoundException(careRequestId));

        return offerRepository.findByCareRequestId(careRequestId);
    }

    // === Offer akzeptieren ===

    /**
     * Owner nimmt ein Offer an. Atomare Operationen:
     *      1. Offer auf ACCEPTED
     *      2. Zugehörige CareRequest auf MATCHED (Issue #10 Pt.1)
     *      3. Alle anderen PENDING-Offers derselben CareRequest auf REJECTED (#9)
     * Schutzregeln:
     *      - Offer muss zur CareRequest des Owners gehören -> sonst OfferNotFoundException
     *      (gleiche Exception wie nicht-existent, damit URL-Manipulation keinen Info-Leak gibt)
     *      - Offer muss PENDING sein -> sonst OfferNotFOundException
     *      - CareRequest muss OPEN sein -> sonst OfferNotPendingException
     *      (anderes Offer wurde schon angenommen -> Race Condition Schutz)
     */
    @Transactional
    public Offer accept(Long offerId, Long ownerUserId){
        OwnerProfile owner = ownerService.findByUserId(ownerUserId);

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new
                        OfferNotFoundException(offerId));

        // Security: Offer muss zu einer CareRequest des Owners gehören.
        // Bewusst gleiche Exception wie "nicht existent", um einen Infoleak zu vermeiden
        if(!offer.getCareRequest().getOwner().getId().equals(owner.getId())){
            throw new OfferNotFoundException(offerId);
        }

        // State Check Offer: muss PENDING sein
        if(offer.getStatus() != OfferStatus.PENDING){
            throw new OfferNotPendingException(offerId);
        }

        CareRequest cr = offer.getCareRequest();

        // State-Check CareRequest: muss OPEN sein (sonst hat schon ein anderes Offer
        // ACCEPTED ausgelöst — sollte durch Offer-State eigentlich nicht passieren,
        // aber doppelte Absicherung gegen Race-Conditions)
        if(cr.getStatus() != RequestStatus.OPEN){
            throw new OfferNotPendingException(offerId);
        }

        // Alle ANDEREN PENDING Offers derselben CareRequest auf REJECTED.
        // Das aktuell anzunehmende Offer ist noch PENDING und in der Liste
        // per Filter explizit ausschließen (somst würden wir es selbst rejecten)
        List<Offer> pending = offerRepository.findByCareRequestIdAndStatus(cr.getId(), OfferStatus.PENDING);

        pending.stream()
                .filter(o -> !o.getId().equals(offerId))
                .forEach(o -> o.setStatus(OfferStatus.REJECTED));

        // Statusupdates auf dem akzeptierten Offer und der CareRequest
        offer.setStatus(OfferStatus.ACCEPTED);
        cr.setStatus(RequestStatus.MATCHED);

        return offer;
    }

    /**
     * Owner lehnt ein Offer manuell ab. Im Gegensatz zu accept:
     *      - CareRequest bleibt OPEN (andere Offers können weiter angenommen werden)
     *      - Nur das eine Offer wird REJECTED, andere PENDING bleiben unberührt Schutzregeln (analog zu accept):
     *      - Offer müssen CareRequest des Owners gehören -> sonst OfferNotFoundException
     *      - Offer muss PENDING sein -> sonst OfferNotPendingException
     */
    @Transactional
    public Offer reject(Long offerId, Long ownerUserId){

        OwnerProfile owner = ownerService.findByUserId(ownerUserId);

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new OfferNotFoundException(offerId));

        // Security: Owner-Scope. Gleiche Exception wie "nicht existent" gegen Infoleak
        if(!offer.getCareRequest().getOwner().getId().equals(owner.getId())) {
            throw new OfferNotFoundException(offerId);
        }

        // Statecheck: nur Pending Offers können rejected werden.
        // Nochmal ablehnen ergibt keinen Sinn.
        if(offer.getStatus() != OfferStatus.PENDING) {
            throw new OfferNotPendingException(offerId);
        }

        offer.setStatus(OfferStatus.REJECTED);
        return offer;
    }
}
