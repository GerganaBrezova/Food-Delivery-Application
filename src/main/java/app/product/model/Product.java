package app.product.model;

import app.order.model.Order;
import app.restaurant.model.Restaurant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private ProductCategory category;

    @Column(nullable = false)
    private BigDecimal price;

    @ManyToMany(mappedBy = "products", fetch = FetchType.EAGER)
    private List<Order> orders = new ArrayList<>();

    @ManyToMany(mappedBy = "products", fetch = FetchType.EAGER)
    private List<Restaurant> restaurants = new ArrayList<>();
}
