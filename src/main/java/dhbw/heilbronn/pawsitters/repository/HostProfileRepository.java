package dhbw.heilbronn.pawsitters.repository;

import dhbw.heilbronn.pawsitters.domain.HostProfile;
import dhbw.heilbronn.pawsitters.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HostProfileRepository extends JpaRepository<HostProfile, Long> {

    // Profil zum eingeloggten User, Spring weiß nur die Email.
    // Der Service braucht aber das Profil (über ID).
    // Gleicher Aufbau wie bei OwnerProfileRepository.findByUserId
    Optional<HostProfile> findByUserId(Long userId);

    Long user(User user);
}

