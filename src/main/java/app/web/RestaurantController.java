package app.web;

import app.product.model.ProductCategory;
import app.restaurant.service.RestaurantService;
import app.security.UserAuthDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CreateRestaurantRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    private final UserService userService;
    private final RestaurantService restaurantService;

    @Autowired
    public RestaurantController(UserService userService, RestaurantService restaurantService) {
        this.userService = userService;
        this.restaurantService = restaurantService;
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

    @GetMapping("/add")
    public ModelAndView getRestaurantCreationPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        User user = userService.getUserById(userAuthDetails.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("restaurant-creation");
        modelAndView.addObject("user", user);
        modelAndView.addObject("createRestaurantRequest", new CreateRestaurantRequest());

        return modelAndView;
    }

    @PostMapping("/add")
    public ModelAndView addNewRestaurant(@AuthenticationPrincipal UserAuthDetails userAuthDetails,
                                      @Valid CreateRestaurantRequest createRestaurantRequest,
                                      BindingResult bindingResult) {

        User user = userService.getUserById(userAuthDetails.getId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("restaurant-creation");
            modelAndView.addObject("user", user);
            return modelAndView;
        }

        restaurantService.addNewRestaurant(createRestaurantRequest);

        return new ModelAndView("redirect:/home");
    }
}
