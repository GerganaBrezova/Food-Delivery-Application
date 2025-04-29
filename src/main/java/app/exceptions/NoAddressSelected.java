package app.exceptions;

public class NoAddressSelected extends RuntimeException {
    public NoAddressSelected(String message) {
        super(message);
    }
}
