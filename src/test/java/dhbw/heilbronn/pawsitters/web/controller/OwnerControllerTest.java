package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import dhbw.heilbronn.pawsitters.security.CurrentUserResolver;
import dhbw.heilbronn.pawsitters.service.OwnerService;
import dhbw.heilbronn.pawsitters.service.exception.EmailAlreadyTakenException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    // OwnerService und CurrentUserResolver werden gemockt, der Controller wird
    // damit komplett von der Datenbank entkoppelt.
    // CurrentUserResolver wird nur fürs Autowiring gebraucht — die Register-Tests
    // benutzen ihn nicht direkt.
    @MockitoBean
    private OwnerService ownerService;

    @MockitoBean
    private CurrentUserResolver currentUserResolver;

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
                        .param("address", "Musterstraße 1"))
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
                        .param("address", "Musterstraße 1"))
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
                        .param("address", "Musterstraße 1"))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "email"));
    }
}