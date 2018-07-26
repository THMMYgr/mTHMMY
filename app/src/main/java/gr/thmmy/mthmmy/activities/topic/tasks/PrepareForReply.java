package gr.thmmy.mthmmy.activities.topic.tasks;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Selector;

import java.io.IOException;
import java.util.ArrayList;

import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.Post;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class PrepareForReply extends AsyncTask<Integer, Void, PrepareForReplyResult> {
    private PrepareForReplyCallbacks listener;
    private OnPrepareForReplyFinished finishListener;
    private String replyPageUrl;
    private ArrayList<Post> postsList;

    public PrepareForReply(PrepareForReplyCallbacks listener, OnPrepareForReplyFinished finishListener,
                           String replyPageUrl, ArrayList<Post> postsList) {
        this.listener = listener;
        this.finishListener = finishListener;
        this.replyPageUrl = replyPageUrl;
        this.postsList = postsList;
    }

    @Override
    protected void onPreExecute() {
        listener.onPrepareForReplyStarted();
    }

    @Override
    protected PrepareForReplyResult doInBackground(Integer... quoteList) {
        String numReplies = null;
        String seqnum = null;
        String sc = null;
        String topic = null;
        StringBuilder buildedQuotes = new StringBuilder("");

        Document document;
        Request request = new Request.Builder()
                .url(replyPageUrl + ";wap2")
                .build();

        OkHttpClient client = BaseApplication.getInstance().getClient();
        try {
            Response response = client.newCall(request).execute();
            document = Jsoup.parse(response.body().string());

            numReplies = replyPageUrl.substring(replyPageUrl.indexOf("num_replies=") + 12);
            seqnum = document.select("input[name=seqnum]").first().attr("value");
            sc = document.select("input[name=sc]").first().attr("value");
            topic = document.select("input[name=topic]").first().attr("value");
        } catch (IOException | Selector.SelectorParseException e) {
            Timber.e(e, "Prepare failed.");
        }

        for (Integer quotePosition : quoteList) {
            request = new Request.Builder()
                    .url("https://www.thmmy.gr/smf/index.php?action=quotefast;quote=" +
                            postsList.get(quotePosition).getPostIndex() +
                            ";" + "sesc=" + sc + ";xml")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                buildedQuotes.append(body.substring(body.indexOf("<quote>") + 7, body.indexOf("</quote>")));
                buildedQuotes.append("\n\n");
            } catch (IOException | Selector.SelectorParseException e) {
                Timber.e(e, "Quote building failed.");
            }
        }
        return new PrepareForReplyResult(numReplies, seqnum, sc, topic, buildedQuotes.toString());
    }

    @Override
    protected void onPostExecute(PrepareForReplyResult result) {
        finishListener.onPrepareForReplyFinished(result);
    }

    public interface PrepareForReplyCallbacks {
        void onPrepareForReplyStarted();
        void onPrepareForReplyCancelled();
    }

    public interface OnPrepareForReplyFinished {
        void onPrepareForReplyFinished(PrepareForReplyResult result);
    }
}
