package dhbw.heilbronn.pawsitters.repository;

import dhbw.heilbronn.pawsitters.domain.CareRequest;
import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CareRequestRepository extends JpaRepository<CareRequest, Long> {

    // Alle Anfragen eines Owners für die Owner Übersicht
    List<CareRequest> findByOwnerId(Long ownerId);

    // Einzelne Anfrage zugeschnitten auf den Owner, Schutz vor URL-Manipulation.
    // (Gleich wie PetRepository.findByIdAndOwnerId)
    Optional<CareRequest> findByIdAndOwnerId(Long id, Long ownerId);

    /**
     * Findet alle OPEN CareRequests, die zu einem Host passen.
     * Vier Filter:
     *    1. Status OPEN
     *    2. Pet-Species ist eine vom Host akzeptierte
     *    3. CareRequest-Zeitraum liegt vollständig in der Host-Verfügbarkeit
     *    4. Host hat noch kein Offer für diese CareRequest abgegeben
     * Status als fully-qualified Enum-Path im JPQL → keine zusätzlichen Importsachen benötigt.
     */
    @Query("""
            SELECT cr FROM CareRequest cr
            JOIN cr.pet p
            WHERE cr.status =   dhbw.heilbronn.pawsitters.domain.RequestStatus.OPEN
            AND p.species IN :acceptedSpecies
            AND cr.startDate >= :availableFrom
            AND cr.endDate <= :availableUntil
            AND NOT EXISTS (
                SELECT 1 FROM Offer o
                WHERE o.careRequest = cr AND o.host.id = :hostId
            )
    """)
        List<CareRequest> findMatchingForHost(
                @Param("acceptedSpecies") Set<PetSpecies> acceptedSpecies,
                @Param("availableFrom") LocalDate availableFrom,
                @Param("availableUntil") LocalDate availableUntil,
                @Param("hostId") Long hostId
                );

    /**
     * Findet alle nicht geschlossenen CareRequests, deren endDate vor dem übergebenen Datum liegt.
     * Genutzt von Expiry-Job, um sie auf CLOSED zu setzen.
     */
    List<CareRequest> findByStatusNotAndEndDateBefore(dhbw.heilbronn.pawsitters.domain.RequestStatus status, LocalDate cutoff);
}
