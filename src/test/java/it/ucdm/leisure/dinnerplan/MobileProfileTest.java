package it.ucdm.leisure.dinnerplan;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.model.Role;
import it.ucdm.leisure.dinnerplan.service.UserService;
import it.ucdm.leisure.dinnerplan.utils.UserAgentUtils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class MobileProfileTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserAgentUtils userAgentUtils;

    @Test
    @WithMockUser(username = "user")
    public void testMobileProfile() throws Exception {
        Mockito.when(userAgentUtils.isMobile(anyString())).thenReturn(true);
        User user = new User();
        user.setUsername("user");
        user.setPassword("pass");
        user.setRole(Role.PARTICIPANT);
        Mockito.when(userService.findByUsername("user")).thenReturn(user);

        mockMvc.perform(get("/profile").header("User-Agent", "Mobile"))
                .andExpect(view().name("mobile/profile"));
    }

    @Test
    @WithMockUser(username = "user")
    public void testDesktopProfile() throws Exception {
        Mockito.when(userAgentUtils.isMobile(anyString())).thenReturn(false);
        User user = new User();
        user.setUsername("user");
        user.setPassword("pass");
        user.setRole(Role.PARTICIPANT);
        Mockito.when(userService.findByUsername("user")).thenReturn(user);

        mockMvc.perform(get("/profile"))
                .andExpect(view().name("profile"));
    }
}
