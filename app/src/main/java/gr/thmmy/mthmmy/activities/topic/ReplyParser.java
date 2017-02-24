package gr.thmmy.mthmmy.activities.topic;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import okhttp3.Response;

class ReplyParser {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    @SuppressWarnings("unused")
    private static final String TAG = "ReplyParser";

    enum REPLY_STATUS {
        SUCCESSFUL, NO_SUBJECT, EMPTY_BODY, NEW_REPLY_WHILE_POSTING, NOT_FOUND, SESSION_ENDED, OTHER_ERROR
    }

    static REPLY_STATUS replyStatus(Response response) throws IOException {
        if (response.code() == 404) return REPLY_STATUS.NOT_FOUND;
        if (response.code() < 200 || response.code() >= 400) return REPLY_STATUS.OTHER_ERROR;
        String finalUrl = response.request().url().toString();
        if (finalUrl.contains("action=post")) {
            Document postErrorPage = Jsoup.parse(response.body().string());
            String[] errors = postErrorPage.select("tr[id=errors] div[id=error_list]").first()
                    .toString().split("<br>");
            for (int i = 0; i < errors.length; ++i) { //TODO test
                Log.d("TAG", String.valueOf(i));
                Log.d("TAG", errors[i]);
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
            return REPLY_STATUS.NEW_REPLY_WHILE_POSTING;
        }
        return REPLY_STATUS.SUCCESSFUL;
    }
}
