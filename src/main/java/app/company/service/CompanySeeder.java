package app.company.service;

import app.company.model.Company;
import app.product.model.Product;
import app.product.model.ProductCategory;
import app.restaurant.model.Restaurant;
import app.web.dto.CompanySeed;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j

@Component
public class CompanySeeder implements CommandLineRunner {

    private final CompanyService companyService;
    private final ObjectMapper objectMapper;

    @Autowired
    public CompanySeeder(CompanyService companyService, ObjectMapper objectMapper) {
        this.companyService = companyService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        if (companyService.allCompaniesCount() > 0) {
            return;
        }

        InputStream inputStream = new ClassPathResource("data/companies.json").getInputStream();
        List<CompanySeed> companies = objectMapper.readValue(inputStream, new TypeReference<>() {
        });

        for (CompanySeed dto : companies) {
            Company company = Company.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .logoUrl(dto.getLogoUrl())
                    .build();

            List<Product> products = generateUniqueProductsForBrand(dto.getName());

            Restaurant sofia = Restaurant.builder()
                    .name(dto.getName() + " Sofia")
                    .address("Sofia")
                    .products(products)
                    .company(company)
                    .build();

            Restaurant plovdiv = Restaurant.builder()
                    .name(dto.getName() + " Plovdiv")
                    .address("Plovdiv")
                    .products(products)
                    .company(company)
                    .build();

            company.setRestaurants(List.of(sofia, plovdiv));

            companyService.saveCompany(company);
        }

        log.info("Companies seeded successfully.");
    }

    private List<Product> generateUniqueProductsForBrand(String brandName) {

        return new ArrayList<>(List.of(
                createProduct(brandName + " Fries", ProductCategory.APPETIZER, "3.50"),
                createProduct(brandName + " Nuggets", ProductCategory.APPETIZER, "4.00"),
                createProduct(brandName + " Mozzarella Sticks", ProductCategory.APPETIZER, "4.20"),

                createProduct(brandName + " Burger", ProductCategory.MAIN, "6.50"),
                createProduct(brandName + " Wrap", ProductCategory.MAIN, "5.90"),
                createProduct(brandName + " Chicken Bowl", ProductCategory.MAIN, "6.20"),

                createProduct(brandName + " Brownie", ProductCategory.DESSERT, "3.00"),
                createProduct(brandName + " Donut", ProductCategory.DESSERT, "2.80"),
                createProduct(brandName + " Sundae", ProductCategory.DESSERT, "3.10"),

                createProduct(brandName + " Cola", ProductCategory.DRINK, "1.90"),
                createProduct(brandName + " Orange Juice", ProductCategory.DRINK, "2.10"),
                createProduct(brandName + "Milkshake", ProductCategory.DRINK, "2.90")
        ));
    }

    private Product createProduct(String name, ProductCategory category, String price) {
        return Product.builder()
                .name(name)
                .category(category)
                .price(new BigDecimal(price))
                .build();
    }
}
