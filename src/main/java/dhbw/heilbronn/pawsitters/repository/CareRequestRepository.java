package dhbw.heilbronn.pawsitters.repository;

import dhbw.heilbronn.pawsitters.domain.CareRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CareRequestRepository extends JpaRepository<CareRequest, Long> {

    // Alle Anfragen eines Owners für die Owner Übersicht
    List<CareRequest> findByOwnerId(Long ownerId);

    // Einzelne Anfrage zugeschnitten auf den Owner, Schutz vor URL Manipulation.
    // (Gleich wie PetRepository.findByIdAndOwnerId)
    Optional<CareRequest> findByIdAndOwnerId(Long id, Long ownerId);
}
