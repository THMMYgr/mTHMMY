package gr.thmmy.mthmmy.utils.parsing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import gr.thmmy.mthmmy.base.BaseApplication;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public abstract class NetworkTask<T> extends ExternalAsyncTask<String, Parcel<T>> {

    private OnParseTaskFinishedListener<T> onParseTaskFinishedListener;

    @Override
    protected final Parcel<T> doInBackground(String... input) {
        Request request = createRequest(input);
        Response response = getResponse(request, BaseApplication.getInstance().getClient());
        String responseBodyString;
        try {
            responseBodyString = response.body().string();
        } catch (IOException e) {
            Timber.e(e);
            return new Parcel<>(Parcel.ResultCode.NETWORK_ERROR, null);
        }
        try {
            T data = performTask(Jsoup.parse(responseBodyString));
            int resultCode = getResultCode(response, data);
            return new Parcel<>(resultCode, data);
        } catch (ParseException pe) {
            Timber.e(pe);
            return new Parcel<>(Parcel.ResultCode.PARSE_ERROR, null);
        } catch (Exception e) {
            Timber.e(e);
            return new Parcel<>(Parcel.ResultCode.PERFORM_TASK_ERROR, null);
        }
    }

    @Override
    protected void onPostExecute(Parcel<T> tParcel) {
        if (onParseTaskFinishedListener != null)
            onParseTaskFinishedListener.onParseFinish(tParcel.getResultCode(), tParcel.getData());
        else
            super.onPostExecute(tParcel);
    }

    abstract Request createRequest(String... input);

    abstract Response getResponse(Request request, OkHttpClient client);

    abstract T performTask(Document document) throws ParseException;

    abstract int getResultCode(Response response, T data);

    public void setOnParseTaskFinishedListener(OnParseTaskFinishedListener<T> onParseTaskFinishedListener) {
        this.onParseTaskFinishedListener = onParseTaskFinishedListener;
    }

    public interface OnParseTaskFinishedListener<T> {
        void onParseFinish(int resultCOde, T data);
    }
}
