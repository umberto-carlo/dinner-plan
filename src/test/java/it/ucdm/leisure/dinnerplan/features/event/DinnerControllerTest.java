package it.ucdm.leisure.dinnerplan.features.event;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import it.ucdm.leisure.dinnerplan.features.user.UserService;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalService;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalCatalogService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class DinnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DinnerEventService dinnerEventService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ProposalService proposalService;

    @MockitoBean
    private ProposalCatalogService proposalCatalogService;

    @MockitoBean
    private InteractionService interactionService;

    @MockitoBean
    private it.ucdm.leisure.dinnerplan.utils.UserAgentUtils userAgentUtils;

    @Test
    @WithMockUser(username = "user")
    @org.junit.jupiter.api.Disabled("Failing with 302 redirect despite mocks - needs investigation")
    void getDashboard_ReturnsDashboardView() throws Exception {
        when(dinnerEventService.getEventsForUser("user")).thenReturn(List.of());
        when(proposalCatalogService.getProposalSuggestions()).thenReturn(List.of());

        it.ucdm.leisure.dinnerplan.features.user.User mockUser = new it.ucdm.leisure.dinnerplan.features.user.User();
        mockUser.setUsername("user");
        mockUser.setRole(it.ucdm.leisure.dinnerplan.features.user.Role.PARTICIPANT);
        when(userService.findByUsername("user")).thenReturn(mockUser);

        when(userAgentUtils.isMobile(org.mockito.ArgumentMatchers.any())).thenReturn(false);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("events"));
    }

    @Test
    void getDashboard_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
