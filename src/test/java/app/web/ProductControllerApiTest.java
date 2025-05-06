package app.web;

import app.product.model.ProductCategory;
import app.product.service.ProductService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerApiTest {

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuthenticatedRequestToProductsPageWithValidParameters_thenReturnProductsPage() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());
        when(productService.getAllCategoryProducts(any(), any(), any())).thenReturn(testProductList());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        UUID brandId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = get("/products")
                .param("brandId", brandId.toString())
                .param("location", "Sofia")
                .param("categoryName", ProductCategory.DESSERT.name())
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("user", "categoryProducts"));

        verify(userService, times(1)).getUserById(userId);
        verify(productService, times(1)).getAllCategoryProducts(any(), any(), any());
    }

    @Test
    void getUnauthenticatedRequestToProductsPagePage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/products");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
        verify(productService, never()).getAllCategoryProducts(any(), any(), any());
    }

    @Test
    void getAuthenticatedRequestToProductsAddPage_thenReturnProductsCreationPage() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = get("/products/add")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("product-creation"))
                .andExpect(model().attributeExists("user", "createProductRequest"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getUnauthenticatedRequestToProductsAddPage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/products/add");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
    }
}
