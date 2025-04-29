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
public class EditProfileRequest {

    @NotBlank(message = "First name can not be empty.")
    private String firstName;

    @NotBlank(message = "Last name can not be empty.")
    private String lastName;

    @Email(message = "Please enter a valid email format.")
    @NotBlank(message = "Email can not be empty.")
    private String email;

    @Size(min = 5, message = "Password must be at least 5 symbols.")
    private String password;

    @Size(min = 5, message = "Password must be at least 5 symbols.")
    private String confirmPassword;

}
