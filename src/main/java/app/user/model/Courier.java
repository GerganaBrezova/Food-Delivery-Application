package app.user.model;

import app.order.model.Order;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Entity
public class Courier extends User {

    @Column(nullable = false)
    private BigDecimal generatedTurnover;

    @Column(nullable = false)
    private LocalDateTime hiredOn;

    @OneToMany(mappedBy = "courier", fetch = FetchType.EAGER)
    private List<Order> acceptedOrders = new ArrayList<>();

    @ManyToOne
    private Employee hiredBy;
}
