package dhbw.heilbronn.pawsitters.repository;

import dhbw.heilbronn.pawsitters.domain.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    // Offer eines Hosts. Für die selbst erstellten Angebote
    List<Offer> findByHostId(Long hostId);

    // Offer für eine bestimmte CareRequest. Für die Owner seitige View
    List<Offer> findByCareRequestId(Long careRequestId);

    // Doppelschutz: ein Host darf nur ein Offer pro CareRequest senden
    boolean existsByHostIdAndCareRequestId(Long hostId, Long careRequestId);
}
