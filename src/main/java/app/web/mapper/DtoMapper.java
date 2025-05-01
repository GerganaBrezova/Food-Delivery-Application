package app.web.mapper;

import app.product.model.Product;
import app.user.model.User;
import app.web.dto.EditProductRequest;
import app.web.dto.EditProfileRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public EditProfileRequest mapToEditProfileRequest(User user) {

        return EditProfileRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }

    public EditProductRequest mapToEditProductRequest(Product product) {

        return EditProductRequest.builder()
                .name(product.getName())
                .price(product.getPrice())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .build();
    }
}
