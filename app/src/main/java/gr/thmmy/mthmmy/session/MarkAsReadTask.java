package gr.thmmy.mthmmy.session;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import gr.thmmy.mthmmy.utils.Parcel;
import gr.thmmy.mthmmy.utils.networking.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.networking.NetworkTask;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import okhttp3.Response;

import static gr.thmmy.mthmmy.session.SessionManager.baseMarkAllAsReadLink;
import static gr.thmmy.mthmmy.session.SessionManager.unreadUrl;

public class MarkAsReadTask extends NetworkTask<Void> {
    private String markAsReadLink;

    public MarkAsReadTask(OnTaskStartedListener onTaskStartedListener, OnNetworkTaskFinishedListener<Void> onParseTaskFinishedListener) {
        super(onTaskStartedListener, onParseTaskFinishedListener);
    }

    @Override
    protected Parcel<Void> doInBackground(String... input) {
        Parcel<Void> parcel = executeInBackground(unreadUrl.toString());
        if(parcel.getResultCode() == NetworkResultCodes.SUCCESSFUL)
            return executeInBackground(markAsReadLink);
        else return parcel;
    }

    @Override
    protected Void performTask(Document document, Response response) {
        try {
            Elements sessionVerificationFailed = document.select("td:containsOwn(Session " +
                    "verification failed. Please try logging out and back in again, and then try " +
                    "again.), td:containsOwn(Η επαλήθευση συνόδου απέτυχε. Παρακαλούμε κάντε " +
                    "αποσύνδεση, επανασύνδεση και ξαναδοκιμάστε.)");
            if(!sessionVerificationFailed.isEmpty())
                throw new InvalidSessionException();
            if(markAsReadLink==null)
                markAsReadLink = extractMarkAsReadLink(document);

        } catch (InvalidSessionException ise) {
            throw ise;
        } catch (Exception e) {
            throw new ParseException("Parsing failed", e);
        }
        return null;
    }

    @Override
    protected int getResultCode(Response response, Void v) {
        return NetworkResultCodes.SUCCESSFUL;
    }

    private String extractMarkAsReadLink(Document document){
        Elements markAllAsReadLink = document.select("a[href^=" + baseMarkAllAsReadLink + "]");

        if (!markAllAsReadLink.isEmpty()) {
            String link = markAllAsReadLink.first().attr("href");
            if (link != null && !link.isEmpty())
                return link;
        }
        throw new ParseException("Parsing failed (markAllAsReadLink extraction)");
    }
}
