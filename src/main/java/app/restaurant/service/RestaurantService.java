package app.restaurant.service;

import app.company.model.Company;
import app.company.service.CompanyService;
import app.exceptions.RestaurantNotFound;
import app.restaurant.model.Restaurant;
import app.restaurant.repository.RestaurantRepository;
import app.web.dto.CreateRestaurantRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CompanyService companyService;

    @Autowired
    public RestaurantService(RestaurantRepository restaurantRepository, CompanyService companyService) {
        this.restaurantRepository = restaurantRepository;
        this.companyService = companyService;
    }

    public Restaurant getRestaurantByBrandAndLocation(String brandName, String location) {
        Company company = companyService.getCompanyByName(brandName);

        return company.getRestaurants().stream().filter(r -> r.getAddress().equals(location))
                .findFirst().orElseThrow(() -> new RestaurantNotFound("Restaurant in %s not found.".formatted(location)));
    }

    public Restaurant getRestaurantById(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFound("Restaurant with id [%s] not found.".formatted(restaurantId)));
    }

    public void saveRestaurant(Restaurant restaurant) {
        restaurantRepository.save(restaurant);
    }

    public void addNewRestaurant(CreateRestaurantRequest createRestaurantRequest) {

        Company company = companyService.getCompanyByName(createRestaurantRequest.getBrandName());

        Restaurant restaurant = Restaurant.builder()
                .name(createRestaurantRequest.getName())
                .address(createRestaurantRequest.getAddress())
                .company(company)
                .build();

        company.getRestaurants().add(restaurant);

        restaurantRepository.save(restaurant);
        companyService.saveCompany(company);
    }
}
