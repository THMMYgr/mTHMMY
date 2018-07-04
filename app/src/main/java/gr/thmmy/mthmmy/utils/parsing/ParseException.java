package gr.thmmy.mthmmy.utils.parsing;

/**
 * ParseException is to be used for errors while parsing.
 */
public class ParseException extends Exception {
    public ParseException() {}

    public ParseException(String message)
    {
        super(message);
    }
}
