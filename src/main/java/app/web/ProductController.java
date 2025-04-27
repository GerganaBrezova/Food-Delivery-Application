package app.web;

import app.product.model.Product;
import app.product.service.ProductService;
import app.security.UserAuthDetails;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final UserService userService;
    private final ProductService productService;

    @Autowired
    public ProductController(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }

    @GetMapping
    public ModelAndView getProductsPage(@AuthenticationPrincipal UserAuthDetails userDetails,
                                        @RequestParam(required = false) UUID brandId,
                                        @RequestParam(required = false) String location,
                                        @RequestParam(required = false) String categoryName) {

        User user = userService.getUserById(userDetails.getId());
        List<Product> categoryProducts = productService.getAllCategoryProducts(brandId, categoryName, location);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("products");
        modelAndView.addObject("user", user);
        modelAndView.addObject("categoryProducts", categoryProducts);

        modelAndView.addObject("brandId", brandId);
        modelAndView.addObject("location", location);
        modelAndView.addObject("categoryName", categoryName);

        return modelAndView;
    }
}
