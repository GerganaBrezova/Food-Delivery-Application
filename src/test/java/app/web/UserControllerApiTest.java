package app.web;

import app.product.model.Product;
import app.product.model.ProductCategory;
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

import static app.web.TestBuilder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuthenticatedRequestToUserProfilePage_thenReturnUserProfilePage() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "customer_user", "123456", "customer_user@gmail.com", "Customer", "User", UserRole.CUSTOMER, true);

        MockHttpServletRequestBuilder request = get("/user/{id}/profile", userId)
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user", "editProfileRequest"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getUnauthenticatedRequestToUserProfilePage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/user/{id}/profile", testCustomer().getId());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
    }

    @Test
    void putAuthenticatedRequestToUserEditProfilePageWithValidData_thenRedirectToHomePage() throws Exception {

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "customer_user", "123456", "customer_user@gmail.com", "Customer", "User", UserRole.CUSTOMER, true);

        MockHttpServletRequestBuilder request = put("/user/{id}/profile/edit", userId)
                .with(user(principal))
                .formField("firstName", "John")
                .formField("lastName", "Doe")
                .formField("username", "john_doe")
                .formField("email", "john.doe@gmail.com")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService, never()).getUserById(userId);
        verify(userService, times(1)).editUserDetails(any(), any());
    }

    @Test
    void putAuthenticatedRequestToUserEditProfilePageWithInvalidData_thenRedirectToHomePage() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "customer_user", "123456", "customer_user@gmail.com", "Customer", "User", UserRole.CUSTOMER, true);

        MockHttpServletRequestBuilder request = put("/user/{id}/profile/edit", userId)
                .with(user(principal))
                .formField("firstName", "")
                .formField("lastName", "")
                .formField("username", "")
                .formField("email", "john.doe@gmail.com")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user", "editProfileRequest"));

        verify(userService, times(1)).getUserById(userId);
        verify(userService, never()).editUserDetails(any(), any());
    }
}
