package exceptions;

// Thrown when the number of players entered at setup is missing or outside the 2-4 range.
public class InvalidPlayerCountException extends Exception {
    public InvalidPlayerCountException(String message) {
        super(message);
    }
}
