package app.web;

import app.company.service.CompanyService;
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
import java.time.LocalDateTime;
import java.util.UUID;

import static app.TestBuilder.testCouriersList;
import static app.TestBuilder.testEmployee;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
public class CompanyControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CompanyService companyService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuthenticatedRequestToCompanyCreationPage_thenReturnCompanyCreationView() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = get("/company/add")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("brand-creation"))
                .andExpect(model().attributeExists("user", "createCompanyRequest"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getUnauthenticatedRequestToCompanyCreationPage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/company/add");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(companyService, never()).createCompany(any());
    }

    @Test
    void postAuthenticatedRequestToCompanyCreationPage_thenRedirectToHomeView() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = post("/company/add")
                .with(user(principal))
                .formField("name", "KFC")
                .formField("description", "Go eat!")
                .formField("logoUrl", "https://www.google.com")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(userService, times(1)).getUserById(userId);
        verify(companyService, times(1)).createCompany(any());
    }

    @Test
    void postUnauthenticatedRequestToCompanyCreationPage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = post("/company/add").with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void postAuthenticatedRequestToCompanyCreationPageWhenInvalidData_thenReturnCompanyCreationPage() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = post("/company/add")
                .with(user(principal))
                .formField("name", "KFC")
                .formField("description", "")
                .formField("logoUrl", "")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("brand-creation"))
                .andExpect(model().attributeExists("user"));

        verify(companyService, never()).createCompany(any());
    }

    @Test
    void deleteAuthenticatedRequestToCompanyPage_thenRedirectToHomeView() throws Exception {

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        UUID companyId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = delete("/company/{id}/delete", companyId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(companyService, times(1)).getCompanyById(companyId);
        verify(companyService, times(1)).deleteCompany(any());
    }

    @Test
    void deleteUnauthenticatedRequestToCompanyPage_thenRedirectToLoginView() throws Exception {

        UUID companyId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = get("/company/{id}/delete", companyId);

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(companyService, never()).getCompanyById(companyId);
        verify(companyService, never()).deleteCompany(any());
    }

    @Test
    void getAuthenticatedRequestToCompanyStatisticsPage_thenReturnCompanyStatisticsView() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());
        when(userService.getAllCouriers()).thenReturn(testCouriersList());
        when(userService.getRevenueFromCouriers(testCouriersList())).thenReturn(BigDecimal.valueOf(11));

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = get("/company/statistics")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("statistics"))
                .andExpect(model().attributeExists("user", "allSystemCouriers", "revenue"));

        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(1)).getAllCouriers();
        verify(userService, times(1)).getRevenueFromCouriers(testCouriersList());
    }

    @Test
    void getUnauthenticatedRequestToCompanyStatisticsPage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/company/statistics");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
        verify(userService, never()).getAllCouriers();
        verify(userService, never()).getRevenueFromCouriers(any());
    }

    @Test
    void getAuthenticatedRequestToCompanyStatisticsFilterPageWithParameters_thenReturnCompanyStatisticsView() throws Exception {

        LocalDateTime from = LocalDateTime.of(2024, 11, 3, 12, 33);
        LocalDateTime to = LocalDateTime.of(2024, 11, 9, 12, 33);

        when(userService.getUserById(any())).thenReturn(testEmployee());
        when(userService.getCouriersByOrderCompletionDate(from, to)).thenReturn(testCouriersList());
        when(companyService.getTotalRevenue(testCouriersList())).thenReturn(BigDecimal.valueOf(11));

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = get("/company/statistics/filter")
                .param("from", String.valueOf(from))
                .param("to", String.valueOf(to))
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("statistics"))
                .andExpect(model().attributeExists("user", "allSystemCouriers", "revenue"));

        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(1)).getCouriersByOrderCompletionDate(any(), any());
        verify(userService, never()).getAllCouriers();
        verify(companyService, times(1)).getTotalRevenue(any());
    }

    @Test
    void getAuthenticatedRequestToCompanyStatisticsFilterPageWithoutParameters_thenReturnCompanyStatisticsView() throws Exception {

        when(userService.getUserById(any())).thenReturn(testEmployee());
        when(userService.getAllCouriers()).thenReturn(testCouriersList());
        when(companyService.getTotalRevenue(testCouriersList())).thenReturn(BigDecimal.valueOf(11));

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "employee_user", "123456", "employee_user@gmail.com", "Employee", "User", UserRole.EMPLOYEE, true);

        MockHttpServletRequestBuilder request = get("/company/statistics/filter").with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("statistics"))
                .andExpect(model().attributeExists("user", "allSystemCouriers", "revenue"));

        verify(userService, times(1)).getUserById(userId);
        verify(userService, times(1)).getAllCouriers();
        verify(userService, never()).getCouriersByOrderCompletionDate(any(), any());
        verify(companyService, times(1)).getTotalRevenue(any());
    }

    @Test
    void getUnauthenticatedRequestToCompanyStatisticsFilterPage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/company/statistics/filter");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
        verify(userService, never()).getAllCouriers();
        verify(userService, never()).getCouriersByOrderCompletionDate(any(), any());
        verify(companyService, never()).getTotalRevenue(any());
    }
}
