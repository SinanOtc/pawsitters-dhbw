package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.config.SecurityConfig;
import dhbw.heilbronn.pawsitters.domain.HostProfile;
import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import dhbw.heilbronn.pawsitters.security.CurrentUserResolver;
import dhbw.heilbronn.pawsitters.service.HostService;
import dhbw.heilbronn.pawsitters.service.exception.EmailAlreadyTakenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest bootet nur die Web-Schicht für HostController.
// @WithMockUser auf Klasse → jeder Test läuft als eingeloggter Host, ausgenommen Anonymous-Tests.
@WebMvcTest(HostController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("dev")
@WithMockUser(username = "host@test.de", roles = "HOST")
        class HostControllerTest {

            private static final String EMAIL = "host@test.de";
            private static final Long USER_ID = 1L;

            @Autowired
            private MockMvc mockMvc;

            @MockitoBean
            private HostService hostService;

            @MockitoBean
            private CurrentUserResolver currentUserResolver;

            @BeforeEach
            void mockAuthenticatedUser() {
                when(currentUserResolver.userId(any(UserDetails.class))).thenReturn(USER_ID);
            }

            // === Registrierung ===

            @Test
            void getRegister_returnsRegisterView() throws Exception {
                mockMvc.perform(get("/host/register"))
                        .andExpect(status().isOk())
                        .andExpect(view().name("host/register"))
                        .andExpect(model().attributeExists("registerForm"))
                        .andExpect(model().attributeExists("allSpecies"));
            }

            @Test
            void postRegister_validForm_redirectsToLoginWithFlag() throws
                    Exception {
                mockMvc.perform(post("/host/register")
                                .with(csrf())
                                .param("email", "neu@test.de")
                                .param("password", "geheim123")
                                .param("firstName", "Erika")
                                .param("lastName", "Mustermann")
                                .param("address", "Hoststraße 5")
                                .param("acceptedSpecies", "DOG", "CAT")
                                .param("availableFrom",
                                        LocalDate.now().plusDays(1).toString())
                                .param("availableUntil",
                                        LocalDate.now().plusDays(30).toString())
                                .param("pricePerWeek", "50.00"))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/login?registered"));

                verify(hostService).register(any());
            }

            @Test
            void postRegister_invalidEmail_returnsRegisterWithFieldError()
                    throws Exception {
                mockMvc.perform(post("/host/register")
                                .with(csrf())
                                .param("email", "keineEmail")
                                .param("password", "geheim123")
                                .param("firstName", "Erika")
                                .param("lastName", "Mustermann")
                                .param("address", "Hoststraße 5")
                                .param("acceptedSpecies", "DOG")
                                .param("availableFrom",
                                        LocalDate.now().plusDays(1).toString())
                                .param("availableUntil",
                                        LocalDate.now().plusDays(30).toString())
                                .param("pricePerWeek", "50.00"))
                        .andExpect(status().isOk())
                        .andExpect(view().name("host/register"))

                        .andExpect(model().attributeHasFieldErrors("registerForm", "email"));

                verify(hostService, never()).register(any());
            }

            @Test
            void
            postRegister_endBeforeStart_returnsRegisterWithoutCallingService()
                    throws Exception {
                // Cross-Field: availableUntil < availableFrom → @AssertTrue feuert
                mockMvc.perform(post("/host/register")
                                .with(csrf())
                                .param("email", "neu@test.de")
                                .param("password", "geheim123")
                                .param("firstName", "Erika")
                                .param("lastName", "Mustermann")
                                .param("address", "Hoststraße 5")
                                .param("acceptedSpecies", "DOG")
                                .param("availableFrom",
                                        LocalDate.now().plusDays(20).toString())
                                .param("availableUntil",
                                        LocalDate.now().plusDays(10).toString())
                                .param("pricePerWeek", "50.00"))
                        .andExpect(status().isOk())
                        .andExpect(view().name("host/register"));

                verify(hostService, never()).register(any());
            }

            @Test
            void postRegister_emailAlreadyTaken_showsFieldError() throws
                    Exception {
                when(hostService.register(any()))
                        .thenThrow(new
                                EmailAlreadyTakenException("doppelt@test.de"));

                mockMvc.perform(post("/host/register")
                                .with(csrf())
                                .param("email", "doppelt@test.de")
                                .param("password", "geheim123")
                                .param("firstName", "Erika")
                                .param("lastName", "Mustermann")
                                .param("address", "Hoststraße 5")
                                .param("acceptedSpecies", "DOG")
                                .param("availableFrom",
                                        LocalDate.now().plusDays(1).toString())
                                .param("availableUntil",
                                        LocalDate.now().plusDays(30).toString())
                                .param("pricePerWeek", "50.00"))
                        .andExpect(status().isOk())
                        .andExpect(view().name("host/register"))

                        .andExpect(model().attributeHasFieldErrors("registerForm", "email"));
            }

            // === Profil anzeigen ===

            @Test
            void getProfile_returnsProfileView() throws Exception {

                when(hostService.findByUserId(USER_ID)).thenReturn(validProfile());

                mockMvc.perform(get("/host/profile"))
                        .andExpect(status().isOk())
                        .andExpect(view().name("host/profile"))
                        .andExpect(model().attributeExists("profile"));
            }

            // === Profil bearbeiten ===

            @Test
            void getEditForm_returnsEditViewPrefilled() throws Exception {

                when(hostService.findByUserId(USER_ID)).thenReturn(validProfile());

                mockMvc.perform(get("/host/profile/edit"))
                        .andExpect(status().isOk())
                        .andExpect(view().name("host/profile-edit"))
                        .andExpect(model().attributeExists("updateForm"))
                        .andExpect(model().attributeExists("allSpecies"));
            }

            @Test
            void postEditForm_validForm_redirectsToProfile() throws Exception
            {
                mockMvc.perform(post("/host/profile/edit")
                                .with(csrf())
                                .param("firstName", "Neu-Vor")
                                .param("lastName", "Neu-Nach")
                                .param("address", "Neu-Str. 9")
                                .param("acceptedSpecies", "RABBIT")
                                .param("availableFrom",
                                        LocalDate.now().plusDays(2).toString())
                                .param("availableUntil",
                                        LocalDate.now().plusDays(40).toString())
                                .param("pricePerWeek", "75.00"))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/host/profile"));

                verify(hostService).update(eq(USER_ID), any());
            }

            @Test
            void postEditForm_invalidForm_returnsEditViewWithoutUpdate()
                    throws Exception {
                mockMvc.perform(post("/host/profile/edit")
                                .with(csrf())
                                .param("firstName", "")  // @NotBlank fails
                                .param("lastName", "Neu-Nach")
                                .param("address", "Neu-Str. 9")
                                .param("acceptedSpecies", "RABBIT")
                                .param("availableFrom",
                                        LocalDate.now().plusDays(2).toString())
                                .param("availableUntil",
                                        LocalDate.now().plusDays(40).toString())
                                .param("pricePerWeek", "75.00"))
                        .andExpect(status().isOk())
                        .andExpect(view().name("host/profile-edit"))

                        .andExpect(model().attributeHasFieldErrors("updateForm",
                                "firstName"));

                verify(hostService, never()).update(any(), any());
            }

            // === Security ===

            @Test
            @WithAnonymousUser
            void getProfile_unauthenticated_redirectsToLogin() throws
                    Exception {
                // SecurityConfig schützt /host/** → ohne Auth Redirect zur Login-Seite
                mockMvc.perform(get("/host/profile"))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/login"));
            }

            // === Helper ===

            private HostProfile validProfile() {
                User user = new User(EMAIL, "hash", UserRole.HOST);
                user.setId(USER_ID);
                return new HostProfile(
                        user, "Erika", "Mustermann", "Hoststraße 5",
                        EnumSet.of(PetSpecies.DOG),
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(30),
                        new BigDecimal("50.00")
                );
            }
        }