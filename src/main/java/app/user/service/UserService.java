package app.user.service;

import app.exceptions.*;
import app.security.UserAuthDetails;
import app.user.model.Customer;
import app.user.model.User;
import app.user.model.UserRole;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
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

        Customer customer = Customer.builder()
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
}
