package app.exceptions;

public class OrderAlreadyPickedUp extends RuntimeException {
    public OrderAlreadyPickedUp(String message) {
        super(message);
    }
}
