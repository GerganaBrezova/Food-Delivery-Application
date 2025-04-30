package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CreateCompanyRequest {

    @Size(min = 1, max = 30, message = "Brand name must be at least 1 symbol.")
    private String name;

    @Size(min = 1, max = 100, message = "Brand motto must be at least 1 symbols.")
    private String description;

    @URL(message = "Please enter a valid URL format.")
    @NotBlank(message = "Image URL can not be empty.")
    private String logoUrl;
}
