package app.exceptions;

public class OrderHasNoProducts extends RuntimeException {
  public OrderHasNoProducts(String message) {
    super(message);
  }
}
