package gr.thmmy.mthmmy.utils;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.utils.exceptions.ParseException;

import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public abstract class ParseTask extends AsyncTask<String, Void, ParseTask.ResultCode> {
    protected enum ResultCode {
        SUCCESS, PARSING_ERROR, NETWORK_ERROR, OTHER_ERROR
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

    protected abstract Request prepareRequest(String... params);
    protected abstract void parse (Document document) throws ParseException;
}

