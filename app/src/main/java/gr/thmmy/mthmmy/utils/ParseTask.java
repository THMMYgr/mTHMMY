package gr.thmmy.mthmmy.utils;

import android.os.AsyncTask;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.utils.exceptions.ParseException;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * An {@link AsyncTask} class to be inherited for asynchronous parsing.
 * Do NOT override doInBackground() and onPostExecute directly.
 * Default usage while executing is ParseTask.execute(urlToParse), however feel free to override
 * and modify prepareRequest() as needed.
 */
public abstract class ParseTask extends AsyncTask<String, Void, ParseTask.ResultCode> {
    protected enum ResultCode {
        SUCCESS, PARSING_ERROR, NETWORK_ERROR, OTHER_ERROR
    }

    protected abstract void parse (Document document) throws ParseException;
    protected abstract void postParsing (ParseTask.ResultCode result);  //ResultCode.NETWORK_ERROR is handled automatically

    protected Request prepareRequest(String... params) {
        return new Request.Builder()
                .url(params[0])
                .build();
    }

    @Override
    protected ResultCode doInBackground(String... params) {
        Request request = prepareRequest(params);
        try {
            Response response = BaseApplication.getInstance().getClient().newCall(request).execute();
            Document document = Jsoup.parse(response.body().string());
            parse(document);
            return ResultCode.SUCCESS;
        } catch (ParseException e) {
            Timber.tag(this.getClass().getSimpleName());
            Timber.e(e, "Parsing Error");
            return ResultCode.PARSING_ERROR;
        } catch (IOException e) {
            Timber.tag(this.getClass().getSimpleName());
            Timber.i(e, "Network Error");
            return ResultCode.NETWORK_ERROR;
        } catch (Exception e) {
            Timber.tag(this.getClass().getSimpleName());
            Timber.e(e, "Other Error");
            return ResultCode.OTHER_ERROR;
        }
    }

    @Override
    protected void onPostExecute(ParseTask.ResultCode result) {
        if (result == ResultCode.NETWORK_ERROR)
            Toast.makeText(BaseApplication.getInstance().getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
        postParsing(result);
    }
}
