package app.user;

import app.exceptions.*;
import app.security.UserAuthDetails;
import app.user.model.*;
import app.user.repository.CourierRepository;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.EditProfileRequest;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static app.TestBuilder.testCustomer;
import static app.TestBuilder.testEmployee;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {


    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private UserService userService;

    //Get User By ID
    @Test
    void returnsCorrectUser_whenGetUser_withExistingId() {

        User expectedUser = testCustomer();

        when(userRepository.findById(expectedUser.getId())).thenReturn(Optional.of(expectedUser));

        User actualUser = userService.getUserById(expectedUser.getId());

        assertNotNull(actualUser);
        assertEquals(expectedUser.getId(), actualUser.getId());
    }

    @Test
    void throwsException_whenGetUser_withNonExistingId() {

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> userService.getUserById(any()));
    }

    //Get User By Username
    @Test
    void returnsCorrectUser_whenGetUser_withExistingUsername() {

        User expectedUser = testCustomer();
        when(userRepository.findByUsername("customer_user")).thenReturn(Optional.of(expectedUser));

        User actualUser = userService.getUserByUsername("customer_user");

        assertNotNull(actualUser);
        assertEquals(expectedUser.getUsername(), actualUser.getUsername());
    }

    @Test
    void throwsException_whenGetUser_withNonExistingUsername() {

        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getUserByUsername(any()));
    }

    //Load user by username
    @Test
    void testIf_returningCorrectUserDetails_whenLoadingUserByUsername() {

        User expectedUser = testCustomer();

        when(userRepository.findByUsername("customer_user")).thenReturn(Optional.of(expectedUser));

        UserDetails userDetails = userService.loadUserByUsername("customer_user");

        assertNotNull(userDetails);
        assertInstanceOf(UserAuthDetails.class, userDetails);
        UserAuthDetails userAuthDetails = (UserAuthDetails) userDetails;

        assertEquals(expectedUser.getId(), userAuthDetails.getId());
        assertEquals(expectedUser.getUsername(), userAuthDetails.getUsername());
        assertEquals(expectedUser.getPassword(), userAuthDetails.getPassword());
        assertEquals(expectedUser.getEmail(), userAuthDetails.getEmail());
        assertEquals(expectedUser.getFirstName(), userAuthDetails.getFirstName());
        assertEquals(expectedUser.getLastName(), userAuthDetails.getLastName());
        assertEquals(expectedUser.getRole(), userAuthDetails.getRole());
        assertEquals(expectedUser.isActive(), userAuthDetails.isActive());
    }

    @Test
    void throwsException_whenLoadingUserByUsername_withNonExistingUsername() {

        when(userRepository.findByUsername("customer_user")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("customer_user"));
    }

    //Register
    @Test
    void happyPath_whenRegister() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Gergana")
                .lastName("Brezova")
                .email("gery@gmail.com")
                .username("GeryBr")
                .password("111213")
                .confirmPassword("111213")
                .build();

        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("111213");

        userService.register(registerRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User actualUser = userCaptor.getValue();

        assertEquals("GeryBr", actualUser.getUsername());
        assertEquals("gery@gmail.com", actualUser.getEmail());
        assertEquals("111213", actualUser.getPassword());
        assertEquals("Gergana", actualUser.getFirstName());
        assertEquals("Brezova", actualUser.getLastName());
        assertEquals(UserRole.CUSTOMER, actualUser.getRole());
        assertTrue(actualUser.isActive());
    }

    @Test
    void throwsException_whenRegister_withExistingUsername() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Gergana")
                .lastName("Brezova")
                .email("gery@gmail.com")
                .username("GeryBr")
                .password("111213")
                .confirmPassword("111213")
                .build();

        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.of(new Customer()));

        assertThrows(UsernameAlreadyExists.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void throwsException_whenRegister_withExistingEmail() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Gergana")
                .lastName("Brezova")
                .email("gery@gmail.com")
                .username("GeryBr")
                .password("111213")
                .confirmPassword("111213")
                .build();

        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(new Customer()));

        assertThrows(EmailAlreadyExists.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void throwsException_whenRegister_withNonMatchingPasswords() {

        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Gergana")
                .lastName("Brezova")
                .email("gery@gmail.com")
                .username("GeryBr")
                .password("111213")
                .confirmPassword("212223")
                .build();

        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());

        assertThrows(PasswordsDoNotMatch.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    //Edit User Details
    @Test
    void testIf_UserDetails_areProperlyEdited() {

        EditProfileRequest editRequest = EditProfileRequest.builder()
                .firstName("Gergana")
                .lastName("Brezova")
                .username("GeryBr")
                .email("gery@gmail.com")
                .build();

        User expectedUser = testCustomer();

        when(userRepository.findById(any())).thenReturn(Optional.of(expectedUser));

        userService.editUserDetails(editRequest, expectedUser.getId());

        assertEquals(editRequest.getFirstName(), expectedUser.getFirstName());
        assertEquals(editRequest.getLastName(), expectedUser.getLastName());
        assertEquals(editRequest.getUsername(), expectedUser.getUsername());
        assertEquals(editRequest.getEmail(), expectedUser.getEmail());
        verify(userRepository, times(1)).findById(any());
        verify(userRepository, times(1)).save(expectedUser);
    }

    @Test
    void throwsException_whenEditUserDetailsWithAlreadyExistingUsername() {

        EditProfileRequest editRequest = EditProfileRequest.builder()
                .firstName("Customer")
                .lastName("User")
                .username("existing_customer_user")
                .email("customer_user@gmail.com")
                .build();

        User existingUser = testCustomer();

        when(userRepository.findById(any())).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername(editRequest.getUsername())).thenReturn(true);

        assertThrows(UsernameAlreadyExists.class, () -> userService.editUserDetails(editRequest, existingUser.getId()));

        verify(userRepository, times(1)).findById(any());
        verify(userRepository, never()).save(existingUser);
    }

    @Test
    void throwsException_whenEditUserDetailsWithAlreadyExistingEmail() {

        EditProfileRequest editRequest = EditProfileRequest.builder()
                .firstName("Customer")
                .lastName("User")
                .username("customer_user")
                .email("existing_customer_user@gmail.com")
                .build();

        User existingUser = testCustomer();

        when(userRepository.findById(any())).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(editRequest.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExists.class, () -> userService.editUserDetails(editRequest, existingUser.getId()));

        verify(userRepository, times(1)).findById(any());
        verify(userRepository, never()).save(existingUser);
    }

    //Promote customer to courier
    @Test
    void testIfPromotesCustomerToCourierSuccessfully() {

        Customer customer = testCustomer();
        Employee employee = testEmployee();

        when(userRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(userRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        userService.promoteCustomerToCourier(customer.getId(), employee.getId());

        verify(userRepository).delete(customer);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        Courier savedCourier = (Courier) savedUser;

        assertEquals(customer.getUsername(), savedCourier.getUsername());
        assertEquals(customer.getEmail(), savedCourier.getEmail());
        assertEquals(customer.getPassword(), savedCourier.getPassword());
        assertEquals(customer.getFirstName(), savedCourier.getFirstName());
        assertEquals(customer.getLastName(), savedCourier.getLastName());
        assertEquals(customer.getCreatedOn(), savedCourier.getCreatedOn());
        assertEquals(UserRole.COURIER, savedCourier.getRole());
        assertTrue(savedCourier.isActive());
        assertEquals(BigDecimal.ZERO, savedCourier.getGeneratedTurnover());
        assertEquals(BigDecimal.ZERO, savedCourier.getBonuses());
        assertNotNull(savedCourier.getHiredOn());
        assertEquals(employee, savedCourier.getHiredBy());
    }
}
