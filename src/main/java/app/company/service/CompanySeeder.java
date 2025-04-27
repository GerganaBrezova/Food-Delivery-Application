package app.company.service;

import app.company.model.Company;
import app.product.model.Product;
import app.product.model.ProductCategory;
import app.product.service.ProductService;
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
import java.util.List;

@Slf4j

@Component
public class CompanySeeder implements CommandLineRunner {

    private final CompanyService companyService;
    private final ObjectMapper objectMapper;
    private final ProductService productService;

    @Autowired
    public CompanySeeder(CompanyService companyService, ObjectMapper objectMapper, ProductService productService) {
        this.companyService = companyService;
        this.objectMapper = objectMapper;
        this.productService = productService;
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

            List<Product> products = productService.generateUniqueProductsForBrand(dto.getName());

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
}
