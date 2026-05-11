package dhbw.heilbronn.pawsitters.service;

import dhbw.heilbronn.pawsitters.domain.CareRequest;
import dhbw.heilbronn.pawsitters.domain.OwnerProfile;
import dhbw.heilbronn.pawsitters.domain.Pet;
import dhbw.heilbronn.pawsitters.repository.CareRequestRepository;
import dhbw.heilbronn.pawsitters.service.exception.CareRequestNotFoundException;
import dhbw.heilbronn.pawsitters.web.form.CareRequestForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service-Klasse zur Verwaltung von Betreuungsanfragen, die von Tierbesitzern gestellt werden.
 * Kapselt die Geschäftslogik für das Anlegen, Abrufen und Validieren von Betreuungsanfragen.
 * Arbeitet mit Repositories und weiteren Services zusammen, um einen korrekten Ablauf
 * und die Datenkonsistenz sicherzustellen.
 */
@Service
public class CareRequestService {

    private final CareRequestRepository careRequestRepository;
    private final OwnerService ownerService;
    private final PetService petService;

    /**
     * Erstellt eine neue Instanz des {@code CareRequestService}.
     *
     * @param careRequestRepository Repository für den Zugriff auf Betreuungsanfragen
     * @param ownerService          Service zum Auflösen und Validieren von Owner-Profilen
     * @param petService            Service zum Auflösen und Validieren von Pets eines Owners
     */
    public CareRequestService(CareRequestRepository careRequestRepository, OwnerService ownerService, PetService petService) {
        this.careRequestRepository = careRequestRepository;
        this.ownerService = ownerService;
        this.petService = petService;
    }

    /**
     * Legt eine Anfrage für den angegebenen Owner an.
     * Pet wird über petService geprüft → fremde Pets erzeugen PetNotFoundException.
     *
     * @param ownerUserId die User-ID des Owners, der die Anfrage stellt
     * @param form        das Formular mit den Daten der Betreuungsanfrage (Pet, Start- und Enddatum)
     * @return die persistierte {@link CareRequest}
     */
    @Transactional
    public CareRequest register(Long ownerUserId, CareRequestForm form) {
        OwnerProfile owner = ownerService.findByUserId(ownerUserId);
        Pet pet = petService.findByIdForOwner(form.petId(), ownerUserId);

        CareRequest cr = new CareRequest(owner, pet, form.startDate(), form.endDate());
        return careRequestRepository.save(cr);
    }

    /**
     * Alle Anfragen eines Owners, für die Ownerübersicht.
     * readOnly = true --> keine Schreibrechte für eine reine Leseoperation.
     *
     * @param ownerUserId die User-ID des Owners, dessen Anfragen abgerufen werden sollen
     * @return Liste aller {@link CareRequest}, die dem Owner zugeordnet sind
     */
    @Transactional(readOnly = true)
    public List<CareRequest> findAllByOwner(Long ownerUserId) {
        OwnerProfile owner = ownerService.findByUserId(ownerUserId);
        return careRequestRepository.findByOwnerId(owner.getId());
    }

    /**
     * Einzelne Anfrage auf bestimmten Owner.
     * Wirft CareRequestNotFoundException wenn Anfrage nicht existiert oder nicht dem Owner gehört.
     *
     * @param careRequestId die ID der gesuchten Betreuungsanfrage
     * @param ownerUserId   die User-ID des Owners, dem die Anfrage zugeordnet sein muss
     * @return die gefundene {@link CareRequest}
     * @throws CareRequestNotFoundException wenn keine Anfrage mit der angegebenen ID
     *                                      für diesen Owner existiert
     */
    @Transactional(readOnly = true)
    public CareRequest findByIdForOwner(Long careRequestId, Long ownerUserId) {
        OwnerProfile owner = ownerService.findByUserId(ownerUserId);
        return careRequestRepository.findByIdAndOwnerId(careRequestId, owner.getId())
                .orElseThrow(() -> new CareRequestNotFoundException(careRequestId));
    }

}