package gr.thmmy.mthmmy.utils.parsing;

/**
 * ParseException is to be used for errors while parsing.
 */
public class ParseException extends RuntimeException {
    public ParseException() {}

    public ParseException(String message)
    {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
