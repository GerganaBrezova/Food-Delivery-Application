package app.order.repository;

import app.order.model.Order;
import app.order.model.OrderStatus;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByCustomerAndStatus(User user, OrderStatus orderStatus);
}
