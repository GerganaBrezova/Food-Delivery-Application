package app.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@Entity
public class Employee extends User {

    @OneToMany(mappedBy = "hiredBy")
    private Set<Courier> hiredCouriers;
}
