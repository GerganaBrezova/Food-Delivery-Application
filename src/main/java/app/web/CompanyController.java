package app.web;

import app.company.model.Company;
import app.company.service.CompanyService;
import app.security.UserAuthDetails;
import app.user.model.Courier;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CreateCompanyRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/company")
@PreAuthorize("hasRole('EMPLOYEE')")
public class CompanyController {

    private final UserService userService;
    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService, UserService userService) {
        this.companyService = companyService;
        this.userService = userService;
    }

    @GetMapping("/add")
    public ModelAndView getBrandCreationPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        User user = userService.getUserById(userAuthDetails.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("brand-creation");
        modelAndView.addObject("user", user);
        modelAndView.addObject("createCompanyRequest", new CreateCompanyRequest());

        return modelAndView;
    }

    @PostMapping("/add")
    public ModelAndView addNewCompany(@AuthenticationPrincipal UserAuthDetails userAuthDetails, @Valid CreateCompanyRequest createCompanyRequest, BindingResult bindingResult) {

        User user = userService.getUserById(userAuthDetails.getId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("brand-creation");
            modelAndView.addObject("user", user);
            return modelAndView;
        }

        companyService.createCompany(createCompanyRequest);

        return new ModelAndView("redirect:/home");
    }

    @DeleteMapping("/{id}/delete")
    public String deleteCompany(@PathVariable UUID id) {

        Company company = companyService.getCompanyById(id);

        companyService.deleteCompany(company);

        return "redirect:/home";
    }

    @GetMapping("/statistics")
    public ModelAndView getCompanyStatisticsPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        User user = userService.getUserById(userAuthDetails.getId());
        List<Courier> allSystemCouriers = userService.getAllCouriers();
        BigDecimal revenue = userService.getRevenueFromCouriers(allSystemCouriers);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("statistics");
        modelAndView.addObject("user", user);
        modelAndView.addObject("allSystemCouriers", allSystemCouriers);
        modelAndView.addObject("revenue", revenue);

        return modelAndView;
    }

    @GetMapping("/statistics/filter")
    public ModelAndView getFilteredStatisticsPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails,
                                                  @RequestParam(required = false) LocalDateTime from,
                                                  @RequestParam(required = false) LocalDateTime to) {

        User user = userService.getUserById(userAuthDetails.getId());

        List<Courier> filteredCouriers = (from != null && to != null)
                ? userService.getCouriersByOrderCompletionDate(from, to)
                : userService.getAllCouriers();

        BigDecimal totalRevenue = companyService.getTotalRevenue(filteredCouriers);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("statistics");
        modelAndView.addObject("user", user);
        modelAndView.addObject("allSystemCouriers", filteredCouriers);
        modelAndView.addObject("revenue", totalRevenue);

        return modelAndView;
    }
}
