package it.ucdm.leisure.dinnerplan;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import it.ucdm.leisure.dinnerplan.controller.UserController;
import it.ucdm.leisure.dinnerplan.service.UserService;
import it.ucdm.leisure.dinnerplan.utils.UserAgentUtils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.mockito.ArgumentMatchers.anyString;

@WebMvcTest(UserController.class)
public class MobileUserListTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserAgentUtils userAgentUtils;

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testMobileUserList() throws Exception {
        Mockito.when(userAgentUtils.isMobile(anyString())).thenReturn(true);

        mockMvc.perform(
                get("/admin/users").header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)"))
                .andExpect(view().name("mobile/user_list"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    public void testDesktopUserList() throws Exception {
        Mockito.when(userAgentUtils.isMobile(anyString())).thenReturn(false);

        mockMvc.perform(get("/admin/users"))
                .andExpect(view().name("user_list"));
    }
}
