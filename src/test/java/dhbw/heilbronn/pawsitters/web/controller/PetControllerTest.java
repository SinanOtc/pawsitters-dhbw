package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.config.SecurityConfig;
import dhbw.heilbronn.pawsitters.domain.OwnerProfile;
import dhbw.heilbronn.pawsitters.domain.Pet;
import dhbw.heilbronn.pawsitters.domain.PetGender;
import dhbw.heilbronn.pawsitters.domain.PetSpecies;
import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
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

// @WebMvcTest bootet nur die Web Schicht für PetController.
// PetService + UserRepository werden gemockt → Controller komplett von DB entkoppelt.
// @WithMockUser auf Klassenebene → jeder Test läuft als eingeloggter Owner,
// einzelne Tests überschreiben das mit @WithAnonymousUser
@WebMvcTest(PetController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("dev")
@WithMockUser(username = "owner@test.de", roles = "OWNER")
class PetControllerTest {

    private static final String EMAIL = "owner@test.de";
    private static final Long USER_ID = 1L;
    private static final Long PET_ID = 42L;

    @Autowired
    private MockMvc mockMvc;

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
    void getPets_returnsListView() throws Exception {
        when(petService.findAllByOwner(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/owner/pets"))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/pets/list"))
                .andExpect(model().attributeExists("pets"));
    }

    // === Anlegen — GET ===
    @Test
    void getNewForm_returnsFormViewInNewMode() throws Exception {
        mockMvc.perform(get("/owner/pets/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/pets/form"))
                .andExpect(model().attributeExists("petForm"))
                .andExpect(model().attribute("mode", "new"));
    }

    // === Anlegen — POST ===
    @Test
    void postNew_validForm_redirectsToList() throws Exception {
        mockMvc.perform(post("/owner/pets/new")
                        .with(csrf())
                        .param("name", "Bello")
                        .param("species", "DOG")
                        .param("gender", "MALE")
                        .param("chipped", "false")
                        .param("vaccinated", "false")
                        .param("neutered", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owner/pets"));

        verify(petService).register(eq(USER_ID), any());
    }

    @Test
    void postNew_emptyName_returnsFormWithFieldError() throws Exception {
        mockMvc.perform(post("/owner/pets/new")
                        .with(csrf())
                        .param("name", "")
                        .param("species", "DOG")
                        .param("gender", "MALE")
                        .param("chipped", "false")
                        .param("vaccinated", "false")
                        .param("neutered", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/pets/form"))
                .andExpect(model().attribute("mode", "new"))
                .andExpect(model().attributeHasFieldErrors("petForm",
                        "name"));

        // Bei Validierungsfehlern darf der Service NICHT aufgerufen werden
        verify(petService, never()).register(any(), any());
    }

    @Test
    void postNew_chippedWithoutChipNumber_failsAssertTrue() throws
            Exception {
        // Edge Case: chipped=true aber chipNumber leer → @AssertTrue isChipDataConsistent feuert
        mockMvc.perform(post("/owner/pets/new")
                        .with(csrf())
                        .param("name", "Bello")
                        .param("species", "DOG")
                        .param("gender", "MALE")
                        .param("chipped", "true")
                        .param("chipNumber", "")
                        .param("vaccinated", "false")
                        .param("neutered", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/pets/form"));

        verify(petService, never()).register(any(), any());
    }

    // === Bearbeiten — GET ===
    @Test
    void getEditForm_existingPet_returnsFormPrefilled() throws Exception {
        Pet pet = createPet();
        when(petService.findByIdForOwner(PET_ID, USER_ID)).thenReturn(pet);

        mockMvc.perform(get("/owner/pets/{id}/edit", PET_ID))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/pets/form"))
                .andExpect(model().attributeExists("petForm"))
                .andExpect(model().attribute("mode", "edit"))
                .andExpect(model().attribute("petId", PET_ID));
    }

    // === Bearbeiten — POST ===
    @Test
    void postEdit_validForm_redirectsToList() throws Exception {
        mockMvc.perform(post("/owner/pets/{id}/edit", PET_ID)
                        .with(csrf())
                        .param("name", "Bello")
                        .param("species", "DOG")
                        .param("gender", "MALE")
                        .param("chipped", "false")
                        .param("vaccinated", "false")
                        .param("neutered", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owner/pets"));

        verify(petService).update(eq(PET_ID), eq(USER_ID), any());
    }

    @Test
    void postEdit_invalidForm_returnsFormInEditModeWithoutUpdate() throws
            Exception {
        mockMvc.perform(post("/owner/pets/{id}/edit", PET_ID)
                        .with(csrf())
                        .param("name", "")
                        .param("species", "DOG")
                        .param("gender", "MALE")
                        .param("chipped", "false")
                        .param("vaccinated", "false")
                        .param("neutered", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("owner/pets/form"))
                .andExpect(model().attribute("mode", "edit"))
                .andExpect(model().attribute("petId", PET_ID))
                .andExpect(model().attributeHasFieldErrors("petForm",
                        "name"));

        // Validation-Fail → kein Update, sonst landen ungültige Daten in der DB
        verify(petService, never()).update(any(), any(), any());
    }

    // === Löschen ===
    @Test
    void postDelete_redirectsToList() throws Exception {
        mockMvc.perform(post("/owner/pets/{id}/delete", PET_ID)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/owner/pets"));

        verify(petService).delete(PET_ID, USER_ID);
    }

    // === Security ===
    @Test
    @WithAnonymousUser
    void getPets_unauthenticated_redirectsToLogin() throws Exception {
        // SecurityConfig schützt /owner/** → ohne Auth Redirect zur Login-Seite
        mockMvc.perform(get("/owner/pets"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // === Helper ===

    // Test-Pet mit allen Pflichtfeldern.
    // Mock von findByIdForOwner gibt das Pet zurück, Controller liest die Werte für toForm()
    private Pet createPet() {
        User user = new User(EMAIL, "hash", UserRole.OWNER);
        OwnerProfile owner = new OwnerProfile(user, "Max", "Muster",
                "Adresse 1");
        Pet pet = new Pet(owner, "Bello", PetSpecies.DOG, PetGender.MALE);
        pet.setId(PET_ID);
        return pet;
    }
}