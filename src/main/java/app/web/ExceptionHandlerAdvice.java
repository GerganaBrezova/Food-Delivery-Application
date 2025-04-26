package app.web;

import app.exceptions.EmailAlreadyExists;
import app.exceptions.PasswordsDoNotMatch;
import app.exceptions.UserNotFound;
import app.exceptions.UsernameAlreadyExists;
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
import java.util.Collections;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(UsernameAlreadyExists.class)
    public String handleUsernameAlreadyExists(RedirectAttributes redirectAttributes, UsernameAlreadyExists usernameException) {

        String message = usernameException.getMessage();
        redirectAttributes.addFlashAttribute("usernameAlreadyExistMessage", message);

        return "redirect:/register";
    }

    @ExceptionHandler(EmailAlreadyExists.class)
    public String handleEmailAlreadyExists(RedirectAttributes redirectAttributes, EmailAlreadyExists emailException) {

        String message = emailException.getMessage();
        redirectAttributes.addFlashAttribute("emailAlreadyExistMessage", message);

        return "redirect:/register";
    }

    @ExceptionHandler(PasswordsDoNotMatch.class)
    public String handlePasswordsDoNotMatch(RedirectAttributes redirectAttributes, PasswordsDoNotMatch passwordException) {

        String message = passwordException.getMessage();
        redirectAttributes.addFlashAttribute("passwordsDoNotMatchMessage", message);

        return "redirect:/register";
    }

    @ExceptionHandler(UserNotFound.class)
    public ModelAndView handleUserNotFoundException(UserNotFound userNotFoundException) {

        ModelAndView modelAndView = new ModelAndView("users");
        modelAndView.addObject("searchPerformed", true);
        modelAndView.addObject("userNotFoundMessage", userNotFoundException.getMessage());
        modelAndView.addObject("users", Collections.emptyList());

        return modelAndView;
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
