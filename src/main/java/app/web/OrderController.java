package app.web;

import app.order.model.Order;
import app.order.service.OrderService;
import app.product.model.Product;
import app.product.service.ProductService;
import app.security.UserAuthDetails;
import app.user.model.Courier;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;

    @Autowired
    public OrderController(UserService userService, OrderService orderService, ProductService productService) {
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ModelAndView getOrdersPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        User user = userService.getUserById(userAuthDetails.getId());
        List<Order> userOrders = orderService.getAllUserOrders(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("orders");
        modelAndView.addObject("user", user);
        modelAndView.addObject("userOrders", userOrders);

        return modelAndView;
    }

    @GetMapping("/courier")
    @PreAuthorize("hasRole('COURIER')")
    public ModelAndView getCourierOrdersPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        Courier user = (Courier) userService.getUserById(userAuthDetails.getId());
        List<Order> completedOrders = user.getCompletedOrders();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("courier-completed-orders");
        modelAndView.addObject("user", user);
        modelAndView.addObject("completedOrders", completedOrders);

        return modelAndView;
    }

    @GetMapping("/basket")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ModelAndView getBasketPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        User user = userService.getUserById(userAuthDetails.getId());
        Order order = orderService.getOrCreateOrder(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("basket");
        modelAndView.addObject("user", user);
        modelAndView.addObject("order", order);

        return modelAndView;
    }

    @PostMapping("/basket/add-product/{productId}")
    public String addProductToOrder(@AuthenticationPrincipal UserAuthDetails userAuthDetails, @PathVariable UUID productId) {

        User user = userService.getUserById(userAuthDetails.getId());

        Product product = productService.getProductById(productId);

        Order order = orderService.getOrCreateOrder(user);

        orderService.addProductToOrder(order, product);

        return "redirect:/orders/basket";

    }

    @DeleteMapping("/basket/delete-product/{productId}")
    public String deleteProductFromOrder(@AuthenticationPrincipal UserAuthDetails userAuthDetails, @PathVariable UUID productId) {

        User user = userService.getUserById(userAuthDetails.getId());

        Product product = productService.getProductById(productId);

        Order order = orderService.getCurrentOrder(user);

        orderService.deleteProductFromOrder(order, product);

        return "redirect:/orders/basket";

    }

    @GetMapping("/success")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ModelAndView getSuccessPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        User user = userService.getUserById(userAuthDetails.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("order-success");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping("/basket/place-order/{orderId}")
    public String placeOrder(@AuthenticationPrincipal UserAuthDetails userAuthDetails, @PathVariable UUID orderId, @RequestParam String address) {

        orderService.validateAddressProvided(address);

        User user = userService.getUserById(userAuthDetails.getId());
        Order order = orderService.getCurrentOrder(user);

        orderService.validateOrderHasProducts(order);
        orderService.updateOrderDetails(order, address);

        return "redirect:/orders/success";
    }

    @GetMapping("/awaiting")
    @PreAuthorize("hasRole('COURIER')")
    public ModelAndView getAwaitingOrdersPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        User user = userService.getUserById(userAuthDetails.getId());
        List<Order> allWaitingOrders = orderService.getAllWaitingOrders();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("orders-list");
        modelAndView.addObject("user", user);
        modelAndView.addObject("allWaitingOrders", allWaitingOrders);

        return modelAndView;
    }

    @PutMapping("/{orderId}/accept")
    public ModelAndView acceptOrder(@AuthenticationPrincipal UserAuthDetails userAuthDetails, @PathVariable UUID orderId) {

        Courier courier = (Courier) userService.getUserById(userAuthDetails.getId());
        Order order = orderService.getOrderById(orderId);

        orderService.acceptOrder(order, courier);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("order", order);
        modelAndView.setViewName("redirect:/orders/awaiting");

        return modelAndView;
    }

    @GetMapping("/details")
    @PreAuthorize("hasRole('COURIER')")
    public ModelAndView getOrderDetailsPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        Courier user = (Courier) userService.getUserById(userAuthDetails.getId());
        Order order = user.getAcceptedOrder();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("order-accepted");
        modelAndView.addObject("user", user);
        modelAndView.addObject("order", order);

        return modelAndView;
    }

    @PutMapping("/change-status")
    public ModelAndView changeOrderStatus(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        Courier user = (Courier) userService.getUserById(userAuthDetails.getId());
        Order order = user.getAcceptedOrder();

        orderService.changeOrderStatus(order, user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("order-accepted");
        modelAndView.addObject("user", user);
        modelAndView.addObject("order", order);

        return modelAndView;
    }

    @DeleteMapping("/clean")
    public String cleanOrder(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        Courier user = (Courier) userService.getUserById(userAuthDetails.getId());

        orderService.deleteCourierAcceptedOrder(user);

        return "redirect:/orders/details";
    }

}
