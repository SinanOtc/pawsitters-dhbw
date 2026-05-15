package dhbw.heilbronn.pawsitters.security;

import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserResolverTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserResolver resolver;

    @Test
    void userId_userExists_returnsId() {
        UserDetails principal = mock(UserDetails.class);

        when(principal.getUsername()).thenReturn("test@test.de");

        User user = new User("test@test.de", "hash",
                UserRole.OWNER);
        user.setId(42L);
        when(userRepository.findByEmail("test@test.de")).thenReturn(Optional.of(user));

        Long id = resolver.userId(principal);

        assertThat(id).isEqualTo(42L);
    }

    @Test
    void userId_userNotFound_throwsIllegalStateException()
    {
        // Sollte im Normalbetrieb nicht passieren — Auth läuft gegen dieselbe DB.
        // Aber wenn ein User zwischen Login und Request gelöscht würde, soll
        // klar erkennbar fehlschlagen statt mit NullPointerException zu crashen.
            UserDetails principal = mock(UserDetails.class);

        when(principal.getUsername()).thenReturn("ghost@test.de");
        when(userRepository.findByEmail("ghost@test.de")).
                thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                resolver.userId(principal))

                .isInstanceOf(IllegalStateException.class);
    }
}