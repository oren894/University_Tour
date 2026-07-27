package exceptions;

// Thrown when a player doesn't have enough money to cover a payment.
public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
