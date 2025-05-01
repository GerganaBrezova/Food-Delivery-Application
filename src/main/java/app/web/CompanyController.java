package app.web;

import app.company.service.CompanyService;
import app.security.UserAuthDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CreateCompanyRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/company")
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

}
