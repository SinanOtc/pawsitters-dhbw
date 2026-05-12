package dhbw.heilbronn.pawsitters.service;

import dhbw.heilbronn.pawsitters.domain.HostProfile;
import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import dhbw.heilbronn.pawsitters.repository.HostProfileRepository;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import dhbw.heilbronn.pawsitters.service.exception.EmailAlreadyTakenException;
import dhbw.heilbronn.pawsitters.service.exception.HostProfileNotFoundException;
import dhbw.heilbronn.pawsitters.web.form.RegisterHostForm;
import dhbw.heilbronn.pawsitters.web.form.UpdateHostForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Reine Unit-Tests für HostService — kein Spring-Context, allesgemockt.
// Gleiches Pattern wie OwnerServiceTest.
@ExtendWith(MockitoExtension.class)
class HostServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HostProfileRepository hostProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private HostService hostService;

    private RegisterHostForm validForm;

    @BeforeEach
    void setUp() {
        validForm = new RegisterHostForm(
                "host@test.de",
                "geheim123",
                "Erika",
                "Mustermann",
                "Hoststraße 5",
                EnumSet.of(PetSpecies.DOG, PetSpecies.CAT),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                new BigDecimal("50.00")
        );
    }

    // === register ===

    @Test
    void register_validForm_savesUserAndProfile() {

        when(userRepository.existsByEmail("host@test.de")).thenReturn(false);

        when(passwordEncoder.encode("geheim123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv ->
                inv.getArgument(0));
        when(hostProfileRepository.save(any(HostProfile.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        HostProfile result = hostService.register(validForm);

        assertThat(result.getFirstName()).isEqualTo("Erika");

        assertThat(result.getAcceptedSpecies()).contains(PetSpecies.DOG,
                PetSpecies.CAT);

        assertThat(result.getUser().getEmail()).isEqualTo("host@test.de");
        verify(userRepository).save(any(User.class));
        verify(hostProfileRepository).save(any(HostProfile.class));
    }

    @Test
    void register_passwordIsHashed_neverStoresPlaintext() {
        // Wichtigster Security-Test: Klartext-Passwort darf NIE inder gespeicherten Entity landen

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("geheim123")).thenReturn("$2a$10$ hashedValue");
                when(userRepository.save(any(User.class))).thenAnswer(inv ->
                        inv.getArgument(0));
        when(hostProfileRepository.save(any(HostProfile.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        hostService.register(validForm);

        verify(passwordEncoder).encode("geheim123");
        verify(userRepository).save(argThat(u ->
                u.getPasswordHashed().equals("$2a$10$ hashedValue")
                        && !u.getPasswordHashed().equals("geheim123")
        ));
    }

    @Test
    void register_userRoleIsHost() {
        // Schutzregel: Über HostService darf NIE jemand mit RolleOWNER registriert werden

        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv ->
                inv.getArgument(0));
        when(hostProfileRepository.save(any(HostProfile.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        hostService.register(validForm);

        verify(userRepository).save(argThat(u -> u.getRole() ==
                UserRole.HOST));
    }

    @Test
    void register_emailAlreadyTaken_throwsException() {

        when(userRepository.existsByEmail("host@test.de")).thenReturn(true);

        assertThatThrownBy(() -> hostService.register(validForm))
                .isInstanceOf(EmailAlreadyTakenException.class)
                .hasMessageContaining("host@test.de");

        // Bei Duplikat darf NICHTS gespeichert werden — sonst hättenwir halbe User in der DB
        verify(userRepository, never()).save(any());
        verify(hostProfileRepository, never()).save(any());
    }

    // === findByUserId ===

    @Test
    void findByUserId_existing_returnsProfile() {
        HostProfile expected = validProfile();
        when(hostProfileRepository.findByUserId(1L)).thenReturn(Optional.of(expected));

        HostProfile result = hostService.findByUserId(1L);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void findByUserId_notFound_throwsHostProfileNotFoundException() {
        when(hostProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hostService.findByUserId(99L))
                .isInstanceOf(HostProfileNotFoundException.class);
    }

    // === update ===

    @Test
    void update_existingProfile_updatesAllFields() {
        HostProfile profile = validProfile();
        when(hostProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        UpdateHostForm form = new UpdateHostForm(
                "Neu-Vor",
                "Neu-Nach",
                "Neu-Straße 9",
                EnumSet.of(PetSpecies.RABBIT),
                LocalDate.now().plusDays(2),
                LocalDate.now().plusDays(40),
                new BigDecimal("75.00")
        );

        HostProfile result = hostService.update(1L, form);

        assertThat(result.getFirstName()).isEqualTo("Neu-Vor");
        assertThat(result.getAcceptedSpecies()).containsExactly(PetSpecies.RABBIT);

        assertThat(result.getPricePerWeek()).isEqualByComparingTo("75.00");
    }

    @Test
    void update_notFound_throwsHostProfileNotFoundException() {
        when(hostProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        UpdateHostForm form = new UpdateHostForm(
                "X", "Y", "Z",
                EnumSet.of(PetSpecies.DOG),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                new BigDecimal("10.00")
        );

        assertThatThrownBy(() -> hostService.update(99L, form))
                .isInstanceOf(HostProfileNotFoundException.class);
    }

    // === Helper ===

    private HostProfile validProfile() {
        User user = new User("h@t.de", "hash", UserRole.HOST);
        return new HostProfile(
                user, "Alt-Vor", "Alt-Nach", "Alt-Str. 1",
                EnumSet.of(PetSpecies.DOG),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30),
                new BigDecimal("50.00")
        );
    }
}