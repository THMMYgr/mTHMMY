package gr.thmmy.mthmmy.utils.exceptions;

/**
 * UnknownException is thrown upon an error (see Report.java in release), when no other specific
 * exception is set, to report to FireBase.
 */
public class UnknownException extends Exception {
    public UnknownException() {
    }

    public UnknownException(String message) {
        super(message);
    }
}
