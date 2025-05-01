package app.web;

import app.company.model.Company;
import app.company.service.CompanyService;
import app.restaurant.model.Restaurant;
import app.security.UserAuthDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class IndexController {

    private final UserService userService;
    private final CompanyService companyService;

    @Autowired
    public IndexController(UserService userService, CompanyService companyService) {
        this.userService = userService;
        this.companyService = companyService;
    }

    @GetMapping("/")
    public String getIndexPage() {
        return "welcome";
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }

    @PostMapping("/register")
    public String registerUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        userService.register(registerRequest);

        return "redirect:/login";

    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParameter) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", new LoginRequest());

        if (errorParameter != null) {
            modelAndView.addObject("errorMessage", "Incorrect username or password.");
        }

        return modelAndView;
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        User user = userService.getUserById(userAuthDetails.getId());
        List<Company> allCompanies = companyService.getAllCompanies();
        Map<UUID, List<Restaurant>> restaurantsByCompany = companyService.getRestaurantsGroupedByCompany();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        modelAndView.addObject("user", user);
        modelAndView.addObject("allCompanies", allCompanies);
        modelAndView.addObject("restaurantsByCompany", restaurantsByCompany);

        return modelAndView;
    }
}
