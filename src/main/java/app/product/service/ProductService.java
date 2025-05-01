package app.product.service;

import app.company.model.Company;
import app.company.service.CompanyService;
import app.exceptions.ProductNotFound;
import app.exceptions.RestaurantNotFound;
import app.order.service.OrderService;
import app.product.model.Product;
import app.product.model.ProductCategory;
import app.product.repository.ProductRepository;
import app.restaurant.model.Restaurant;
import app.restaurant.service.RestaurantService;
import app.web.dto.CreateProductRequest;
import app.web.dto.EditProductRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j

@Service
public class ProductService {

    private final CompanyService companyService;
    private final ProductRepository productRepository;
    private final RestaurantService restaurantService;
    private final OrderService orderService;

    @Autowired
    public ProductService(CompanyService companyService, ProductRepository productRepository, RestaurantService restaurantService, OrderService orderService) {
        this.companyService = companyService;
        this.productRepository = productRepository;
        this.restaurantService = restaurantService;
        this.orderService = orderService;
    }

    public List<Product> generateUniqueProductsForBrand(String brandName) {

        return new ArrayList<>(List.of(
                createProduct(brandName + " Fries", ProductCategory.APPETIZER, "3.50", "/images/fries.jpg"),
                createProduct(brandName + " Nuggets", ProductCategory.APPETIZER, "4.00", "/images/nuggets.jpg"),
                createProduct(brandName + " Mozzarella Sticks", ProductCategory.APPETIZER, "4.20", "/images/mozzarella-sticks.jpg"),

                createProduct(brandName + " Burger", ProductCategory.MAIN, "6.50", "/images/burger.jpg"),
                createProduct(brandName + " Wrap", ProductCategory.MAIN, "5.90", "/images/wrap.jpg"),
                createProduct(brandName + " Chicken Bowl", ProductCategory.MAIN, "6.20", "/images/chicken-bowl.jpg"),

                createProduct(brandName + " Brownie", ProductCategory.DESSERT, "3.00", "/images/brownie.jpg"),
                createProduct(brandName + " Donut", ProductCategory.DESSERT, "2.80", "/images/donut.jpg"),
                createProduct(brandName + " Sundae", ProductCategory.DESSERT, "3.10", "/images/sundae.jpg"),

                createProduct(brandName + " Cola", ProductCategory.DRINK, "1.90", "/images/cola.jpg"),
                createProduct(brandName + " Orange Juice", ProductCategory.DRINK, "2.10", "/images/orange-juice.jpg"),
                createProduct(brandName + " Milk Shake", ProductCategory.DRINK, "2.90", "/images/milk-shake.jpg")
        ));
    }

    private Product createProduct(String name, ProductCategory category, String price, String imageUrl) {
        return Product.builder()
                .name(name)
                .category(category)
                .price(new BigDecimal(price))
                .imageUrl(imageUrl)
                .build();
    }

    public List<Product> getAllCategoryProducts(UUID brandId, String categoryName, String location) {

        Company company = companyService.getCompanyById(brandId);
        Restaurant restaurant = company.getRestaurants().stream()
                .filter(r -> r.getAddress().equals(location))
                .findFirst().orElseThrow(() -> new RestaurantNotFound("Restaurant %s in %s not found.".formatted(company, location)));

        return restaurant.getProducts().stream()
                .filter(p -> p.getCategory().name().equals(categoryName))
                .collect(Collectors.toList());
    }

    public Product getProductById(UUID productId) {
        return productRepository.findById(productId).orElseThrow(() -> new ProductNotFound("Product with id [%s] not found.".formatted(productId)));
    }

    public void addNewProduct(CreateProductRequest createProductRequest) {

        Restaurant restaurant = restaurantService.getRestaurantByBrandAndLocation(createProductRequest.getBrandName(), createProductRequest.getRestaurantAddress());

        Product product = Product.builder()
                .name(createProductRequest.getName())
                .category(createProductRequest.getCategory())
                .price(createProductRequest.getPrice())
                .imageUrl(createProductRequest.getImageUrl())
                .build();

        restaurant.getProducts().add(product);

        productRepository.save(product);
        restaurantService.saveRestaurant(restaurant);
    }

    public void editProduct(EditProductRequest editProductRequest, UUID productId) {

        Product product = getProductById(productId);

        product.setName(editProductRequest.getName());
        product.setCategory(editProductRequest.getCategory());
        product.setPrice(editProductRequest.getPrice());
        product.setImageUrl(editProductRequest.getImageUrl());

        productRepository.save(product);
    }

    public void deleteProduct(Product product) {

        product.getRestaurants().forEach(restaurant -> restaurant.getProducts().remove(product));
        product.getOrders().forEach(order -> order.getProducts().remove(product));

        restaurantService.saveAll(product.getRestaurants());
        orderService.saveAll(product.getOrders());

        productRepository.delete(product);
    }
}

