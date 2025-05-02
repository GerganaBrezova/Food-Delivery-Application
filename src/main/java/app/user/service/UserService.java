package app.user.service;

import app.exceptions.*;
import app.security.UserAuthDetails;
import app.user.model.*;
import app.user.repository.CourierRepository;
import app.user.repository.CustomerRepository;
import app.user.repository.UserRepository;
import app.web.dto.EditProfileRequest;
import app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CourierRepository courierRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, CourierRepository courierRepository, CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.courierRepository = courierRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User %s was not found.".formatted(username)));

        return new UserAuthDetails(user.getId(), username, user.getPassword(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole(), user.isActive());
    }

    @Transactional
    public void register(RegisterRequest registerRequest) {

        Optional<User> optionalUserByUsername = userRepository.findByUsername(registerRequest.getUsername());
        Optional<User> optionalUserByEmail = userRepository.findByEmail(registerRequest.getEmail());

        if (optionalUserByUsername.isPresent()) {
            throw new UsernameAlreadyExists("Username %s already exists.".formatted(registerRequest.getUsername()));
        }

        if (optionalUserByEmail.isPresent()) {
            throw new EmailAlreadyExists("Email %s already exists.".formatted(registerRequest.getEmail()));
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new PasswordsDoNotMatch("Passwords do not match.");
        }

        User customer = Customer.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .createdOn(LocalDateTime.now())
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();

        userRepository.save(customer);

        log.info("Successfully created account for username %s with id %s.".formatted(customer.getUsername(), customer.getId()));
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFound("User with id [%s] was not found.".formatted(id)));
    }

    public void editUserDetails(EditProfileRequest editProfileRequest, UUID id) {

        User user = getUserById(id);

        if (!user.getUsername().equals(editProfileRequest.getUsername()) &&
                userRepository.existsByUsername(editProfileRequest.getUsername())) {
            throw new UsernameAlreadyExists("Username %s already exists.".formatted(editProfileRequest.getUsername()));
        }

        if (!user.getEmail().equals(editProfileRequest.getEmail()) &&
                userRepository.existsByEmail(editProfileRequest.getEmail())) {
            throw new EmailAlreadyExists("Email %s already exists.".formatted(editProfileRequest.getEmail()));
        }

        user.setFirstName(editProfileRequest.getFirstName());
        user.setLastName(editProfileRequest.getLastName());
        user.setEmail(editProfileRequest.getEmail());
        user.setUsername(editProfileRequest.getUsername());

        userRepository.save(user);
    }

    public List<Courier> getAllCouriers() {
        return courierRepository.findAll();
    }

    public void promoteCustomerToCourier(UUID customerId, UUID employeeId) {

        Customer customer = (Customer) userRepository.findById(customerId)
                .orElseThrow(() -> new UserNotFound("Customer not found"));

        userRepository.delete(customer);

        Employee employee = (Employee) userRepository.findById(employeeId)
                .orElseThrow(() -> new UserNotFound("Employee not found"));

        Courier courier = Courier.builder()
                .username(customer.getUsername())
                .email(customer.getEmail())
                .password(customer.getPassword())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .createdOn(customer.getCreatedOn())
                .role(UserRole.COURIER)
                .isActive(true)
                .generatedTurnover(BigDecimal.ZERO)
                .hiredOn(LocalDateTime.now())
                .hiredBy(employee)
                .build();

        userRepository.save(courier);
    }

    public List<Customer> getAllCustomers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == UserRole.CUSTOMER)
                .map(user -> (Customer) user)
                .collect(Collectors.toList());
    }

    public BigDecimal getRevenueFromCouriers(List<Courier> allSystemCouriers) {
        return allSystemCouriers.stream()
                .map(Courier::getGeneratedTurnover)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Courier> getCouriersByOrderCompletionDate(LocalDateTime from, LocalDateTime to) {
        List<Courier> allCouriers = courierRepository.findAll();

        return allCouriers.stream().filter(courier -> courier.getCompletedOrders().stream()
                .anyMatch(order -> !order.getCreatedOn().isBefore(from) && !order.getCreatedOn().isAfter(to)))
                .collect(Collectors.toList());
    }
}
