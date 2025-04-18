package app.web.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Size(min = 5, max = 15, message = "Username must be between 5 and 15 symbols.")
    private String username;

    @Size(min = 5, message = "Password must be at least 5 symbols.")
    private String password;

}
