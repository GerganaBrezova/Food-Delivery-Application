package app.web.dto;

import app.product.model.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;


import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CreateOrEditProductRequest {

    @Size(min = 1, max = 30, message = "Product name must be at least 1 symbol.")
    private String name;

    @NotNull(message = "Category must be selected")
    private ProductCategory category;

    @Positive(message = "Price must be positive.")
    @NotNull(message = "Please enter a price.")
    private BigDecimal price;

    @URL(message = "Please enter a valid URL format.")
    @NotBlank(message = "Image URL can not be empty.")
    private String imageUrl;

    @NotBlank(message = "Please enter a brand name.")
    private String brandName;

    @NotBlank(message = "Please enter restaurant address.")
    private String restaurantAddress;
}
