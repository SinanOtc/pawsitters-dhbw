package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.config.SecurityConfig;
import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import dhbw.heilbronn.pawsitters.service.CareRequestService;
import dhbw.heilbronn.pawsitters.service.PetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest bootet nur die Web Schicht für CareRequestController.
// Services + UserRepository werden gemockt → Controller komplett von DB entkoppelt.
// @WithMockUser auf Klassenebene → jeder Test läuft als eingeloggter Owner,
// einzelne Tests überschreiben das mit @WithAnonymousUser
@WebMvcTest(CareRequestController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("dev")
@WithMockUser(username = "owner@test.de", roles = "OWNER")
class CareRequestControllerTest {

    private static final String EMAIL = "owner@test.de";
    private static final Long USER_ID = 1L;
    private static final Long PET_ID = 42L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CareRequestService careRequestService;

    @MockitoBean
    private PetService petService;

    @MockitoBean
    private UserRepository userRepository;

    // currentUserId(...) im Controller braucht den User aus dem Repository.
    // Default-Setup für jeden Test: eingeloggter Owner mit ID 1
    @BeforeEach
    void mockAuthenticatedUser() {
        User user = new User(EMAIL, "hash", UserRole.OWNER);
        user.setId(USER_ID);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    // === Liste ===
    @Test
    void getCareRequests_returnsListView() throws Exception {
        when(careRequestService.findAllByOwner(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/owner/care-requests"))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/care-requests/list"))
                .andExpect(model().attributeExists("careRequests"));
    }

    // === Anlegen — GET ===
    @Test
    void getNewForm_returnsFormViewWithPetsAndEmptyRequestForm() throws Exception {
        when(petService.findAllByOwner(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/owner/care-requests/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/care-requests/form"))
                .andExpect(model().attributeExists("careRequestForm"))
                .andExpect(model().attributeExists("pets"));
    }

    // === Anlegen — POST ===
    @Test
    void postNew_validForm_redirectsToList() throws Exception {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(8);

        mockMvc.perform(post("/owner/care-requests/new")
                        .with(csrf())
                        .param("petId", PET_ID.toString())
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owner/care-requests"));

        verify(careRequestService).register(eq(USER_ID), any());
    }

    @Test
    void postNew_endBeforeStart_returnsFormWithoutRegister() throws Exception {
        // Cross-Field: Enddatum vor Startdatum → @AssertTrue isDateRangeValid feuert
        when(petService.findAllByOwner(USER_ID)).thenReturn(List.of());
        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = LocalDate.now().plusDays(5);

        mockMvc.perform(post("/owner/care-requests/new")
                        .with(csrf())
                        .param("petId", PET_ID.toString())
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/care-requests/form"))
                .andExpect(model().attributeExists("pets"));

        verify(careRequestService, never()).register(any(), any());
    }

    @Test
    void postNew_pastStartDate_returnsFormWithFieldError() throws Exception {
        // @Future feuert auf startDate → kein Service-Call
        when(petService.findAllByOwner(USER_ID)).thenReturn(List.of());
        LocalDate past = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(5);

        mockMvc.perform(post("/owner/care-requests/new")
                        .with(csrf())
                        .param("petId", PET_ID.toString())
                        .param("startDate", past.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/care-requests/form"))
                .andExpect(model().attributeHasFieldErrors("careRequestForm", "startDate"));

        verify(careRequestService, never()).register(any(), any());
    }

    @Test
    void postNew_missingPetId_returnsFormWithFieldError() throws Exception {
        // @NotNull auf petId → kein Service-Call
        when(petService.findAllByOwner(USER_ID)).thenReturn(List.of());

        mockMvc.perform(post("/owner/care-requests/new")
                        .with(csrf())
                        // petId fehlt
                        .param("startDate", LocalDate.now().plusDays(1).toString())
                        .param("endDate", LocalDate.now().plusDays(8).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/care-requests/form"))
                .andExpect(model().attributeHasFieldErrors("careRequestForm", "petId"));

        verify(careRequestService, never()).register(any(), any());
    }

    // === Security ===
    @Test
    @WithAnonymousUser
    void getCareRequests_unauthenticated_redirectsToLogin() throws Exception {
        // SecurityConfig schützt /owner/** → ohne Auth Redirect zur Login-Seite
        mockMvc.perform(get("/owner/care-requests"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}