package app.order.model;

import app.product.model.Product;
import app.user.model.Courier;
import app.user.model.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @ManyToOne
    private Customer customer;

    @ManyToOne
    private Courier courier;

    @ManyToMany(fetch = FetchType.EAGER)
    List<Product> products = new ArrayList<>();
}
