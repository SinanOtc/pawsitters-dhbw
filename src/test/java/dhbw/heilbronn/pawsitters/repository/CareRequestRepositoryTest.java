package dhbw.heilbronn.pawsitters.repository;

import dhbw.heilbronn.pawsitters.domain.CareRequest;
import dhbw.heilbronn.pawsitters.domain.HostProfile;
import dhbw.heilbronn.pawsitters.domain.Offer;
import dhbw.heilbronn.pawsitters.domain.OwnerProfile;
import dhbw.heilbronn.pawsitters.domain.Pet;
import dhbw.heilbronn.pawsitters.domain.PetGender;
import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import dhbw.heilbronn.pawsitters.domain.RequestStatus;
import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest bootet nur die JPA-Schicht gegen In-Memory-H2.
// Schneller als @SpringBootTest und perfekt um dieMatching-JPQL gegen echte SQL zu prüfen.
// Jeder Test läuft in einer eigenen Transaktion die am Ende gerollbackt wird → kein State-Leak.
@DataJpaTest
class CareRequestRepositoryTest {

            @Autowired
            private TestEntityManager em;

            @Autowired
            private CareRequestRepository repo;

            // === Helper ===

            // Host mit DOG-Akzeptanz, Verfügbarkeit T+1..T+30, Preis 50€/Woche.
            // Jeder Test ruft das auf und nutzt die Werte als Such-Parameter.
            private HostProfile persistDogHost() {
                User u = new User("host-" + System.nanoTime() +
                        "@t.de", "hash", UserRole.HOST);
                em.persist(u);
                HostProfile h = new HostProfile(
                        u, "Erika", "Mustermann", "Hoststraße 5",
                        EnumSet.of(PetSpecies.DOG),
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(30),
                        new BigDecimal("50.00")
                );
                em.persist(h);
                em.flush();
                return h;
            }

            // CareRequest für ein Pet einer bestimmten Species, im gegebenen Zeitraum
            private CareRequest persistRequestFor(PetSpecies
                                                          species, LocalDate start, LocalDate end) {
                User ownerUser = new User("owner-" +
                        System.nanoTime() + "@t.de", "hash", UserRole.OWNER);
                em.persist(ownerUser);
                OwnerProfile owner = new OwnerProfile(ownerUser,
                        "Max", "Muster", "Adresse 1");
                em.persist(owner);
                Pet pet = new Pet(owner, "Bello", species,
                        PetGender.MALE);
                em.persist(pet);
                CareRequest cr = new CareRequest(owner, pet, start,
                        end);
                em.persist(cr);
                em.flush();
                return cr;
            }

            private Offer persistOffer(HostProfile host,
                                       CareRequest cr) {
                Offer o = new Offer(host, cr, new
                        BigDecimal("60.00"));
                em.persist(o);
                em.flush();
                return o;
            }

            // === Tests ===

            @Test
            void
            findMatchingForHost_speciesAndDateMatch_returnsRequest() {
                // Happy Path: alle Filter passen → CareRequest erscheint
                HostProfile host = persistDogHost();
                CareRequest cr = persistRequestFor(PetSpecies.DOG,
                        LocalDate.now().plusDays(5),
                        LocalDate.now().plusDays(15));

                List<CareRequest> result =
                        repo.findMatchingForHost(
                                host.getAcceptedSpecies(),
                                host.getAvailableFrom(),
                                host.getAvailableUntil(),
                                host.getId()
                        );

                assertThat(result).containsExactly(cr);
            }

            @Test
            void findMatchingForHost_speciesMismatch_returnsEmpty()
            {
                // Host akzeptiert nur DOG, Anfrage ist für CAT → muss ausgefiltert werden
                HostProfile host = persistDogHost();
                persistRequestFor(PetSpecies.CAT,
                        LocalDate.now().plusDays(5),
                        LocalDate.now().plusDays(15));

                List<CareRequest> result =
                        repo.findMatchingForHost(
                                host.getAcceptedSpecies(),
                                host.getAvailableFrom(),
                                host.getAvailableUntil(),
                                host.getId()
                        );

                assertThat(result).isEmpty();
            }

            @Test
            void
            findMatchingForHost_dateOutsideAvailability_returnsEmpty()
            {
                // Host verfügbar bis T+30, Anfrage geht bis T+40 → ausgefiltert
                HostProfile host = persistDogHost();
                persistRequestFor(PetSpecies.DOG,
                        LocalDate.now().plusDays(20),
                        LocalDate.now().plusDays(40));

                List<CareRequest> result =
                        repo.findMatchingForHost(
                                host.getAcceptedSpecies(),
                                host.getAvailableFrom(),
                                host.getAvailableUntil(),
                                host.getId()
                        );

                assertThat(result).isEmpty();
            }

            @Test
            void
            findMatchingForHost_hostAlreadyOffered_excludesRequest() {
                // Wenn Host schon ein Offer für diese Anfrage hat → nicht mehr anzeigen
                HostProfile host = persistDogHost();
                CareRequest cr = persistRequestFor(PetSpecies.DOG,
                        LocalDate.now().plusDays(5),
                        LocalDate.now().plusDays(15));
                persistOffer(host, cr);

                List<CareRequest> result =
                        repo.findMatchingForHost(
                                host.getAcceptedSpecies(),
                                host.getAvailableFrom(),
                                host.getAvailableUntil(),
                                host.getId()
                        );

                assertThat(result).isEmpty();
            }

            @Test
            void findMatchingForHost_closedRequest_returnsEmpty() {
                // Status-Filter: nur OPEN-Requests, MATCHED/CLOSED werden ausgefiltert
                HostProfile host = persistDogHost();
                CareRequest cr = persistRequestFor(PetSpecies.DOG,
                        LocalDate.now().plusDays(5),
                        LocalDate.now().plusDays(15));
                cr.setStatus(RequestStatus.MATCHED);
                em.flush();

                List<CareRequest> result =
                        repo.findMatchingForHost(
                                host.getAcceptedSpecies(),
                                host.getAvailableFrom(),
                                host.getAvailableUntil(),
                                host.getId()
                        );

                assertThat(result).isEmpty();
            }
        }
