package dhbw.heilbronn.pawsitters.web.controller;

import dhbw.heilbronn.pawsitters.config.SecurityConfig;
import dhbw.heilbronn.pawsitters.domain.User;
import dhbw.heilbronn.pawsitters.domain.UserRole;
import dhbw.heilbronn.pawsitters.repository.UserRepository;
import dhbw.heilbronn.pawsitters.service.OfferService;
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

// @WebMvcTest für OfferController. Beide Rollen (HOST und OWNER) werden getestet,
// deshalb keine @WithMockUser-Klassen-Annotation — pro Test einzeln gesetzt.
@WebMvcTest(OfferController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("dev")
class OfferControllerTest {

    private static final String HOST_EMAIL = "host@test.de";
    private static final Long HOST_USER_ID = 1L;
    private static final String OWNER_EMAIL = "owner@test.de";
    private static final Long OWNER_USER_ID = 2L;
    private static final Long CARE_REQUEST_ID = 42L;
    private static final Long OFFER_ID = 100L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OfferService offerService;

    @MockitoBean
    private UserRepository userRepository;

    // Mocks für beide Rollen — pro Test wird per @WithMockUser entschieden welche aktiv ist
    @BeforeEach
    void mockUsers() {
        User host = new User(HOST_EMAIL, "hash",
                UserRole.HOST);
        host.setId(HOST_USER_ID);
        when(userRepository.findByEmail(HOST_EMAIL)).thenReturn(Optional.of(host));

        User owner = new User(OWNER_EMAIL, "hash",
                UserRole.OWNER);
        owner.setId(OWNER_USER_ID);
        when(userRepository.findByEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    }

    // === Host: Browse passende Anfragen ===
    @Test
    @WithMockUser(username = HOST_EMAIL, roles = "HOST")
    void getBrowseMatchingRequests_returnsListView() throws
            Exception {
        when(offerService.findMatchingRequests(HOST_USER_ID
        )).thenReturn(List.of());

        mockMvc.perform(get("/host/care-requests"))
                .andExpect(status().isOk())

                .andExpect(view().name("host/care-requests/list"))

                .andExpect(model().attributeExists("matchingRequests"));
    }

    // === Host: Offer-Form ===
    @Test
    @WithMockUser(username = HOST_EMAIL, roles = "HOST")
    void getOfferForm_returnsFormViewWithCareRequestId()
            throws Exception {

        mockMvc.perform(get("/host/care-requests/{id}/offer",
                        CARE_REQUEST_ID))
                .andExpect(status().isOk())

                .andExpect(view().name("host/care-requests/offer-form"))

                .andExpect(model().attributeExists("offerForm"))

                .andExpect(model().attribute("careRequestId",
                        CARE_REQUEST_ID));
    }

    // === Host: Offer senden ===
    @Test
    @WithMockUser(username = HOST_EMAIL, roles = "HOST")
    void postCreateOffer_validForm_redirectsToHostOffers()
            throws Exception {

        mockMvc.perform(post("/host/care-requests/{id}/offer",
                        CARE_REQUEST_ID)
                        .with(csrf())
                        .param("weeklyPrice", "60.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/host/offers"));

        verify(offerService).createOffer(eq(HOST_USER_ID),
                eq(CARE_REQUEST_ID), any());
    }

    @Test
    @WithMockUser(username = HOST_EMAIL, roles = "HOST")
    void postCreateOffer_invalidPrice_returnsFormViewWithoutService() throws Exception {
        // @DecimalMin(inclusive=false) → 0 ist NICHT erlaubt

        mockMvc.perform(post("/host/care-requests/{id}/offer",
                        CARE_REQUEST_ID)
                        .with(csrf())
                        .param("weeklyPrice", "0.00"))
                .andExpect(status().isOk())

                .andExpect(view().name("host/care-requests/offer-form"))

                .andExpect(model().attribute("careRequestId",
                        CARE_REQUEST_ID))

                .andExpect(model().attributeHasFieldErrors("offerForm",
                        "weeklyPrice"));

        verify(offerService, never()).createOffer(any(),
                any(), any());
    }

    // === Host: Eigene Offers ===
    @Test
    @WithMockUser(username = HOST_EMAIL, roles = "HOST")
    void getHostOffers_returnsHostOffersView() throws
            Exception {
        when(offerService.findOffersByHost(HOST_USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/host/offers"))
                .andExpect(status().isOk())
                .andExpect(view().name("host/offers/list"))

                .andExpect(model().attributeExists("offers"));
    }

    // === Owner: Eingegangene Offers ===
    @Test
    @WithMockUser(username = OWNER_EMAIL, roles = "OWNER")
    void getOwnerOffers_returnsOwnerOffersView() throws
            Exception {

        when(offerService.findOffersByCareRequest(OWNER_USER_ID,
                CARE_REQUEST_ID))
                .thenReturn(List.of());


        mockMvc.perform(get("/owner/care-requests/{id}/offers",
                        CARE_REQUEST_ID))
                .andExpect(status().isOk())

                .andExpect(view().name("owner/care-requests/offers"))

                .andExpect(model().attributeExists("offers"))

                .andExpect(model().attribute("careRequestId",
                        CARE_REQUEST_ID));
    }

    // === Security ===
    @Test
    @WithAnonymousUser
    void getBrowseMatchingRequests_unauthenticated_redirectsToLogin() throws Exception {
        // SecurityConfig schützt /host/** → ohne Auth Redirect zur Login-Seite
        mockMvc.perform(get("/host/care-requests"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // === Owner: Offer annehmen ===
    @Test
    @WithMockUser(username = OWNER_EMAIL, roles = "OWNER")
    void postAcceptOffer_validOffer_redirectsToCareRequests()
            throws Exception {
        mockMvc.perform(post("/owner/offers/{offerId}/accept",
                        OFFER_ID)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())

                .andExpect(redirectedUrl("/owner/care-requests"));

        verify(offerService).accept(OFFER_ID, OWNER_USER_ID);
    }
}
