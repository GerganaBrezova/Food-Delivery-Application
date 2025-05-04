package app.web;

import app.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(OrderAlreadyPickedUp.class)
    public String handleOrderAlreadyPickedUp(RedirectAttributes redirectAttributes, OrderAlreadyPickedUp orderAlreadyPickedUp) {

        String message = orderAlreadyPickedUp.getMessage();
        redirectAttributes.addFlashAttribute("orderAlreadyPickedUpMessage", message);

        return "redirect:/orders/awaiting";
    }

    @ExceptionHandler(CompanyNotFound.class)
    public String handleCompanyNotFound(RedirectAttributes redirectAttributes,
                                        CompanyNotFound companyNotFound,
                                        HttpServletRequest request){

        String message = companyNotFound.getMessage();
        redirectAttributes.addFlashAttribute("noCompanyFoundMessage", message);

        String referer = request.getHeader("Referer");

        return (referer != null && referer.contains("/restaurant/add")) ? "redirect:/restaurant/add" : "redirect:/products/add";
    }

    @ExceptionHandler(RestaurantNotFound.class)
    public String handleRestaurantNotFound(RedirectAttributes redirectAttributes, RestaurantNotFound restaurantNotFound) {

        String message = restaurantNotFound.getMessage();
        redirectAttributes.addFlashAttribute("noRestaurantFoundMessage", message);

        return "redirect:/products/add";
    }

    @ExceptionHandler(NoAddressSelected.class)
    public String handleNoAddressSelected(RedirectAttributes redirectAttributes, NoAddressSelected noAddressSelected) {

        String message = noAddressSelected.getMessage();
        redirectAttributes.addFlashAttribute("noAddressSelectedMessage", message);

        return "redirect:/orders/basket";
    }

    @ExceptionHandler(OrderHasNoProducts.class)
    public String handleOrderHasNoProducts(RedirectAttributes redirectAttributes, OrderHasNoProducts orderHasNoProducts) {

        String message = orderHasNoProducts.getMessage();
        redirectAttributes.addFlashAttribute("orderHasNoProductsMessage", message);

        return "redirect:/orders/basket";
    }

    @ExceptionHandler(UsernameAlreadyExists.class)
    public String handleUsernameAlreadyExists(RedirectAttributes redirectAttributes,
                                              UsernameAlreadyExists usernameException,
                                              HttpServletRequest request) {

        String message = usernameException.getMessage();
        redirectAttributes.addFlashAttribute("usernameAlreadyExistMessage", message);

        String referer = request.getHeader("Referer");
        return referer != null ? "redirect:" + referer : "redirect:/register";
    }

    @ExceptionHandler(EmailAlreadyExists.class)
    public String handleEmailAlreadyExists(RedirectAttributes redirectAttributes,
                                           EmailAlreadyExists emailException,
                                           HttpServletRequest request) {

        String message = emailException.getMessage();
        redirectAttributes.addFlashAttribute("emailAlreadyExistMessage", message);

        String referer = request.getHeader("Referer");
        return referer != null ? "redirect:" + referer : "redirect:/register";
    }

    @ExceptionHandler(PasswordsDoNotMatch.class)
    public String handlePasswordsDoNotMatch(RedirectAttributes redirectAttributes, PasswordsDoNotMatch passwordException) {

        String message = passwordException.getMessage();
        redirectAttributes.addFlashAttribute("passwordsDoNotMatchMessage", message);

        return "redirect:/register";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class,
            NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class
    })
    public ModelAndView handleNotFoundExceptions() {

        return new ModelAndView("not-found");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception exception) {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("server-error");
        modelAndView.addObject("errorMessage", exception.getClass().getSimpleName());

        return modelAndView;
    }
}
