package app.web;

import app.security.UserAuthDetails;
import app.user.model.Customer;
import app.user.model.User;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeController {

    private final UserService userService;

    @Autowired
    public EmployeeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/customers")
    public ModelAndView getCustomersPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        User user = userService.getUserById(userAuthDetails.getId());
        List<Customer> allSystemCustomers = userService.getAllCustomers();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("customers");
        modelAndView.addObject("user", user);
        modelAndView.addObject("allSystemCustomers", allSystemCustomers);

        return modelAndView;
    }

    @PostMapping("/customers/{id}/promote")
    public String promoteCustomerToCourier(@AuthenticationPrincipal UserAuthDetails userAuthDetails, @PathVariable UUID id) {

        User employee = userService.getUserById(userAuthDetails.getId());

        userService.promoteCustomerToCourier(id, employee.getId());

        return "redirect:/home";
    }
}
