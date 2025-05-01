package app.company.service;

import app.company.model.Company;
import app.company.repository.CompanyRepository;
import app.exceptions.CompanyNotFound;
import app.restaurant.model.Restaurant;
import app.web.dto.CreateCompanyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public Company getCompanyById(UUID brandId) {
        return companyRepository.findById(brandId).orElseThrow(() -> new CompanyNotFound("Company with id [%s] not found]".formatted(brandId)));
    }

    public void createCompany(CreateCompanyRequest createCompanyRequest) {

        Restaurant sofia = Restaurant.builder()
                .name(createCompanyRequest.getName())
                .address("Sofia")
                .build();

        Restaurant plovdiv = Restaurant.builder()
                .name(createCompanyRequest.getName())
                .address("Plovdiv")
                .build();

        List<Restaurant> restaurants = List.of(sofia, plovdiv);

        Company company = Company.builder()
                .name(createCompanyRequest.getName())
                .description(createCompanyRequest.getDescription())
                .logoUrl(createCompanyRequest.getLogoUrl())
                .restaurants(restaurants)
                .build();

        restaurants.forEach(r -> r.setCompany(company));

        companyRepository.save(company);
    }

    public Company getCompanyByName(String brandName) {
        return companyRepository.getCompanyByName(brandName).orElseThrow(() -> new CompanyNotFound("Brand %s not found.".formatted(brandName)));
    }

    public Map<UUID, List<Restaurant>> getRestaurantsGroupedByCompany() {
        List<Company> companies = getAllCompanies();
        Map<UUID, List<Restaurant>> restaurantsByCompany = new HashMap<>();

        for (Company company : companies) {
            restaurantsByCompany.put(company.getId(), company.getRestaurants());
        }

        return restaurantsByCompany;
    }
}
