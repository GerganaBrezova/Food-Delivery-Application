package app.web;

import app.company.model.Company;
import app.order.model.Order;
import app.order.model.OrderStatus;
import app.product.model.Product;
import app.product.model.ProductCategory;
import app.restaurant.model.Restaurant;
import app.user.model.*;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@UtilityClass
public class TestBuilder {

    public static Employee testEmployee() {

        return Employee.builder()
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
    }

    public static Customer testCustomer() {

        return Customer.builder()
                .id(UUID.randomUUID())
                .username("customer_user")
                .email("customer_user@gmail.com")
                .firstName("Customer")
                .lastName("User")
                .createdOn(LocalDateTime.now())
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .orders(new ArrayList<>(List.of(new Order())))
                .build();
    }

    public static Courier testCourier() {

        Order order = Order.builder()
                .status(OrderStatus.COURIER_FOUND)
                .build();

        return Courier.builder()
                .id(UUID.randomUUID())
                .username("courier_user")
                .email("courier_user@gmail.com")
                .firstName("Courier")
                .lastName("User")
                .createdOn(LocalDateTime.now())
                .role(UserRole.COURIER)
                .isActive(true)
                .generatedTurnover(BigDecimal.valueOf(100))
                .hiredOn(LocalDateTime.now())
                .acceptedOrder(order)
                .completedOrders(new ArrayList<>(List.of(new Order())))
                .hiredBy(testEmployee())
                .build();
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

    public static List<Product> testProductList() {

        Product firstProduct = Product.builder()
                .id(UUID.randomUUID())
                .category(ProductCategory.DESSERT)
                .restaurants(List.of(Restaurant.builder().address("Sofia").build()))
                .build();

        Product secondProduct = Product.builder()
                .id(UUID.randomUUID())
                .category(ProductCategory.DESSERT)
                .restaurants(List.of(Restaurant.builder().address("Sofia").build()))
                .build();

        return new ArrayList<>(List.of(firstProduct, secondProduct));
    }

    public static Company testCompany() {

        return Company.builder()
                .id(UUID.randomUUID())
                .name("Company Name")
                .build();
    }
}
