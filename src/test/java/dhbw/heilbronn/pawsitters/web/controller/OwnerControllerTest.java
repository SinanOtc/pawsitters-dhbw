package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.config.SecurityConfig;
import dhbw.heilbronn.pawsitters.domain.OwnerProfile;
import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import org.springframework.context.annotation.Import;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import dhbw.heilbronn.pawsitters.service.OwnerService;
import dhbw.heilbronn.pawsitters.service.exception.EmailAlreadyTakenException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest bootet nur die Web Schicht (Controller + Validation + MockMvc)
// statt die ganze App. Schneller als @SpringBootTest und reicht für Routing
// und Validation Tests
@WebMvcTest(OwnerController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("dev")
class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // OwnerService und UserRepository werden gemockt, der Controller wird
    // damit komplett von der Datenbank entkoppelt
    @MockitoBean
    private OwnerService ownerService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void getRegister_returnsRegisterView() throws Exception {
        mockMvc.perform(get("/owner/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/register"))
                .andExpect(model().attributeExists("registerForm"));
    }

    @Test
    void postRegister_validForm_redirectsToLoginWithFlag() throws Exception {
        mockMvc.perform(post("/owner/register")
                        .with(csrf())
                        .param("email", "neu@test.de")
                        .param("password", "geheim123")
                        .param("firstName", "Max")
                        .param("lastName", "Mustermann")
                        .param("street", "Musterstraße")
                        .param("streetNumber", "1")
                        .param("postalCode", "74072")
                        .param("city", "Heilbronn"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(ownerService).register(any());
    }

    @Test
    void postRegister_invalidEmail_returnsRegisterWithErrors() throws Exception {
        mockMvc.perform(post("/owner/register")
                        .with(csrf())
                        .param("email", "keineEmail")
                        .param("password", "geheim123")
                        .param("firstName", "Max")
                        .param("lastName", "Mustermann")
                        .param("street", "Musterstraße")
                        .param("streetNumber", "1")
                        .param("postalCode", "74072")
                        .param("city", "Heilbronn"))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "email"));

        // Bei Validation Fehler darf der Service NICHT aufgerufen werden
        verify(ownerService, never()).register(any());
    }

    @Test
    void postRegister_emailAlreadyTaken_showsFieldError() throws Exception {
        when(ownerService.register(any()))
                .thenThrow(new EmailAlreadyTakenException("doppelt@test.de"));

        mockMvc.perform(post("/owner/register")
                        .with(csrf())
                        .param("email", "doppelt@test.de")
                        .param("password", "geheim123")
                        .param("firstName", "Max")
                        .param("lastName", "Mustermann")
                        .param("street", "Musterstraße")
                        .param("streetNumber", "1")
                        .param("postalCode", "74072")
                        .param("city", "Heilbronn"))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "email"));
    }

    @Test
    void postRegister_invalidPostalCode_returnsRegisterWithErrors() throws Exception {
        mockMvc.perform(post("/owner/register")
                        .with(csrf())
                        .param("email", "neu@test.de")
                        .param("password", "geheim123")
                        .param("firstName", "Max")
                        .param("lastName", "Mustermann")
                        .param("street", "Musterstraße")
                        .param("streetNumber", "1")
                        .param("postalCode", "7407")
                        .param("city", "Heilbronn"))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "postalCode"));

        verify(ownerService, never()).register(any());
    }

    @Test
    void getProfile_existingOwner_returnsProfileViewWithAddressParts() throws Exception {
        User user = ownerUser();
        OwnerProfile profile = new OwnerProfile(user, "Max", "Mustermann", "Musterstraße 1, 74072 Heilbronn");
        when(userRepository.findByEmail("owner@test.de")).thenReturn(Optional.of(user));
        when(ownerService.findByUserId(1L)).thenReturn(profile);

        mockMvc.perform(get("/owner/profile")
                        .with(user("owner@test.de").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/profile"))
                .andExpect(model().attribute("profile", profile))
                .andExpect(model().attributeExists("addressParts"));
    }

    @Test
    void getEdit_existingOwner_returnsEditViewWithPrefilledForm() throws Exception {
        User user = ownerUser();
        OwnerProfile profile = new OwnerProfile(user, "Max", "Mustermann", "Musterstraße 1, 74072 Heilbronn");
        when(userRepository.findByEmail("owner@test.de")).thenReturn(Optional.of(user));
        when(ownerService.findByUserId(1L)).thenReturn(profile);

        mockMvc.perform(get("/owner/profile/edit")
                        .with(user("owner@test.de").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/profile-edit"))
                .andExpect(model().attributeExists("updateForm"));
    }

    @Test
    void postEdit_validForm_redirectsToProfile() throws Exception {
        User user = ownerUser();
        when(userRepository.findByEmail("owner@test.de")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/owner/profile/edit")
                        .with(csrf())
                        .with(user("owner@test.de").roles("OWNER"))
                        .param("firstName", "Max")
                        .param("lastName", "Mustermann")
                        .param("street", "Musterstraße")
                        .param("streetNumber", "2")
                        .param("postalCode", "74072")
                        .param("city", "Heilbronn"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owner/profile"));

        verify(ownerService).update(eq(1L), any());
    }

    @Test
    void postEdit_invalidPostalCode_returnsEditViewWithErrors() throws Exception {
        mockMvc.perform(post("/owner/profile/edit")
                        .with(csrf())
                        .with(user("owner@test.de").roles("OWNER"))
                        .param("firstName", "Max")
                        .param("lastName", "Mustermann")
                        .param("street", "Musterstraße")
                        .param("streetNumber", "2")
                        .param("postalCode", "abc")
                        .param("city", "Heilbronn"))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/profile-edit"))
                .andExpect(model().attributeHasFieldErrors("updateForm", "postalCode"));

        verify(ownerService, never()).update(any(), any());
    }

    private User ownerUser() {
        User user = new User("owner@test.de", "hash", UserRole.OWNER);
        user.setId(1L);
        return user;
    }
}
