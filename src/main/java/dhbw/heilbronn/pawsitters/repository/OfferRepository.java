package dhbw.heilbronn.pawsitters.repository;

import dhbw.heilbronn.pawsitters.domain.Offer;
import dhbw.heilbronn.pawsitters.domain.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    // Offer eines Hosts. Für die selbst erstellten Angebote
    List<Offer> findByHostId(Long hostId);

    // Offer für eine bestimmte CareRequest. Für die Owner seitige View
    List<Offer> findByCareRequestId(Long careRequestId);

    // Doppelschutz: ein Host darf nur ein Offer pro CareRequest senden
    boolean existsByHostIdAndCareRequestId(Long hostId, Long careRequestId);

    // Alle PENDING Offers einer CareRequest finden, um sie auf REJECTED zu setzen wenn ein anderes angenommen wird.
    // Für Issue #9
    List<Offer> findByCareRequestIdAndStatus(Long careRequestId, OfferStatus status);
}
