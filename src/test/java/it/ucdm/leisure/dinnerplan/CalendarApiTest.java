package it.ucdm.leisure.dinnerplan;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import it.ucdm.leisure.dinnerplan.controller.DinnerController;
import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.model.Proposal;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.model.Role;
import it.ucdm.leisure.dinnerplan.service.DinnerEventService;
import it.ucdm.leisure.dinnerplan.service.InteractionService;
import it.ucdm.leisure.dinnerplan.service.ProposalService;
import it.ucdm.leisure.dinnerplan.service.UserService;
import it.ucdm.leisure.dinnerplan.utils.UserAgentUtils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@WebMvcTest(DinnerController.class)
public class CalendarApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DinnerEventService dinnerEventService;

    @MockBean
    private ProposalService proposalService;

    @MockBean
    private InteractionService interactionService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserAgentUtils userAgentUtils;

    @Test
    @WithMockUser(username = "testuser")
    public void testGetCalendarEvents() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("pass");
        user.setRole(Role.PARTICIPANT);

        DinnerEvent event = new DinnerEvent();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setDescription("Desc");
        event.setDeadline(LocalDateTime.now().plusDays(5));
        event.setOrganizer(user);
        event.setStatus(DinnerEvent.EventStatus.OPEN);

        Mockito.when(dinnerEventService.getEventsForUser("testuser")).thenReturn(Collections.singletonList(event));
        Mockito.when(userService.findByUsername("testuser")).thenReturn(user);

        mockMvc.perform(get("/api/calendar/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type", is("DEADLINE")));
    }
}
