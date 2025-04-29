package app.web;

import app.order.model.Order;
import app.order.service.OrderService;
import app.product.model.Product;
import app.product.service.ProductService;
import app.security.UserAuthDetails;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ModelAndView getOrdersPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        User user = userService.getUserById(userAuthDetails.getId());
        List<Order> userOrders = orderService.getAllUserOrders(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("orders");
        modelAndView.addObject("user", user);
        modelAndView.addObject("userOrders", userOrders);

        return modelAndView;
    }

    @GetMapping("/basket")
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
}
