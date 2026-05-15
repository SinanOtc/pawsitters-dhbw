package dhbw.heilbronn.pawsitters.config;

import dhbw.heilbronn.pawsitters.domain.CareRequest;
import dhbw.heilbronn.pawsitters.domain.HostProfile;
import dhbw.heilbronn.pawsitters.domain.OwnerProfile;
import dhbw.heilbronn.pawsitters.domain.Pet;
import dhbw.heilbronn.pawsitters.domain.PetGender;
import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import dhbw.heilbronn.pawsitters.service.CareRequestService;
import dhbw.heilbronn.pawsitters.service.HostService;
import dhbw.heilbronn.pawsitters.service.OfferService;
import dhbw.heilbronn.pawsitters.service.OwnerService;
import dhbw.heilbronn.pawsitters.service.PetService;
import dhbw.heilbronn.pawsitters.web.form.CareRequestForm;
import dhbw.heilbronn.pawsitters.web.form.OfferForm;
import dhbw.heilbronn.pawsitters.web.form.PetForm;
import dhbw.heilbronn.pawsitters.web.form.RegisterHostForm;
import dhbw.heilbronn.pawsitters.web.form.RegisterOwnerForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;

/**
 * Seed-Daten für Dev und Demo.
 * Legt beim App-Start eine komplette End-to-End-Beispielwelt an:
 *   Owner → Pet → CareRequest und Host → Offer auf diese Request.
 *
 * Damit ist die App nach `mvn spring-boot:run` sofort vorführbar — kein manuelles
 * Klicken durch Registrierung, Pet-Anlegen etc. nötig.
 *
 * Nur in Profile "dev" aktiv → in Prod existieren keine Demo-Daten.
 * Idempotent: Re-Run überspringt bei vorhandenem Demo-Owner.
 *
 * Demo-Logins (siehe README):
 *   demo-owner@test.de / demo12345
 *   demo-host@test.de  / demo12345
 */
@Configuration
@Profile("dev")
public class DevUsersConfig {

    private static final Logger log = LoggerFactory.getLogger(DevUsersConfig.class);

    private static final String DEMO_OWNER_EMAIL = "demo-owner@test.de";
    private static final String DEMO_HOST_EMAIL = "demo-host@test.de";
    private static final String DEMO_PASSWORD = "demo12345";

    @Bean
    public CommandLineRunner seedDevData(UserRepository userRepository,
                                         OwnerService ownerService,
                                         HostService hostService,
                                         PetService petService,
                                         CareRequestService careRequestService,
                                         OfferService offerService) {
        return args -> {
            // Idempotenz-Guard: Re-Run überspringen wenn Demo-Daten schon existieren
            if (userRepository.existsByEmail(DEMO_OWNER_EMAIL)) {
                log.info("Dev-Seed übersprungen — Demo-Daten existieren bereits.");
                return;
            }

            // === Owner anlegen ===
            OwnerProfile owner = ownerService.register(new RegisterOwnerForm(
                    DEMO_OWNER_EMAIL,
                    DEMO_PASSWORD,
                    "Max",
                    "Mustermann",
                    "Musterstraße 1, 74072 Heilbronn"
            ));
            Long ownerUserId = owner.getUser().getId();

            // === Host anlegen (mit vollem Profil) ===
            HostProfile host = hostService.register(new RegisterHostForm(
                    DEMO_HOST_EMAIL,
                    DEMO_PASSWORD,
                    "Erika",
                    "Schmidt",
                    "Hoststraße 5, 74072 Heilbronn",
                    EnumSet.of(PetSpecies.DOG, PetSpecies.CAT),
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(90),
                    new BigDecimal("50.00")
            ));
            Long hostUserId = host.getUser().getId();

            // === Pet für den Owner ===
            Pet pet = petService.register(ownerUserId, new PetForm(
                    "Bello",
                    PetSpecies.DOG,
                    "Labrador",
                    PetGender.MALE,
                    2020,
                    true,
                    "DE1234567890",
                    true,
                    true,
                    "Verträgt sich gut mit anderen Hunden, liebt lange Spaziergänge."
            ));

            // === Betreuungsanfrage für das Pet ===
            CareRequest careRequest = careRequestService.register(ownerUserId, new CareRequestForm(
                    pet.getId(),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(14)
            ));

            // === Offer vom Host auf die Anfrage ===
            offerService.createOffer(hostUserId, careRequest.getId(), new OfferForm(
                    new BigDecimal("55.00")
            ));

            log.info("Dev-Seed fertig: Owner + Host + Pet + CareRequest + Offer angelegt.");
            log.info("Demo-Login Owner: {} / {}", DEMO_OWNER_EMAIL, DEMO_PASSWORD);
            log.info("Demo-Login Host:  {} / {}", DEMO_HOST_EMAIL, DEMO_PASSWORD);
        };
    }
}
