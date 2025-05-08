package app.web;

import app.security.UserAuthDetails;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static app.TestBuilder.testCustomersList;
import static app.TestBuilder.testEmployee;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuthenticatedRequestToCustomersPage_thenReturnCustomersPage() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());
        when(userService.getAllCustomers()).thenReturn(testCustomersList());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = get("/customers")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("customers"))
                .andExpect(model().attributeExists("user", "allSystemCustomers"));

        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(1)).getAllCustomers();
    }

    @Test
    void getUnauthenticatedRequestToCustomersPage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/customers");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
        verify(userService, never()).getAllCustomers();
    }

    @Test
    void postAuthenticatedRequestToCustomersPromotionPage_thenRedirectToHomeView() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        UUID customerId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = post("/customers/{id}/promote", customerId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getUnauthenticatedRequestToCustomersPromotionPage_thenRedirectToLogin() throws Exception {

        UUID customerId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = get("/customers/{id}/promote}", customerId);

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
    }
}
