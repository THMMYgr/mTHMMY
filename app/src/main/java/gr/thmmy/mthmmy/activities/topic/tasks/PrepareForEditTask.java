package gr.thmmy.mthmmy.activities.topic.tasks;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Selector;

import java.io.IOException;

import gr.thmmy.mthmmy.activities.topic.tasks.PrepareForEditResult;
import gr.thmmy.mthmmy.base.BaseApplication;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class PrepareForEditTask extends AsyncTask<String, Void, PrepareForEditResult> {
    private int position;
    private String replyPageUrl;
    private PrepareForEditCallbacks listener;
    private OnPrepareEditFinished finishListener;

    public PrepareForEditTask(PrepareForEditCallbacks listener, OnPrepareEditFinished finishListener, int position, String replyPageUrl) {
        this.listener = listener;
        this.finishListener = finishListener;
        this.position = position;
        this.replyPageUrl = replyPageUrl;
    }

    @Override
    protected void onPreExecute() {
        listener.onPrepareEditStarted();
    }

    @Override
    protected PrepareForEditResult doInBackground(String... strings) {
        Document document;
        String url = strings[0];
        Request request = new Request.Builder()
                .url(url + ";wap2")
                .build();

        try {
            String postText, commitEditURL, numReplies, seqnum, sc, topic;
            OkHttpClient client = BaseApplication.getInstance().getClient();
            Response response = client.newCall(request).execute();
            document = Jsoup.parse(response.body().string());

            Element message = document.select("textarea").first();
            postText = message.text();

            commitEditURL = document.select("form").first().attr("action");
            numReplies = replyPageUrl.substring(replyPageUrl.indexOf("num_replies=") + 12);
            seqnum = document.select("input[name=seqnum]").first().attr("value");
            sc = document.select("input[name=sc]").first().attr("value");
            topic = document.select("input[name=topic]").first().attr("value");

            return new PrepareForEditResult(postText, commitEditURL, numReplies, seqnum, sc, topic, position, true);
        } catch (IOException | Selector.SelectorParseException e) {
            Timber.e(e, "Prepare failed.");
            return new PrepareForEditResult(null, null, null, null, null, null, position, false);
        }
    }

    @Override
    protected void onPostExecute(PrepareForEditResult result) {
        finishListener.onPrepareEditFinished(result, position);
    }

    public interface PrepareForEditCallbacks {
        void onPrepareEditStarted();
        void onPrepareEditCancelled();
    }

    public interface OnPrepareEditFinished {
        void onPrepareEditFinished(PrepareForEditResult result, int position);
    }
}
