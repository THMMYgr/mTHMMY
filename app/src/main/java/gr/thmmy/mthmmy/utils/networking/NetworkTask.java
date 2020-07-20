package gr.thmmy.mthmmy.utils.networking;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.session.InvalidSessionException;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.ExternalAsyncTask;
import gr.thmmy.mthmmy.utils.Parcel;
import gr.thmmy.mthmmy.utils.crashreporting.CrashReporter;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public abstract class NetworkTask<T> extends ExternalAsyncTask<String, Parcel<T>> {
    private OnNetworkTaskFinishedListener<T> onNetworkTaskFinishedListener;

    public NetworkTask(OnTaskStartedListener onTaskStartedListener, OnTaskCancelledListener onTaskCancelledListener,
                       OnNetworkTaskFinishedListener<T> onNetworkTaskFinishedListener) {
        super(onTaskStartedListener, onTaskCancelledListener, null);
        this.onNetworkTaskFinishedListener = onNetworkTaskFinishedListener;
    }

    public NetworkTask(OnTaskStartedListener onTaskStartedListener, OnNetworkTaskFinishedListener<T> onNetworkTaskFinishedListener) {
        super(onTaskStartedListener, null);
        this.onNetworkTaskFinishedListener = onNetworkTaskFinishedListener;
    }

    public NetworkTask() {}

    @Override
    protected Parcel<T> doInBackground(String... input) {
        return executeInBackground(input);
    }

    @Override
    protected void onPostExecute(Parcel<T> tParcel) {
        if (onNetworkTaskFinishedListener != null)
            onNetworkTaskFinishedListener.onNetworkTaskFinished(tParcel.getResultCode(), tParcel.getData());
        else
            super.onPostExecute(tParcel);
    }

    protected Parcel<T> executeInBackground(String... input) {
        Response response;
        try {
            response = sendRequest(BaseApplication.getInstance().getClient(), input);
        } catch (IOException e) {
            Timber.e(e, "Error connecting to thmmy.gr");
            return new Parcel<>(NetworkResultCodes.NETWORK_ERROR, null);
        }
        String responseBodyString;
        try {
            responseBodyString = response.body().string();
        } catch (NullPointerException npe) {
            Timber.wtf(npe, "Invalid response. Details: https://square.github.io/okhttp/3.x/okhttp/okhttp3/Response.html#body--");
            return new Parcel<>(NetworkResultCodes.NETWORK_ERROR, null);
        } catch (IOException e) {
            Timber.e(e, "Error getting response body string");
            return new Parcel<>(NetworkResultCodes.NETWORK_ERROR, null);
        }
        try {
            T data = performTask(Jsoup.parse(responseBodyString), response);
            int resultCode = getResultCode(response, data);
            return new Parcel<>(resultCode, data);
        } catch (ParseException pe) {
            Timber.e(pe);
            SharedPreferences settingsPreferences = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
            if (settingsPreferences.getBoolean(BaseApplication.getInstance()
                    .getString(R.string.pref_privacy_crashlytics_enable_key), false))
                CrashReporter.reportForumInfo(Jsoup.parse(responseBodyString));
            return new Parcel<>(NetworkResultCodes.PARSE_ERROR, null);
        } catch (InvalidSessionException ise) {
            //TODO: Uncomment the lines below when UI is ready to auto-adjust to changes in session data
            // BaseApplication.getInstance().getSessionManager().clearSessionData();
            // BaseApplication.getInstance().getSessionManager().guestLogin();
            return new Parcel<>(SessionManager.INVALID_SESSION, null);
        }catch (Exception e) {
            Timber.e(e);
            return new Parcel<>(NetworkResultCodes.PERFORM_TASK_ERROR, null);
        }
    }

    protected Response sendRequest(OkHttpClient client, String... input) throws IOException {
        String url = input[0];
        Request request = new Request.Builder()
                .url(url)
                .build();
        return client.newCall(request).execute();
    }

    protected abstract T performTask(Document document, Response response);

    protected abstract int getResultCode(Response response, T data);

    public void setOnNetworkTaskFinishedListener(OnNetworkTaskFinishedListener<T> onNetworkTaskFinishedListener) {
        this.onNetworkTaskFinishedListener = onNetworkTaskFinishedListener;
    }

    public interface OnNetworkTaskFinishedListener<T> {
        void onNetworkTaskFinished(int resultCode, T data);
    }
}
