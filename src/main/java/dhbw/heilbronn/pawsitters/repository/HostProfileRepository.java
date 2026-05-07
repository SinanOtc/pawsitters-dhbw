package dhbw.heilbronn.pawsitters.repository;

import dhbw.heilbronn.pawsitters.domain.HostProfile;
import dhbw.heilbronn.pawsitters.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Datenzugriff für HostProfile.
 * Spring Data JPA generiert die Implementierung zur Laufzeit.
 */
@Repository
public interface HostProfileRepository extends JpaRepository<HostProfile, Long> {

    Optional<HostProfile> findByUser(User user);

    Optional<HostProfile> findByUser_Email(String email);

    boolean existsByUser(User user);
}