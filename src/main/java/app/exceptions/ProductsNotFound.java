package app.exceptions;

public class ProductsNotFound extends RuntimeException {
    public ProductsNotFound(String message) {
        super(message);
    }
}
