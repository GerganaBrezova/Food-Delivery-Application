package app.order.service;

import app.exceptions.NoAddressSelected;
import app.exceptions.OrderAlreadyPickedUp;
import app.exceptions.OrderHasNoProducts;
import app.exceptions.OrderNotFound;
import app.order.model.Order;
import app.order.model.OrderStatus;
import app.order.repository.OrderRepository;
import app.product.model.Product;
import app.user.model.Courier;
import app.user.model.User;
import app.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j

@Service
public class OrderService {

    private static final BigDecimal DELIVERY_FEE = new BigDecimal(5);
    private final OrderRepository orderRepository;
    private final UserService userService;

    @Autowired
    public OrderService(OrderRepository orderRepository, UserService userService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    public Order getOrCreateOrder(User user) {

        Optional<Order> orderOptional = orderRepository.findByCustomerAndStatus(user, OrderStatus.PENDING);

        if (orderOptional.isPresent()) {
            return orderOptional.get();
        } else {
            Order order = Order.builder()
                    .customer(user)
                    .status(OrderStatus.PENDING)
                    .createdOn(LocalDateTime.now())
                    .totalPrice(DELIVERY_FEE)
                    .build();

            return orderRepository.save(order);
        }
    }

    public void addProductToOrder(Order order, Product product) {

        order.getProducts().add(product);

        BigDecimal totalPrice = order.getTotalPrice().add(product.getPrice());

        order.setTotalPrice(totalPrice);

        orderRepository.save(order);
    }

    public void deleteProductFromOrder(Order order, Product product) {

        BigDecimal totalPrice = order.getTotalPrice().subtract(product.getPrice());
        order.setTotalPrice(totalPrice);

        order.getProducts().remove(product);

        orderRepository.save(order);
    }

    public Order getCurrentOrder(User user) {
        return orderRepository.findByCustomerAndStatus(user, OrderStatus.PENDING)
                .orElseThrow(() -> new OrderNotFound("No active order for user."));
    }

    public void updateOrderDetails(Order order, String address) {

        order.setStatus(OrderStatus.CREATED);
        order.setAddress(address);

        orderRepository.save(order);

        log.info("Order [%s] placed successfully.".formatted(order.getId()));
    }

    public List<Order> getAllUserOrders(User user) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getCustomer().getId().equals(user.getId()))
                .filter(order -> order.getStatus() != OrderStatus.PENDING)
                .sorted(Comparator.comparing(Order::getCreatedOn).reversed())
                .collect(Collectors.toList());
    }

    public void validateOrderHasProducts(Order order) {
        if (order.getProducts() == null || order.getProducts().isEmpty()) {
            throw new OrderHasNoProducts("Cannot place an order with no products.");
        }
    }

    public void validateAddressProvided(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new NoAddressSelected("Please enter an address!");
        }
    }

    public void saveAll(List<Order> orders) {
        orderRepository.saveAll(orders);
    }

    public List<Order> getAllWaitingOrders() {
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus().name().equals("CREATED"))
                .collect(Collectors.toList());
    }

    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound("Order [%s] not available.".formatted(orderId)));
    }

    public void acceptOrder(Order order, Courier courier) {

        if (order.getResponsibleCourier() != null) {
            throw new OrderAlreadyPickedUp("Another courier is responsible for this order!");
        }

        if (courier.getAcceptedOrder() != null) {
            throw new OrderAlreadyPickedUp("You already picked up an order!");
        }

        order.setResponsibleCourier(courier);
        order.setStatus(OrderStatus.COURIER_FOUND);

        orderRepository.save(order);
    }

    public void changeOrderStatus(Order order, Courier courier) {

        if (order.getStatus() == OrderStatus.COURIER_FOUND) {
            order.setStatus(OrderStatus.PICKED_UP);
        } else if (order.getStatus() == OrderStatus.PICKED_UP) {
            order.setStatus(OrderStatus.DELIVERED);
            courier.setGeneratedTurnover(courier.getGeneratedTurnover().add(order.getTotalPrice()));
            checkForBonus(courier);
            courier.getCompletedOrders().add(order);
            userService.saveUser(courier);
        }

        orderRepository.save(order);
    }

    private static void checkForBonus(Courier courier) {

        BigDecimal BONUS_THRESHOLD = new BigDecimal(500);
        BigDecimal BONUS_AMOUNT = new BigDecimal(100);

        BigDecimal turnover = courier.getGeneratedTurnover();
        BigDecimal bonus = courier.getBonuses();

        int fullThresholdsCrossed = turnover.divide(BONUS_THRESHOLD, RoundingMode.DOWN).intValue();
        int bonusesGiven = bonus.divide(BONUS_AMOUNT, RoundingMode.DOWN).intValue();

        if (fullThresholdsCrossed > bonusesGiven) {
            int bonusesToAdd = fullThresholdsCrossed - bonusesGiven;
            BigDecimal bonusToAdd = BONUS_AMOUNT.multiply(BigDecimal.valueOf(bonusesToAdd));
            courier.setBonuses(bonus.add(bonusToAdd));
        }
    }

    public void deleteCourierAcceptedOrder(Courier courier) {
        courier.setAcceptedOrder(null);

        userService.saveUser(courier);
    }
}
