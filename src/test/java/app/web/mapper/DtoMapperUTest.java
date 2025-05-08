package app.web.mapper;

import app.product.model.Product;
import app.web.dto.EditProductRequest;
import app.web.dto.EditProfileRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static app.TestBuilder.testCustomer;
import static app.TestBuilder.testProductList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class DtoMapperUTest {

    @Test
    void happyPathWhenMappingUserToEditProfileRequest() {

        EditProfileRequest editRequest = DtoMapper.mapToEditProfileRequest(testCustomer());

        assertNotNull(editRequest);
        assertEquals(testCustomer().getFirstName(), editRequest.getFirstName());
        assertEquals(testCustomer().getLastName(), editRequest.getLastName());
        assertEquals(testCustomer().getEmail(), editRequest.getEmail());
        assertEquals(testCustomer().getUsername(), editRequest.getUsername());
    }

    @Test
    void happyPathWhenMappingProductToEditProductRequest() {

        Product product = testProductList().get(0);

        EditProductRequest editRequest = DtoMapper.mapToEditProductRequest(product);

        assertNotNull(editRequest);
        assertEquals(product.getName(), editRequest.getName());
        assertEquals(product.getPrice(), editRequest.getPrice());
        assertEquals(product.getCategory(), editRequest.getCategory());
        assertEquals(product.getImageUrl(), editRequest.getImageUrl());
    }
}
