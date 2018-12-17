package gr.thmmy.mthmmy.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public abstract class NetworkTask<T> extends ExternalAsyncTask<String, Parcel<T>> {

    protected OnNetworkTaskFinishedListener<T> onNetworkTaskFinishedListener;

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
    protected final Parcel<T> doInBackground(String... input) {
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
            Timber.wtf(npe, "Invalid response. Detatails: https://square.github.io/okhttp/3.x/okhttp/okhttp3/Response.html#body--");
            return new Parcel<>(NetworkResultCodes.NETWORK_ERROR, null);
        } catch (IOException e) {
            Timber.e(e, "Error getting response body string");
            return new Parcel<>(NetworkResultCodes.NETWORK_ERROR, null);
        }
        try {
            T data = performTask(ParseHelpers.parse(responseBodyString), response);
            int resultCode = getResultCode(response, data);
            return new Parcel<>(resultCode, data);
        } catch (ParseException pe) {
            Timber.e(pe);
            SharedPreferences settingsPreferences = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
            if (settingsPreferences.getBoolean(BaseApplication.getInstance()
                    .getString(R.string.pref_privacy_crashlytics_enable_key), false))
                CrashReporter.reportForumInfo(Jsoup.parse(responseBodyString));
            return new Parcel<>(NetworkResultCodes.PARSE_ERROR, null);
        } catch (Exception e) {
            Timber.e(e);
            return new Parcel<>(NetworkResultCodes.PERFORM_TASK_ERROR, null);
        }
    }

    @Override
    protected void onPostExecute(Parcel<T> tParcel) {
        if (onNetworkTaskFinishedListener != null)
            onNetworkTaskFinishedListener.onNetworkTaskFinished(tParcel.getResultCode(), tParcel.getData());
        else
            super.onPostExecute(tParcel);
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
