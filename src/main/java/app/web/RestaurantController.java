package app.web;

import app.product.model.ProductCategory;
import app.security.UserAuthDetails;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    private final UserService userService;

    @Autowired
    public RestaurantController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/categories/{brandId}/location/{location}")
    public ModelAndView showCategoriesPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails, @PathVariable UUID brandId, @PathVariable String location) {

        User user = userService.getUserById(userAuthDetails.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("categories");

        ProductCategory[] categories = ProductCategory.values();
        modelAndView.addObject("categories", categories);
        modelAndView.addObject("brandId", brandId);
        modelAndView.addObject("location", location);
        modelAndView.addObject("user", user);

        return modelAndView;
    }
}
