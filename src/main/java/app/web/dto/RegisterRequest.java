package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @Email(message = "Please enter a valid email format.")
    @NotBlank(message = "Email can not be empty.")
    private String email;

    @NotBlank(message = "First name can not be empty.")
    private String firstName;

    @NotBlank(message = "Last name can not be empty.")
    private String lastName;

    @Size(min = 5, max = 15, message = "Username must be between 5 and 15 symbols.")
    private String username;

    @Size(min = 5, message = "Password must be at least 5 symbols.")
    private String password;

    @Size(min = 5, message = "Password must be at least 5 symbols.")
    private String confirmPassword;

}
