package app.order.service;

import app.exceptions.OrderNotFound;
import app.order.model.Order;
import app.order.model.OrderStatus;
import app.order.repository.OrderRepository;
import app.product.model.Product;
import app.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

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

}
