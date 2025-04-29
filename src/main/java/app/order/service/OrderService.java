package app.order.service;

import app.exceptions.NoAddressSelected;
import app.exceptions.OrderHasNoProducts;
import app.exceptions.OrderNotFound;
import app.order.model.Order;
import app.order.model.OrderStatus;
import app.order.repository.OrderRepository;
import app.product.model.Product;
import app.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
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
                    .totalPrice(BigDecimal.ZERO)
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
}
