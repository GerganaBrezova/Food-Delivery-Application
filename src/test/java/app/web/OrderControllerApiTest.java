package app.web;

import app.exceptions.EmailAlreadyExists;
import app.exceptions.NoAddressSelected;
import app.exceptions.OrderAlreadyPickedUp;
import app.exceptions.OrderHasNoProducts;
import app.order.model.Order;
import app.order.model.OrderStatus;
import app.order.service.OrderService;
import app.product.model.Product;
import app.product.service.ProductService;
import app.security.UserAuthDetails;
import app.user.model.Courier;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static app.web.TestBuilder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuthenticatedRequestToOrdersPage_thenReturnOrdersView() throws Exception {

        List<Order> orders = new ArrayList<>(List.of(new Order(), new Order()));

        when(userService.getUserById(any())).thenReturn(testCustomer());
        when(orderService.getAllUserOrders(testCustomer())).thenReturn(orders);

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "customer_user", "123456", "customer_user@gmail.com", "Customer", "User", UserRole.CUSTOMER, true);

        MockHttpServletRequestBuilder request = get("/orders")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("user", "userOrders"));

        verify(userService, times(1)).getUserById(userId);
        verify(orderService, times(1)).getAllUserOrders(any());
    }

    @Test
    void getUnauthenticatedRequestToOrdersPage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/orders");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
        verify(orderService, never()).getAllUserOrders(any());
    }

    @Test
    void getAuthenticatedRequestToCourierOrdersPage_thenReturnCourierOrdersView() throws Exception {

        when(userService.getUserById(any())).thenReturn(testCourier());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "courier_user", "123456", "courier_user@gmail.com", "Courier", "User", UserRole.COURIER, true);

        MockHttpServletRequestBuilder request = get("/orders/courier")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("courier-completed-orders"))
                .andExpect(model().attributeExists("user", "completedOrders"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getUnauthenticatedRequestToCourierOrdersPage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/orders/courier");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
    }

    @Test
    void getAuthenticatedRequestToBasketPage_thenReturnBasketView() throws Exception {

        Order order = Order.builder()
                .customer(testCustomer())
                .totalPrice(BigDecimal.valueOf(100))
                .build();

        when(userService.getUserById(any())).thenReturn(testCustomer());
        when(orderService.getOrCreateOrder(any())).thenReturn(order);

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "customer_user", "123456", "customer_user@gmail.com", "Customer", "User", UserRole.CUSTOMER, true);

        MockHttpServletRequestBuilder request = get("/orders/basket")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("basket"))
                .andExpect(model().attributeExists("user", "order"));

        verify(userService, times(1)).getUserById(userId);
        verify(orderService, times(1)).getOrCreateOrder(any());
    }

    @Test
    void getUnauthenticatedRequestToBasketPage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/orders/basket");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
        verify(orderService, never()).getOrCreateOrder(any());
    }

    @Test
    void postAuthenticatedRequestToBasketAddProductPage_thenRedirectToBasketView() throws Exception {

        UUID productId = UUID.randomUUID();
        Order order = Order.builder()
                .customer(testCustomer())
                .totalPrice(BigDecimal.valueOf(100))
                .build();

        when(userService.getUserById(any())).thenReturn(testCustomer());
        when(productService.getProductById(productId)).thenReturn(new Product());
        when(orderService.getOrCreateOrder(any())).thenReturn(order);

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "customer_user", "123456", "customer_user@gmail.com", "Customer", "User", UserRole.CUSTOMER, true);

        MockHttpServletRequestBuilder request = post("/orders/basket/add-product/{productId}", productId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/basket"));

        verify(userService, times(1)).getUserById(userId);
        verify(productService, times(1)).getProductById(productId);
        verify(orderService, times(1)).getOrCreateOrder(any());
    }

    @Test
    void getUnauthenticatedRequestToBasketAddProductPage_thenRedirectToLogin() throws Exception {

        UUID productId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = get("/orders/basket/add-product/{productId}", productId);

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
        verify(productService, never()).getProductById(any());
        verify(orderService, never()).getOrCreateOrder(any());
    }

    @Test
    void deleteAuthenticatedRequestToBasketDeleteProductPage_thenRedirectToBasketView() throws Exception {

        UUID productId = UUID.randomUUID();
        Order order = Order.builder()
                .customer(testCustomer())
                .totalPrice(BigDecimal.valueOf(100))
                .build();

        when(userService.getUserById(any())).thenReturn(testCustomer());
        when(productService.getProductById(productId)).thenReturn(new Product());
        when(orderService.getCurrentOrder(testCustomer())).thenReturn(order);

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "customer_user", "123456", "customer_user@gmail.com", "Customer", "User", UserRole.CUSTOMER, true);

        MockHttpServletRequestBuilder request = delete("/orders/basket/delete-product/{productId}", productId)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/basket"));

        verify(userService, times(1)).getUserById(userId);
        verify(productService, times(1)).getProductById(productId);
        verify(orderService, times(1)).getCurrentOrder(any());
    }

    @Test
    void deleteUnauthenticatedRequestToBasketDeleteProductPage_thenRedirectToLoginView() throws Exception {

        UUID productId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = get("/orders/basket/delete-product/{productId}", productId);

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
        verify(productService, never()).getProductById(any());
        verify(orderService, never()).getCurrentOrder(any());
    }

    @Test
    void getAuthenticatedRequestToOrderSuccessPage_thenReturnOrderSuccessView() throws Exception {

        when(userService.getUserById(any())).thenReturn(testCustomer());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "customer_user", "123456", "customer_user@gmail.com", "Customer", "User", UserRole.CUSTOMER, true);

        MockHttpServletRequestBuilder request = get("/orders/success")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("order-success"))
                .andExpect(model().attributeExists("user"));

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getUnauthenticatedRequestToOrderSuccessPage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/orders/success");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
    }

    @Test
    void postAuthenticatedRequestToBasketPlaceOrderPageWithValidParamAndProducts_thenRedirectToOrderSuccessView() throws Exception {

        Order order = Order.builder()
                .customer(testCustomer())
                .totalPrice(BigDecimal.valueOf(100))
                .products(List.of(new Product()))
                .build();

        when(userService.getUserById(any())).thenReturn(testCustomer());
        when(orderService.getCurrentOrder(any())).thenReturn(order);
        doNothing().when(orderService).validateAddressProvided(anyString());
        doNothing().when(orderService).validateOrderHasProducts(any());
        doNothing().when(orderService).updateOrderDetails(any(), anyString());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "customer_user", "123456", "customer_user@gmail.com", "Customer", "User", UserRole.CUSTOMER, true);

        UUID orderId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = post("/orders/basket/place-order/{orderId}", orderId)
                .param("address", "Smolyan")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/success"));

        verify(userService, times(1)).getUserById(userId);
        verify(orderService, times(1)).getCurrentOrder(any());
        verify(orderService, times(1)).updateOrderDetails(any(), any());
    }

    @Test
    void postAuthenticatedRequestToBasketPlaceOrderPageWithValidParamAndNoProducts_thenThrowOrderHasNoProductsException() throws Exception {

        Order order = Order.builder()
                .customer(testCustomer())
                .totalPrice(BigDecimal.valueOf(100))
                .build();

        when(userService.getUserById(any())).thenReturn(testCustomer());
        when(orderService.getCurrentOrder(any())).thenReturn(order);
        doThrow(new OrderHasNoProducts("Cannot place an order with no products.")).when(orderService).validateOrderHasProducts(any());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "customer_user", "123456", "customer_user@gmail.com", "Customer", "User", UserRole.CUSTOMER, true);

        UUID orderId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = post("/orders/basket/place-order/{orderId}", orderId)
                .param("address", "Smolyan")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/basket"))
                .andExpect(flash().attributeExists("orderHasNoProductsMessage"));

        verify(userService, times(1)).getUserById(userId);
        verify(orderService, times(1)).getCurrentOrder(any());
        verify(orderService, never()).updateOrderDetails(any(), any());
    }

    @Test
    void postAuthenticatedRequestToBasketPlaceOrderPageWithInvalidParamAndProducts_thenRedirectToOrderSuccessView() throws Exception {

        Order order = Order.builder()
                .customer(testCustomer())
                .totalPrice(BigDecimal.valueOf(100))
                .products(List.of(new Product()))
                .build();

        when(userService.getUserById(any())).thenReturn(testCustomer());
        when(orderService.getCurrentOrder(any())).thenReturn(order);
        doThrow(new NoAddressSelected("Can not place an order with no address!")).when(orderService).validateAddressProvided("");

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "customer_user", "123456", "customer_user@gmail.com", "Customer", "User", UserRole.CUSTOMER, true);

        UUID orderId = UUID.randomUUID();
        MockHttpServletRequestBuilder request = post("/orders/basket/place-order/{orderId}", orderId)
                .param("address", "")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/basket"))
                .andExpect(flash().attributeExists("noAddressSelectedMessage"));

        verify(userService, never()).getUserById(userId);
        verify(orderService, times(1)).validateAddressProvided("");
        verify(orderService, never()).updateOrderDetails(any(), any());
    }

    @Test
    void getUnauthenticatedRequestToBasketPlaceOrderPage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/orders/basket/place-order/{orderId}", UUID.randomUUID());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
        verify(orderService, never()).validateAddressProvided(any());
        verify(orderService, never()).validateOrderHasProducts(any());
    }

    @Test
    void getAuthenticatedRequestToAwaitingOrdersPage_thenReturnAwaitingOrdersView() throws Exception {

        Order order = Order.builder()
                .status(OrderStatus.CREATED)
                .build();

        when(userService.getUserById(any())).thenReturn(testCourier());
        when(orderService.getAllWaitingOrders()).thenReturn(new ArrayList<>(List.of(order)));

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "courier_user", "123456", "courier_user@gmail.com", "Courier", "User", UserRole.COURIER, true);

        MockHttpServletRequestBuilder request = get("/orders/awaiting")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("orders-list"))
                .andExpect(model().attributeExists("user", "allWaitingOrders"));

        verify(userService, times(1)).getUserById(userId);
        verify(orderService, times(1)).getAllWaitingOrders();
    }

    @Test
    void getUnauthenticatedRequestToAwaitingOrdersPage_thenRedirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/orders/awaiting");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));

        verify(userService, never()).getUserById(any());
        verify(orderService, never()).getAllWaitingOrders();
    }

    @Test
    void putAuthenticatedRequestToOrderAcceptPageHappyPath_thenRedirectToAwaitingOrdersPage() throws Exception {

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .status(OrderStatus.CREATED)
                .build();

        when(userService.getUserById(any())).thenReturn(testCourier());
        when(orderService.getOrderById(any())).thenReturn(order);
        doNothing().when(orderService).acceptOrder(order, testCourier());

        UUID userId = UUID.randomUUID();
        UserAuthDetails principal = new UserAuthDetails(userId, "courier_user", "123456", "courier_user@gmail.com", "Courier", "User", UserRole.COURIER, true);

        MockHttpServletRequestBuilder request = put("/orders/{orderId}/accept", order.getId())
                .with(user(principal))
                .with(csrf());;

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/awaiting"))
                .andExpect(model().attributeExists("order"));

        verify(userService, times(1)).getUserById(userId);
        verify(orderService, times(1)).getOrderById(order.getId());
        verify(orderService, times(1)).acceptOrder(any(), any());
    }

//    @Test
//    void putAuthenticatedRequestToOrderAcceptPageWhenOrderAlreadyPickedUp_thenThrowOrderAlreadyPickedUpException() throws Exception {
//
//        Order order = Order.builder()
//                .id(UUID.randomUUID())
//                .status(OrderStatus.COURIER_FOUND)
//                .responsibleCourier(new Courier())
//                .build();
//
//        when(userService.getUserById(any())).thenReturn(testCourier());
//        when(orderService.getOrderById(any())).thenReturn(order);
//        doThrow(new OrderAlreadyPickedUp("Another courier is responsible for this order!")).when(orderService).acceptOrder(order, testCourier());
//
//        UUID userId = UUID.randomUUID();
//        UserAuthDetails principal = new UserAuthDetails(userId, "courier_user", "123456", "courier_user@gmail.com", "Courier", "User", UserRole.COURIER, true);
//
//        MockHttpServletRequestBuilder request = put("/orders/{orderId}/accept", order.getId())
//                .with(user(principal))
//                .with(csrf());;
//
//        mockMvc.perform(request)
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/orders/awaiting"))
//                .andExpect(model().attributeExists("order"))
//                .andExpect(flash().attributeExists("orderAlreadyPickedUpMessage"));
//
//        verify(userService, times(1)).getUserById(userId);
//        verify(orderService, times(1)).getOrderById(order.getId());
//        verify(orderService, times(1)).acceptOrder(any(), any());
//    }
}
