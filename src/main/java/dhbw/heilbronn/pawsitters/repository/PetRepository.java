package dhbw.heilbronn.pawsitters.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import dhbw.heilbronn.pawsitters.domain.Pet;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository für Pet Entitäten
 * Zugriff immer kombiniert mit ownerId prüfen, damit kein Owner fremde
 * Pets bearbeiten oder löschen kann
 */

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    // Liste aller Pets eines PetOwners (für Übersicht im Dashboard)
    List<Pet> findByOwnerId(Long ownerId);

    // Einzelnes Pet, aber nur wenn es wirklich genau dem Owner gehört.
    // Verhindert URL Manipulation (Bsp.: /pet/123/edit)
    Optional<Pet> findByIdAndOwnerId(Long id, Long ownerId);
}
