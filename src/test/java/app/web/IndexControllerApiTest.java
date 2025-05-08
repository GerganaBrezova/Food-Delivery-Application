package app.web;

import app.company.service.CompanyService;
import app.exceptions.EmailAlreadyExists;
import app.exceptions.UsernameAlreadyExists;
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

import static app.TestBuilder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CompanyService companyService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestToIndexEndpoint_thenReturnIndexView() throws Exception {

        MockHttpServletRequestBuilder request = get("/");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"));
    }

    @Test
    void getRequestToRegisterEndpoint_thenReturnRegisterView() throws Exception {

        MockHttpServletRequestBuilder request = get("/register");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void postRequestToRegisterEndpoint_happyPath() throws Exception {

        MockHttpServletRequestBuilder request = post("/register")
                .formField("firstName", "Gergana")
                .formField("lastName", "Brezova")
                .formField("email", "gery11@gmail.com")
                .formField("username", "Gery11")
                .formField("password", "123456")
                .formField("confirmPassword", "123456")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verify(userService, times(1)).register(any());
    }

    @Test
    void postRequestToRegisterEndpoint_whenUsernameAlreadyExist_thenRedirectToRegisterViewWithFlashParameter() throws Exception {

        doThrow(new UsernameAlreadyExists("Username already exist!")).when(userService).register(any());

        MockHttpServletRequestBuilder request = post("/register")
                .formField("firstName", "Gergana")
                .formField("lastName", "Brezova")
                .formField("email", "gery11@gmail.com")
                .formField("username", "Gery11")
                .formField("password", "123456")
                .formField("confirmPassword", "123456")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("usernameAlreadyExistMessage"));

        verify(userService, times(1)).register(any());
    }

    @Test
    void postRequestToRegisterEndpoint_whenEmailAlreadyExist_thenRedirectToRegisterViewWithFlashParameter() throws Exception {

        doThrow(new EmailAlreadyExists("Email already exists!")).when(userService).register(any());

        MockHttpServletRequestBuilder request = post("/register")
                .formField("firstName", "Gergana")
                .formField("lastName", "Brezova")
                .formField("email", "gery11@gmail.com")
                .formField("username", "Gery11")
                .formField("password", "123456")
                .formField("confirmPassword", "123456")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("emailAlreadyExistMessage"));

        verify(userService, times(1)).register(any());
    }

    @Test
    void postRequestToRegisterEndpoint_whenInvalidData_thenRedirectToRegisterView() throws Exception {

        doThrow(new UsernameAlreadyExists("Username already exist!")).when(userService).register(any());

        MockHttpServletRequestBuilder request = post("/register")
                .formField("firstName", "Gergana")
                .formField("lastName", "Brezova")
                .formField("email", "")
                .formField("username", "")
                .formField("password", "123456")
                .formField("confirmPassword", "123456")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"));

        verify(userService, never()).register(any());
    }

    @Test
    void getRequestToLoginEndpoint_thenReturnLoginView() throws Exception {

        MockHttpServletRequestBuilder request = get("/login");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void getRequestToLoginEndpoint_withErrorParameter_thenReturnLoginViewAndErrorMessageAttribute() throws Exception {

        MockHttpServletRequestBuilder request = get("/login")
                .param("error", "");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest", "errorMessage"));
    }

    @Test
    void getUnauthenticatedRequestToHome_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/home");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
        verify(userService, never()).getUserById(any());
    }

    @Test
    void getAuthenticatedRequestToHome_thenReturnHomeView() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());
        when(companyService.getAllCompanies()).thenReturn(testCompaniesList());
        when(companyService.getRestaurantsGroupedByCompany()).thenReturn(testRestaurantsMap());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = get("/home").with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user", "allCompanies", "restaurantsByCompany"));

        verify(userService, times(1)).getUserById(userId);
        verify(companyService, times(1)).getAllCompanies();
        verify(companyService, times(1)).getRestaurantsGroupedByCompany();
    }
}
