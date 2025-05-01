package app.web;

import app.product.model.Product;
import app.product.service.ProductService;
import app.security.UserAuthDetails;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.CreateOrEditProductRequest;
import app.web.mapper.DtoMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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

        return modelAndView;
    }

    @GetMapping("/add")
    public ModelAndView getProductCreationPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails) {

        User user = userService.getUserById(userAuthDetails.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("product-creation");
        modelAndView.addObject("user", user);
        modelAndView.addObject("createOrEditProductRequest", new CreateOrEditProductRequest());

        return modelAndView;
    }

    @PostMapping("/add")
    public ModelAndView addNewProduct(@AuthenticationPrincipal UserAuthDetails userAuthDetails,
                                      @Valid CreateOrEditProductRequest createOrEditProductRequest,
                                      BindingResult bindingResult) {

        User user = userService.getUserById(userAuthDetails.getId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("product-creation");
            modelAndView.addObject("user", user);
            return modelAndView;
        }

        productService.addNewProduct(createOrEditProductRequest);

        return new ModelAndView("redirect:/home");
    }

    @GetMapping("/{id}/edit")
    public ModelAndView getProductEditPage(@AuthenticationPrincipal UserAuthDetails userAuthDetails, @PathVariable UUID id) {

        User user = userService.getUserById(userAuthDetails.getId());
        Product product = productService.getProductById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("edit-product");
        modelAndView.addObject("user", user);
        modelAndView.addObject("product", product);
        modelAndView.addObject("createOrEditProductRequest", DtoMapper.mapToCreateOrEditProductRequest(product));

        return modelAndView;
    }
}
