package app.company.service;

import app.company.model.Company;
import app.company.repository.CompanyRepository;
import app.product.model.Product;
import app.product.model.ProductCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public int allCompaniesCount() {
        return companyRepository.findAll().size();
    }

    public void saveCompany(Company company) {
        companyRepository.save(company);
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public List<Product> generateUniqueProductsForBrand(String brandName) {

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
