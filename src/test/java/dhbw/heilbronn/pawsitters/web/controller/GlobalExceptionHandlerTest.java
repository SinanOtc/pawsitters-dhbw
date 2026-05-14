package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.config.SecurityConfig;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

// Testet den GlobalExceptionHandler über den top-levelThrowingTestController,
// der gezielt jede gemappte Exception wirft.
@WebMvcTest(controllers = ThrowingTestController.class)
@Import({SecurityConfig.class,
        GlobalExceptionHandler.class})
@ActiveProfiles("dev")
@WithMockUser(roles = "OWNER")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // SecurityConfig zieht transitiv den CustomUserDetailsService, der UserRepository braucht.
    // Mock damit der Test-Context sauber lädt — wir nutzen den Mock selbst nicht.
    @MockitoBean
    private UserRepository userRepository;

    // === 404-Handler ===

    @Test
    void petNotFound_returns404View() throws Exception {
        mockMvc.perform(get("/test/pet-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))

                .andExpect(model().attributeExists("message"));
    }

    @Test
    void careRequestNotFound_returns404View() throws
            Exception {

        mockMvc.perform(get("/test/care-request-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))

                .andExpect(model().attributeExists("message"));
    }

    @Test
    void offerNotFound_returns404View() throws Exception {
        mockMvc.perform(get("/test/offer-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))

                .andExpect(model().attributeExists("message"));
    }

    @Test
    void hostProfileNotFound_returns404View() throws
            Exception {

        mockMvc.perform(get("/test/host-profile-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))

                .andExpect(model().attributeExists("message"));
    }

    // === 409-Handler ===

    @Test
    void offerNotPending_returns409View() throws Exception
    {
        mockMvc.perform(get("/test/offer-not-pending"))
                .andExpect(status().isConflict())
                .andExpect(view().name("error/409"))

                .andExpect(model().attributeExists("message"));
    }

    @Test
    void offerNotEligible_returns409View() throws
            Exception {
        mockMvc.perform(get("/test/offer-not-eligible"))
                .andExpect(status().isConflict())
                .andExpect(view().name("error/409"))

                .andExpect(model().attributeExists("message"));
    }
}