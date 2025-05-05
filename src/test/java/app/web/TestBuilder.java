package app.web;

import app.order.model.Order;
import app.user.model.*;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

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
}
