package it.ucdm.leisure.dinnerplan;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ReproduceBugTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "umberto", roles = { "ORGANIZER" })
    void testAddProposal() throws Exception {
        // Need a valid event ID. Assuming 1 exists or fails gracefully if not found
        // (but logs will show).
        // Sending a typical datetime-local string.
        mockMvc.perform(post("/events/1/add-proposal")
                .param("dateOption", "2025-12-31T20:00") // Valid ISO Local Date Time
                .param("location", "Test Location")
                .param("address", "Test Address")
                .param("description", "Test Description"))
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }
}
