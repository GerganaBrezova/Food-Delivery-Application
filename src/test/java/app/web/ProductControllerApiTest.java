package app.web;

import app.product.model.Product;
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

import java.math.BigDecimal;
import java.util.UUID;

import static app.web.TestBuilder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    void postAuthenticatedRequestToProductCreationPageWithValidData_thenRedirectToHomeView() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = post("/products/add")
                .with(user(principal))
                .formField("name", "Dubai chocolate")
                .formField("category", ProductCategory.DESSERT.name())
                .formField("price", String.valueOf(30))
                .formField("imageUrl", "https://www.google.com")
                .formField("brandName", "Happy")
                .formField("restaurantAddress", "Sofia")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService, times(1)).getUserById(userId);
        verify(productService, times(1)).addNewProduct(any());
    }

    @Test
    void postAuthenticatedRequestToProductCreationPageWithInvalidData_thenReturnProductCreationPage() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = post("/products/add")
                .with(user(principal))
                .formField("name", "")
                .formField("category", ProductCategory.DESSERT.name())
                .formField("price", String.valueOf(30))
                .formField("imageUrl", "https://www.google.com")
                .formField("brandName", "")
                .formField("restaurantAddress", "")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("product-creation"))
                .andExpect(model().attributeExists("user"));

        verify(userService, times(1)).getUserById(userId);
        verify(productService, never()).addNewProduct(any());
    }

    @Test
    void getUnauthenticatedRequestToProductCreationPage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/products/add").with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
        verify(productService, never()).addNewProduct(any());
    }

    @Test
    void getAuthenticatedRequestToProductEditPage_thenReturnEditProductPage() throws Exception {

        Product product = testProductList().get(0);

        when(userService.getUserById(any())).thenReturn(testEmployee());
        when(productService.getProductById(any())).thenReturn(product);

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = get("/products/{id}/edit", product.getId())
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-product"))
                .andExpect(model().attributeExists("user", "product", "editProductRequest"));

        verify(userService, times(1)).getUserById(userId);
        verify(productService, times(1)).getProductById(any());
    }

    @Test
    void getUnauthenticatedRequestToProductEditPage_thenRedirectToLogin() throws Exception {

        Product product = testProductList().get(0);
        MockHttpServletRequestBuilder request = get("/products/{id}/edit", product.getId())
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
        verify(productService, never()).getProductById(any());
    }

    @Test
    void putAuthenticatedRequestToProductEditPageWithValidData_thenRedirectToHomePage() throws Exception {

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        Product product = testProductList().get(0);
        MockHttpServletRequestBuilder request = put("/products/{id}/edit", product.getId())
                .with(user(principal))
                .formField("name", "Dubai chocolate")
                .formField("category", ProductCategory.DESSERT.name())
                .formField("price", String.valueOf(30))
                .formField("imageUrl", "https://www.google.com")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService, never()).getUserById(userId);
        verify(productService, never()).getProductById(any());
        verify(productService, times(1)).editProduct(any(), any());
    }

    @Test
    void putAuthenticatedRequestToProductEditPageWithInvalidData_thenRedirectToEditProductPage() throws Exception {

        Product product = testProductList().get(0);
        when(userService.getUserById(any())).thenReturn(testEmployee());
        when(productService.getProductById(any())).thenReturn(product);

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = put("/products/{id}/edit", product.getId())
                .with(user(principal))
                .formField("name", "")
                .formField("category", ProductCategory.DESSERT.name())
                .formField("price", String.valueOf(30))
                .formField("imageUrl", "")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-product"))
                .andExpect(model().attributeExists("user", "product", "editProductRequest"));

        verify(userService, times(1)).getUserById(userId);
        verify(productService, times(1)).getProductById(any());
    }

    @Test
    void deleteAuthenticatedRequestToProductDeletePage_thenRedirectToHomePage() throws Exception {

        Product product = testProductList().get(0);
        when(productService.getProductById(any())).thenReturn(product);

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = delete("/products/{id}/delete", product.getId())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(productService, times(1)).getProductById(any());
        verify(productService, times(1)).deleteProduct(any());
    }

    @Test
    void deleteUnauthenticatedRequestToProductDeletePage_thenRedirectToLoginView() throws Exception {

        Product product = testProductList().get(0);
        MockHttpServletRequestBuilder request = get("/products/{id}/delete", product.getId());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(productService, never()).getProductById(any());
        verify(productService, never()).deleteProduct(any());
    }
}
