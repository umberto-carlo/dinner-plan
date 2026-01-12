package it.ucdm.leisure.dinnerplan;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mail.javamail.JavaMailSender;

import it.ucdm.leisure.dinnerplan.features.user.User;
import it.ucdm.leisure.dinnerplan.features.user.Role;
import it.ucdm.leisure.dinnerplan.features.user.UserService;
import it.ucdm.leisure.dinnerplan.utils.UserAgentUtils;
import org.springframework.context.MessageSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ProfileNotificationTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private UserAgentUtils userAgentUtils;

        @MockitoBean
        private MessageSource messageSource;

        @MockitoBean
        private JavaMailSender javaMailSender;

        @Test
        @WithMockUser(username = "user")
        public void testChangePasswordNotification() throws Exception {
                User user = new User();
                user.setUsername("user");
                user.setRole(Role.PARTICIPANT);
                Mockito.when(userService.findByUsername("user")).thenReturn(user);
                Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.any()))
                                .thenReturn("Success Message");

                mockMvc.perform(post("/profile/change-password")
                                .param("newPassword", "newpass123"))
                                .andExpect(model().attributeExists("passwordSuccess"))
                                .andExpect(model().attributeDoesNotExist("emailSuccess"))
                                .andExpect(view().name("profile"));
        }

        @Test
        @WithMockUser(username = "user")
        public void testUpdateEmailNotification() throws Exception {
                User user = new User();
                user.setUsername("user");
                user.setRole(Role.PARTICIPANT);
                Mockito.when(userService.findByUsername("user")).thenReturn(user);
                Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.any()))
                                .thenReturn("Success Message");

                mockMvc.perform(post("/profile/update-email")
                                .param("newEmail", "new@example.com"))
                                .andExpect(model().attributeExists("emailSuccess"))
                                .andExpect(model().attributeDoesNotExist("passwordSuccess"))
                                .andExpect(view().name("profile"));
        }

        @Test
        @WithMockUser(username = "user")
        public void testGetProfile() throws Exception {
                User user = new User();
                user.setUsername("user");
                user.setRole(Role.ORGANIZER);
                Mockito.when(userService.findByUsername("user")).thenReturn(user);

                mockMvc.perform(get("/profile"))
                                .andExpect(status().isOk())
                                .andExpect(model().attributeExists("user"))
                                .andExpect(view().name("profile"));
        }

        @Test
        @WithMockUser(username = "user")
        public void testUpdateEmailWithEmptyValue() throws Exception {
                User user = new User();
                user.setUsername("user");
                user.setRole(Role.PARTICIPANT);
                Mockito.when(userService.findByUsername("user")).thenReturn(user);
                Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.any()))
                                .thenReturn("Success Message");

                mockMvc.perform(post("/profile/update-email")
                                .param("newEmail", ""))
                                .andExpect(model().attributeExists("emailSuccess"))
                                .andExpect(view().name("profile"));
        }
}
