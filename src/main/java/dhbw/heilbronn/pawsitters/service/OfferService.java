package dhbw.heilbronn.pawsitters.service;

import dhbw.heilbronn.pawsitters.domain.CareRequest;
import dhbw.heilbronn.pawsitters.domain.HostProfile;
import dhbw.heilbronn.pawsitters.domain.Offer;
import dhbw.heilbronn.pawsitters.domain.OwnerProfile;
import dhbw.heilbronn.pawsitters.repository.CareRequestRepository;
import dhbw.heilbronn.pawsitters.repository.OfferRepository;
import dhbw.heilbronn.pawsitters.service.exception.CareRequestNotFoundException;
import dhbw.heilbronn.pawsitters.service.exception.OfferNotEligibleException;
import dhbw.heilbronn.pawsitters.web.form.OfferForm;
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
        Offer offer = new Offer(host, cr, form.weeklyPrice());
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


}
