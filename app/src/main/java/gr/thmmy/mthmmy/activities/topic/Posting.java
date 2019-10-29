package gr.thmmy.mthmmy.activities.topic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import okhttp3.Response;
import timber.log.Timber;

/**
 * This is a utility class containing a collection of static methods to help with topic replying.
 */
public class Posting {
    /**
     * {@link REPLY_STATUS} enum defines the different possible outcomes of a topic reply request.
     */
    public enum REPLY_STATUS {
        /**
         * The request was successful
         */
        SUCCESSFUL,
        /**
         * Request was lacking a subject
         */
        NO_SUBJECT,
        /**
         * Request had empty body
         */
        EMPTY_BODY,
        /**
         * There were new topic replies while making the request
         */
        NEW_REPLY_WHILE_POSTING,
        /**
         * Error 404, page was not found
         */
        NOT_FOUND,
        /**
         * User session ended while posting the reply
         */
        SESSION_ENDED,
        /**
         * Other undefined of unidentified error
         */
        OTHER_ERROR
    }

    /**
     * This method can be used to check whether a topic post request was successful or not and if
     * not maybe get the reason why.
     *
     * @param response {@link okhttp3.Response} of the request
     * @return a {@link REPLY_STATUS} that describes the response status
     * @throws IOException method relies to {@link org.jsoup.Jsoup#parse(String)}
     */
    public static REPLY_STATUS replyStatus(Response response) throws IOException {
        if (response.code() == 404) return REPLY_STATUS.NOT_FOUND;
        if (response.code() < 200 || response.code() >= 400) return REPLY_STATUS.OTHER_ERROR;
        String finalUrl = response.request().url().toString();
        if (finalUrl.contains("action=post")) {
            Document postErrorPage = Jsoup.parse(response.body().string());
            Element errorsElement = postErrorPage.select("tr[id=errors] div[id=error_list]").first();
            if(errorsElement!=null){
                String[] errors = errorsElement.toString().split("<br>");
                for (int i = 0; i < errors.length; ++i) { //TODO test
                    Timber.d(String.valueOf(i));
                    Timber.d(errors[i]);
                }
                for (String error : errors) {
                    if (error.contains("Your session timed out while posting") ||
                            error.contains("Υπερβήκατε τον μέγιστο χρόνο σύνδεσης κατά την αποστολή"))
                        return REPLY_STATUS.SESSION_ENDED;
                    if (error.contains("No subject was filled in")
                            || error.contains("Δεν δόθηκε τίτλος"))
                        return REPLY_STATUS.NO_SUBJECT;
                    if (error.contains("The message body was left empty")
                            || error.contains("Δεν δόθηκε κείμενο για το μήνυμα"))
                        return REPLY_STATUS.EMPTY_BODY;
                }
            }
            return REPLY_STATUS.NEW_REPLY_WHILE_POSTING;
        }
        return REPLY_STATUS.SUCCESSFUL;
    }
}
