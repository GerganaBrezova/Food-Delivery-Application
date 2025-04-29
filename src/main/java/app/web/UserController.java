package app.web;

import app.security.UserAuthDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.EditProfileRequest;
import app.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}/profile")
    public ModelAndView getUserProfilePage(@PathVariable UUID id) {

        User user = userService.getUserById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");
        modelAndView.addObject("user", user);
        modelAndView.addObject("editProfileRequest", DtoMapper.mapToEditProfileRequest(user));

        return modelAndView;
    }

    @PutMapping("/{id}/profile")
    public ModelAndView editUserProfile(@PathVariable UUID id, @Valid EditProfileRequest editProfileRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            User user = userService.getUserById(id);

            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("profile");
            modelAndView.addObject("user", user);
            modelAndView.addObject("editProfileRequest", editProfileRequest);

            return modelAndView;
        }

        userService.editUserDetails(editProfileRequest, id);

        return new ModelAndView("redirect:/home");
    }
}
