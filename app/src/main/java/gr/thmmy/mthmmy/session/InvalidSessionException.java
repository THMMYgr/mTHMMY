package gr.thmmy.mthmmy.session;

public class InvalidSessionException extends RuntimeException {
    public InvalidSessionException() {}

    public InvalidSessionException(String message) {
        super(message);
    }

    public InvalidSessionException(String message, Throwable cause) {
        super(message, cause);
    }
}
