package gr.thmmy.mthmmy.utils.networking;

public class NetworkResultCodes {
    /**
     * The request was successful
     */
    public static final int SUCCESSFUL = 0;
    /**
     * Error 404, page was not found
     */
    public static final int NOT_FOUND = 1;
    /**
     * User session ended while posting the reply
     */
    public static final int SESSION_ENDED = 2;
    /**
     * Exception occured while parsing
     */
    public static final int PARSE_ERROR = 3;
    /**
     * Other undefined of unidentified error
     */
    public static final int OTHER_ERROR = 4;
    /**
     * Failed to connect to thmmy.gr
     */
    public static final int NETWORK_ERROR = 5;
    /**
     * Error while excecuting NetworkTask's performTask()
     */
    public static final int PERFORM_TASK_ERROR = 6;
}
