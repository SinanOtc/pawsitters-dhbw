package dhbw.heilbronn.pawsitters.service;

import dhbw.heilbronn.pawsitters.domain.OwnerProfile;
import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import dhbw.heilbronn.pawsitters.repository.OwnerProfileRepository;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import dhbw.heilbronn.pawsitters.service.exception.EmailAlreadyTakenException;
import dhbw.heilbronn.pawsitters.web.form.RegisterOwnerForm;
import dhbw.heilbronn.pawsitters.web.form.UpdateOwnerForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI Generated Test
 */
@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OwnerProfileRepository ownerProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OwnerService ownerService;

    private RegisterOwnerForm validForm;

    @BeforeEach
    void setUp() {
        validForm = new RegisterOwnerForm(
                "owner@test.de",
                "geheim123",
                "Max",
                "Mustermann",
                "Musterstraße",
                "1",
                "74072",
                "Heilbronn"
        );
    }

    @Test
    void register_validForm_savesUserAndProfile() {
        when(userRepository.existsByEmail("owner@test.de")).thenReturn(false);
        when(passwordEncoder.encode("geheim123")).thenReturn("hashed");
        // thenAnswer "gib zurück was reinkam" - simuliert das save() Verhalten
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ownerProfileRepository.save(any(OwnerProfile.class))).thenAnswer(inv ->
                inv.getArgument(0));

        OwnerProfile result = ownerService.register(validForm);

        assertThat(result.getFirstName()).isEqualTo("Max");
        assertThat(result.getUser().getEmail()).isEqualTo("owner@test.de");
        verify(userRepository).save(any(User.class));
        verify(ownerProfileRepository).save(any(OwnerProfile.class));
    }

    @Test
    void register_passwordIsHashed_neverStoresPlaintext() {
        // Wichtigster Security-Test: Klartext-Passwort darf NIE in der
        // gespeicherten Entity landen. Wir prüfen mit argThat() das User-Argument
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("geheim123")).thenReturn("$2a$10$hashedValue");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ownerProfileRepository.save(any(OwnerProfile.class))).thenAnswer(inv ->
                inv.getArgument(0));

        ownerService.register(validForm);

        verify(passwordEncoder).encode("geheim123");
        verify(userRepository).save(argThat(u ->
                u.getPasswordHashed().equals("$2a$10$hashedValue")
                        && !u.getPasswordHashed().equals("geheim123")
        ));
    }

    @Test
    void register_userRoleIsOwner() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ownerProfileRepository.save(any(OwnerProfile.class))).thenAnswer(inv ->
                inv.getArgument(0));

        ownerService.register(validForm);

        // Schutzregel: Über OwnerService darf NIE jemand mit Rolle HOST registriert werden
        verify(userRepository).save(argThat(u -> u.getRole() == UserRole.OWNER));
    }

    @Test
    void register_emailAlreadyTaken_throwsException() {
        when(userRepository.existsByEmail("owner@test.de")).thenReturn(true);

        assertThatThrownBy(() -> ownerService.register(validForm))
                .isInstanceOf(EmailAlreadyTakenException.class)
                .hasMessageContaining("owner@test.de");

        // Bei Duplikat darf NICHTS gespeichert werden - sonst hätten wir
        // halbe User in der DB
        verify(userRepository, never()).save(any());
        verify(ownerProfileRepository, never()).save(any());
    }

    @Test
    void findByUserId_existing_returnsProfile() {
        OwnerProfile expected = new OwnerProfile(
                new User("o@t.de", "h", UserRole.OWNER), "F", "L", "A");
        when(ownerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(expected));

        OwnerProfile result = ownerService.findByUserId(1L);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void findByUserId_notFound_throwsIllegalStateException() {
        when(ownerProfileRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.findByUserId(99L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void update_existingProfile_updatesAllFields() {
        User user = new User("o@t.de", "hash", UserRole.OWNER);
        OwnerProfile profile = new OwnerProfile(user, "Alt-Vor", "Alt-Nach", "Alt-Str. 1");
        when(ownerProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        UpdateOwnerForm form = new UpdateOwnerForm("Neu-Vor", "Neu-Nach", "Neu-Str.", "2", "74072", "Heilbronn");
        OwnerProfile result = ownerService.update(1L, form);

        assertThat(result.getFirstName()).isEqualTo("Neu-Vor");
        assertThat(result.getLastName()).isEqualTo("Neu-Nach");
        assertThat(result.getAddress()).isEqualTo("Neu-Str. 2, 74072 Heilbronn");
    }
}
