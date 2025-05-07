package app.web;

import app.company.model.Company;
import app.product.model.ProductCategory;
import app.restaurant.service.RestaurantService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantController.class)
public class RestaurantControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RestaurantService restaurantService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuthenticatedRequestToCategoriesPage_thenReturnCategotiesPage() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        Company company = testCompany();
        MockHttpServletRequestBuilder request = get("/restaurant/categories/{brandId}/location/{location}", company.getId(), "Sofia")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("categories"))
                .andExpect(model().attributeExists("categories", "brandId", "location", "user"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getUnauthenticatedRequestToCategoriesPage_thenRedirectToLogin() throws Exception {

        Company company = testCompany();
        MockHttpServletRequestBuilder request = get("/restaurant/categories/{brandId}/location/{location}", company.getId(), "Sofia");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
    }

    @Test
    void getAuthenticatedRequestToRestaurantCreationPage_thenReturnRestaurantCreationPage() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = get("/restaurant/add")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant-creation"))
                .andExpect(model().attributeExists("user", "createRestaurantRequest"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getUnauthenticatedRequestToRestaurantCreationPage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/restaurant/add");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
    }

    @Test
    void postAuthenticatedRequestToRestaurantCreationPageWithValidData_thenRedirectToHomeView() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = post("/restaurant/add")
                .with(user(principal))
                .formField("name", "Happy Sofia")
                .formField("address", "Sofia")
                .formField("brandName", "Happy")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService, times(1)).getUserById(userId);
        verify(restaurantService, times(1)).addNewRestaurant(any());
    }

    @Test
    void postAuthenticatedRequestToRestaurantCreationPageWithInvalidData_thenReturnRestaurantCreationPage() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = post("/restaurant/add")
                .with(user(principal))
                .formField("name", "")
                .formField("address", "")
                .formField("brandName", "Happy")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("restaurant-creation"))
                .andExpect(model().attributeExists("user"));

        verify(userService, times(1)).getUserById(userId);
        verify(restaurantService, never()).addNewRestaurant(any());
    }
}
