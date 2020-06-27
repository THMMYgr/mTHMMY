package gr.thmmy.mthmmy.session;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.utils.Parcel;
import gr.thmmy.mthmmy.utils.networking.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.networking.NetworkTask;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import okhttp3.Response;
import timber.log.Timber;

import static gr.thmmy.mthmmy.session.SessionManager.baseLogoutLink;
import static gr.thmmy.mthmmy.session.SessionManager.indexUrl;


public class LogoutTask extends NetworkTask<Void> {
    private String logoutLink;

    public LogoutTask(OnTaskStartedListener onTaskStartedListener, OnNetworkTaskFinishedListener<Void> onParseTaskFinishedListener) {
        super(onTaskStartedListener, onParseTaskFinishedListener);
    }

    @Override
    protected Parcel<Void> doInBackground(String... input) {
        /*  Firstly we will find the logout link
            Keep in mind, server changes sesc at will over time for a given session!
        */
        Parcel<Void> parcel = executeInBackground(indexUrl.toString());
        if(parcel.getResultCode() == NetworkResultCodes.SUCCESSFUL)
            return executeInBackground(logoutLink);  // Now we will attempt to logout
        else return parcel;
    }

    @Override
    protected Void performTask(Document document, Response response) {
        try {
            if(logoutLink==null)
                logoutLink = extractLogoutLink(document);
            else {   // Just for logging purposes
                Elements sessionVerificationFailed = document.select("td:containsOwn(Session " +
                        "verification failed. Please try logging out and back in again, and then try " +
                        "again.), td:containsOwn(Η επαλήθευση συνόδου απέτυχε. Παρακαλούμε κάντε " +
                        "αποσύνδεση, επανασύνδεση και ξαναδοκιμάστε.)");
                if(!sessionVerificationFailed.isEmpty()){
                    Timber.i("Logout failed (invalid session)");
                    throw new InvalidSessionException();
                }
                Elements loginButton = document.select("[value=Login]");  //Attempt to find login button
                if (!loginButton.isEmpty()) //If login button exists, logout was successful
                    Timber.i("Logout successful!");
                else
                    Timber.i("Logout failed");
            }
        } catch (InvalidSessionException ise) {
            throw ise;
        } catch (Exception e) {
            throw new ParseException("Parsing failed", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Parcel<Void> voidParcel) {
        super.onPostExecute(voidParcel);
        //All data should always be cleared from device regardless the result of logout
        BaseApplication.getInstance().getSessionManager().logoutCleanup();
    }

    @Override
    protected int getResultCode(Response response, Void v) {
        return NetworkResultCodes.SUCCESSFUL;
    }

    private String extractLogoutLink(Document document){
        Elements logoutLink = document.select("a[href^=" + baseLogoutLink + "]");

        if (!logoutLink.isEmpty()) {
            String link = logoutLink.first().attr("href");
            if (link != null && !link.isEmpty())
                return link;
        }
        throw new ParseException("Parsing failed (logoutLink extraction)");
    }
}
