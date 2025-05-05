package app.web;

import app.company.model.Company;
import app.order.model.Order;
import app.restaurant.model.Restaurant;
import app.user.model.*;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@UtilityClass
public class TestBuilder {

    public static User testEmployee() {

        Employee employee = Employee.builder()
                .id(UUID.randomUUID())
                .username("employee_user")
                .email("employee_user@gmail.com")
                .firstName("Employee")
                .lastName("User")
                .createdOn(LocalDateTime.now())
                .role(UserRole.EMPLOYEE)
                .isActive(true)
                .hiredCouriers(new HashSet<>())
                .build();

       return employee;
    }

    public static List<Courier> testCouriersList() {

        Order firstOrder = Order.builder()
                .createdOn(LocalDateTime.of(2024, 11, 4, 12, 33))
                .build();

        Order secondOrder = Order.builder()
                .createdOn(LocalDateTime.of(2024, 11, 5, 12, 33))
                .build();

        Order thirdOrder = Order.builder()
                .createdOn(LocalDateTime.of(2024, 11, 6, 12, 33))
                .build();

        Order forthOrder = Order.builder()
                .createdOn(LocalDateTime.of(2024, 11, 7, 12, 33))
                .build();

        Courier firstCourier = Courier.builder()
                .generatedTurnover(BigDecimal.TEN)
                .completedOrders(List.of(firstOrder, secondOrder))
                .build();

        Courier secondCourier = Courier.builder()
                .generatedTurnover(BigDecimal.ONE)
                .completedOrders(List.of(thirdOrder, forthOrder))
                .build();

        return new ArrayList<>(List.of(firstCourier, secondCourier));
    }

    public static List<Customer> testCustomersList() {

        Customer firstCustomer = Customer.builder().build();

        Customer secondCustomer = Customer.builder().build();

        return new ArrayList<>(List.of(firstCustomer, secondCustomer));
    }

    public static List<Company> testCompaniesList() {

        Company firstCompany = Company.builder().build();

        Company secondCompany = Company.builder().build();

        return new ArrayList<>(List.of(firstCompany, secondCompany));
    }

    public static Map<UUID, List<Restaurant>> testRestaurantsMap() {

        Map<UUID, List<Restaurant>> restaurantsMap = new HashMap<>();

        restaurantsMap.put(UUID.randomUUID(), List.of(Restaurant.builder().build()));

        restaurantsMap.put(UUID.randomUUID(), List.of(Restaurant.builder().build()));

        return restaurantsMap;
    }
}
